package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ScanRecordEntity
import com.example.data.db.SecurityDatabase
import com.example.data.model.DetectedThreat
import com.example.data.model.ScanProgress
import com.example.data.model.ScanState
import com.example.data.model.ScanType
import com.example.data.model.SecurityOverviewState
import com.example.data.model.SystemSecurityReport
import com.example.data.model.ThreatSeverity
import com.example.data.repository.SecurityScannerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SecurityScannerRepository

    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _detectedThreats = MutableStateFlow<List<DetectedThreat>>(emptyList())
    val detectedThreats: StateFlow<List<DetectedThreat>> = _detectedThreats.asStateFlow()

    private val _systemReport = MutableStateFlow(SystemSecurityReport())
    val systemReport: StateFlow<SystemSecurityReport> = _systemReport.asStateFlow()

    private val _overviewState = MutableStateFlow(SecurityOverviewState())
    val overviewState: StateFlow<SecurityOverviewState> = _overviewState.asStateFlow()

    val scanHistory: StateFlow<List<ScanRecordEntity>>

    private var scanJob: Job? = null

    init {
        val db = SecurityDatabase.getInstance(application)
        repository = SecurityScannerRepository(application, db.securityDao())

        scanHistory = repository.scanHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load initial state and system audit
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val report = repository.performSystemSecurityAudit()
            _systemReport.value = report

            val latest = repository.getLatestScan()
            val totalApps = try {
                getApplication<Application>().packageManager.getInstalledPackages(0).size
            } catch (_: Exception) {
                0
            }

            if (latest != null) {
                _overviewState.value = SecurityOverviewState(
                    lastScanTimestamp = latest.timestamp,
                    healthScore = latest.securityScore,
                    totalAppsInstalled = totalApps,
                    totalThreatsCount = latest.threatsFoundCount,
                    highRiskCount = 0,
                    mediumRiskCount = 0,
                    statusTitle = if (latest.threatsFoundCount > 0) "تم اكتشاف تهديدات محتملة" else "الهاتف محمي وآمن",
                    statusSubtitle = "آخر فحص: ${latest.scanType} (${latest.totalAppsScanned} تطبيق)"
                )
            } else {
                _overviewState.value = SecurityOverviewState(
                    lastScanTimestamp = null,
                    healthScore = 95,
                    totalAppsInstalled = totalApps,
                    totalThreatsCount = 0,
                    statusTitle = "جاهز للفحص الأمني",
                    statusSubtitle = "اضغط على زر الفحص للتحقق من خلو هاتفك من التهديدات"
                )
            }
        }
    }

    fun startScan(scanType: ScanType) {
        if (_scanProgress.value.state == ScanState.SCANNING) return

        scanJob?.cancel()
        _detectedThreats.value = emptyList()

        scanJob = viewModelScope.launch {
            repository.performScan(
                scanType = scanType,
                onThreatDetected = { threat ->
                    _detectedThreats.value = _detectedThreats.value + threat
                    vibrateBriefly(50)
                }
            ).collect { progress ->
                _scanProgress.value = progress

                if (progress.state == ScanState.COMPLETED) {
                    onScanFinished(scanType, progress)
                }
            }
        }
    }

    private suspend fun onScanFinished(scanType: ScanType, progress: ScanProgress) {
        vibrateCompletion()

        val threats = _detectedThreats.value.filter { !it.isWhitelisted }
        val highRiskCount = threats.count { it.severity == ThreatSeverity.HIGH_RISK || it.severity == ThreatSeverity.CRITICAL }
        val mediumRiskCount = threats.count { it.severity == ThreatSeverity.MEDIUM_RISK }

        var calculatedScore = 100
        calculatedScore -= (highRiskCount * 25)
        calculatedScore -= (mediumRiskCount * 10)
        if (_systemReport.value.isRooted) calculatedScore -= 20
        if (_systemReport.value.isUsbDebuggingActive) calculatedScore -= 5
        calculatedScore = calculatedScore.coerceIn(10, 100)

        val statusTitle = when {
            highRiskCount > 0 -> "تحذير: تم اكتشاف برمجيات خطيرة!"
            mediumRiskCount > 0 -> "تنبيه: توجد تطبيقات مشبوهة بحاجة لمراجعة"
            else -> "الهاتف محمي وخالٍ من الفيروسات"
        }

        val statusSubtitle = when {
            threats.isEmpty() -> "تم فحص ${progress.currentScannedCount} تطبيقاً والتأكد من سلامة النظام"
            else -> "تم اكتشاف ${threats.size} تهديد أمني بحاجة لمعالجة سريعة"
        }

        val now = System.currentTimeMillis()
        _overviewState.value = SecurityOverviewState(
            lastScanTimestamp = now,
            healthScore = calculatedScore,
            totalAppsInstalled = progress.totalAppsToScan,
            totalThreatsCount = threats.size,
            highRiskCount = highRiskCount,
            mediumRiskCount = mediumRiskCount,
            statusTitle = statusTitle,
            statusSubtitle = statusSubtitle
        )

        // Save scan to database
        repository.saveScanRecord(
            scanType = scanType,
            totalAppsScanned = progress.currentScannedCount,
            threatsCount = threats.size,
            securityScore = calculatedScore,
            summaryAr = statusTitle
        )
    }

    fun stopScan() {
        scanJob?.cancel()
        _scanProgress.value = _scanProgress.value.copy(
            state = ScanState.STOPPED,
            currentPhaseText = "تم إيقاف الفحص من قبل المستخدم"
        )
    }

    fun trustApp(threat: DetectedThreat) {
        viewModelScope.launch {
            repository.trustApp(threat.packageName, threat.appName)
            _detectedThreats.value = _detectedThreats.value.map {
                if (it.packageName == threat.packageName) it.copy(isWhitelisted = true) else it
            }
            updateOverviewAfterResolution()
        }
    }

    fun unTrustApp(packageName: String) {
        viewModelScope.launch {
            repository.unTrustApp(packageName)
            _detectedThreats.value = _detectedThreats.value.map {
                if (it.packageName == packageName) it.copy(isWhitelisted = false) else it
            }
            updateOverviewAfterResolution()
        }
    }

    private fun updateOverviewAfterResolution() {
        val remainingActive = _detectedThreats.value.filter { !it.isWhitelisted }
        val highRiskCount = remainingActive.count { it.severity == ThreatSeverity.HIGH_RISK || it.severity == ThreatSeverity.CRITICAL }
        val mediumRiskCount = remainingActive.count { it.severity == ThreatSeverity.MEDIUM_RISK }

        var calculatedScore = 100
        calculatedScore -= (highRiskCount * 25)
        calculatedScore -= (mediumRiskCount * 10)
        calculatedScore = calculatedScore.coerceIn(10, 100)

        val statusTitle = if (remainingActive.isEmpty()) "الهاتف محمي وخالٍ من الفيروسات" else "تم اكتشاف تهديدات أمنية"

        _overviewState.value = _overviewState.value.copy(
            healthScore = calculatedScore,
            totalThreatsCount = remainingActive.size,
            highRiskCount = highRiskCount,
            mediumRiskCount = mediumRiskCount,
            statusTitle = statusTitle
        )
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun refreshSystemAudit() {
        viewModelScope.launch {
            _systemReport.value = repository.performSystemSecurityAudit()
        }
    }

    fun openAppDetails(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun uninstallApp(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppDetails(context, packageName)
        }
    }

    fun openSecuritySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    private fun vibrateBriefly(millis: Long) {
        try {
            val v = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(millis)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateCompletion() {
        try {
            val v = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 100, 80, 150)
                v?.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(250)
            }
        } catch (_: Exception) {}
    }
}
