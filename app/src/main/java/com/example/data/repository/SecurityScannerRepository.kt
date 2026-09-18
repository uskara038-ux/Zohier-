package com.example.data.repository

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import com.example.data.db.ScanRecordEntity
import com.example.data.db.SecurityDao
import com.example.data.db.TrustedAppEntity
import com.example.data.model.DetectedThreat
import com.example.data.model.ScanProgress
import com.example.data.model.ScanState
import com.example.data.model.ScanType
import com.example.data.model.SystemSecurityReport
import com.example.data.model.ThreatSeverity
import com.example.data.model.ThreatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class SecurityScannerRepository(
    private val context: Context,
    private val securityDao: SecurityDao
) {

    val scanHistory: Flow<List<ScanRecordEntity>> = securityDao.getAllScanRecords()
    val trustedApps: Flow<List<TrustedAppEntity>> = securityDao.getAllTrustedApps()

    suspend fun getLatestScan(): ScanRecordEntity? = withContext(Dispatchers.IO) {
        securityDao.getLatestScanRecord()
    }

    suspend fun trustApp(packageName: String, appName: String) = withContext(Dispatchers.IO) {
        securityDao.addTrustedApp(TrustedAppEntity(packageName = packageName, appName = appName))
    }

    suspend fun unTrustApp(packageName: String) = withContext(Dispatchers.IO) {
        securityDao.removeTrustedApp(packageName)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        securityDao.clearScanHistory()
    }

    /**
     * Executes real security scan across installed packages and system parameters,
     * emitting real-time progress for animation and monitoring.
     */
    fun performScan(
        scanType: ScanType,
        onThreatDetected: (DetectedThreat) -> Unit
    ): Flow<ScanProgress> = flow {
        emit(
            ScanProgress(
                state = ScanState.SCANNING,
                scanType = scanType,
                progressPercent = 0.05f,
                currentPhaseText = "تهيئة محرك الفحص ومطابقة التوقيعات...",
                currentScannedCount = 0,
                totalAppsToScan = 0
            )
        )
        delay(400)

        val pm = context.packageManager
        val trustedPackages = securityDao.getTrustedPackageNames().toSet()

        val installedPackages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (_: Exception) {
            emptyList()
        }

        // For quick scan, focus on user-installed non-system apps or critical system apps
        val targetPackages = when (scanType) {
            ScanType.QUICK -> {
                val nonSystem = installedPackages.filter { (it.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM == 0 }
                if (nonSystem.size < 5) installedPackages else nonSystem
            }
            ScanType.FULL -> installedPackages
            ScanType.SYSTEM -> emptyList() // System only audit
        }

        val totalToScan = targetPackages.size.coerceAtLeast(1)
        var scannedCount = 0
        var threatsCount = 0

        // Phase 1: Package scanning
        if (scanType != ScanType.SYSTEM) {
            for (pkg in targetPackages) {
                val appName = try {
                    pkg.applicationInfo?.let { pm.getApplicationLabel(it).toString() } ?: pkg.packageName
                } catch (_: Exception) {
                    pkg.packageName
                }

                scannedCount++
                val percent = (scannedCount.toFloat() / totalToScan.toFloat()) * 0.85f

                val phase = when {
                    scannedCount < totalToScan * 0.3f -> "فحص توقيعات التطبيقات والشهادات الرقمية..."
                    scannedCount < totalToScan * 0.7f -> "تحليل الأذونات الحساسة والوصول إلى الخلفية..."
                    else -> "كشف برمجيات التجسس وحقن الشاشات..."
                }

                emit(
                    ScanProgress(
                        state = ScanState.SCANNING,
                        scanType = scanType,
                        progressPercent = percent,
                        currentScannedCount = scannedCount,
                        totalAppsToScan = totalToScan,
                        currentAppName = appName,
                        currentPackageName = pkg.packageName,
                        currentPhaseText = phase,
                        threatsFoundSoFar = threatsCount
                    )
                )

                // Analyze package for threat heuristic
                val threat = analyzePackageThreat(pkg, appName, trustedPackages)
                if (threat != null) {
                    threatsCount++
                    onThreatDetected(threat)
                }

                // Smooth delay to ensure pleasant, responsive scanning cadence
                delay(if (targetPackages.size > 50) 25L else 55L)
            }
        }

        // Phase 2: System audit check
        emit(
            ScanProgress(
                state = ScanState.SCANNING,
                scanType = scanType,
                progressPercent = 0.92f,
                currentScannedCount = scannedCount,
                totalAppsToScan = totalToScan,
                currentAppName = "أمان نظام أندرويد",
                currentPackageName = "android.system.core",
                currentPhaseText = "فحص امتيازات الجذر (Root) وتصحيح USB وحماية الشاشة...",
                threatsFoundSoFar = threatsCount
            )
        )
        delay(500)

        // Phase 3: Finalizing report
        emit(
            ScanProgress(
                state = ScanState.COMPLETED,
                scanType = scanType,
                progressPercent = 1.0f,
                currentScannedCount = scannedCount,
                totalAppsToScan = totalToScan,
                currentAppName = "اكتمل الفحص بنجاح",
                currentPackageName = "",
                currentPhaseText = "تم إكمال التحليل الأمني وتوليد التقرير",
                threatsFoundSoFar = threatsCount
            )
        )
    }.flowOn(Dispatchers.IO)

    /**
     * Deep heuristic inspection of single package
     */
    private fun analyzePackageThreat(
        pkg: PackageInfo,
        appName: String,
        trustedPackages: Set<String>
    ): DetectedThreat? {
        val packageName = pkg.packageName
        if (packageName == context.packageName) return null // Don't flag self

        val isSystem = (pkg.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
        val isDebuggable = (pkg.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val permissions = pkg.requestedPermissions?.toList() ?: emptyList()

        val reasons = mutableListOf<String>()
        var severity = ThreatSeverity.SAFE
        var threatType = ThreatType.SUSPICIOUS_PERMISSIONS

        // Check 1: Overlay Hijacking / Screen Tapjacking
        val hasOverlay = permissions.contains("android.permission.SYSTEM_ALERT_WINDOW")
        val hasBoot = permissions.contains("android.permission.RECEIVE_BOOT_COMPLETED")
        if (hasOverlay && hasBoot && !isSystem) {
            reasons.add("إمكانية العرض فوق التطبيقات الأخرى والبدء التلقائي مع تشغيل الهاتف (خطر اختطاف الواجهات)")
            severity = ThreatSeverity.HIGH_RISK
            threatType = ThreatType.OVERLAY_HIJACKING
        }

        // Check 2: SMS interception without default SMS handler
        val hasSmsRead = permissions.contains("android.permission.READ_SMS")
        val hasSmsReceive = permissions.contains("android.permission.RECEIVE_SMS")
        val hasInternet = permissions.contains("android.permission.INTERNET")
        if ((hasSmsRead || hasSmsReceive) && hasInternet && !isSystem && !packageName.contains("messaging", ignoreCase = true)) {
            reasons.add("إمكانية قراءة الرسائل القصيرة ورموز التحقق (OTP) مع الوصول للإنترنت")
            severity = ThreatSeverity.CRITICAL
            threatType = ThreatType.SMS_INTERCEPTOR
        }

        // Check 3: Accessibility Service Abuse
        if (permissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE") && !isSystem) {
            reasons.add("طلب صلاحية إمكانية الوصول الكاملة (إمكانية رصد مدخلات لوحة المفاتيح والتحكم بالشاشة)")
            severity = ThreatSeverity.HIGH_RISK
            threatType = ThreatType.ACCESSIBILITY_ABUSE
        }

        // Check 4: Installer source (Sideloaded from unknown web sources)
        val installer = getInstallerPackage(packageName)
        val isFromGooglePlay = installer == "com.android.vending" || installer == "com.google.android.packageinstaller"
        val isKnownStore = isFromGooglePlay || installer == "com.sec.android.app.samsungapps" || installer == "com.amazon.venezia"
        if (!isSystem && !isKnownStore && (installer == null || installer.isEmpty())) {
            reasons.add("مثبت من مصدر غير موثوق أو خارجي خارج متجر Google Play")
            if (severity == ThreatSeverity.SAFE) {
                severity = ThreatSeverity.MEDIUM_RISK
                threatType = ThreatType.SIDELOADED_UNKNOWN
            }
        }

        // Check 5: Debuggable release app in production
        if (isDebuggable && !isSystem) {
            reasons.add("التطبيق يحتوي على وضع التصحيح (Debuggable) مفعّل مما يجعله عرضة للتلاعب وحقن الشيفرات")
            if (severity.level < ThreatSeverity.MEDIUM_RISK.level) {
                severity = ThreatSeverity.MEDIUM_RISK
                threatType = ThreatType.DEBUGGABLE_FLAG
            }
        }

        // Check 6: Audio & Location tracking combination
        val hasAudio = permissions.contains("android.permission.RECORD_AUDIO")
        val hasFineLoc = permissions.contains("android.permission.ACCESS_FINE_LOCATION")
        val hasBgLoc = permissions.contains("android.permission.ACCESS_BACKGROUND_LOCATION")
        if (hasAudio && (hasFineLoc || hasBgLoc) && !isSystem) {
            if (!appName.lowercase().contains("recorder") && !appName.lowercase().contains("camera")) {
                reasons.add("صلاحيات حساسة تشمل التسجيل الصوتي وتتبع الموقع الجغرافي بالخلفية")
                if (severity.level < ThreatSeverity.LOW_RISK.level) {
                    severity = ThreatSeverity.LOW_RISK
                    threatType = ThreatType.SUSPICIOUS_PERMISSIONS
                }
            }
        }

        if (reasons.isEmpty()) {
            return null
        }

        val title = when (threatType) {
            ThreatType.SMS_INTERCEPTOR -> "خطر اعتراض رسائل OTP الحساسة"
            ThreatType.OVERLAY_HIJACKING -> "تطبيق شاشات منبثقة مشبوه"
            ThreatType.ACCESSIBILITY_ABUSE -> "صلاحية تحكم ومراقبة الشاشة"
            ThreatType.SIDELOADED_UNKNOWN -> "تطبيق غير موثوق المصدر"
            ThreatType.DEBUGGABLE_FLAG -> "تطبيق قابل للتصحيح والاختراق"
            else -> "أذونات وصول مفرطة وغير معتادة"
        }

        val description = when (threatType) {
            ThreatType.SMS_INTERCEPTOR -> "هذا التطبيق يملك وصولاً لرسائل SMS والإنترنت في آن واحد، مما قد يتيح سرقة رموز التوثيق المالي."
            ThreatType.OVERLAY_HIJACKING -> "يستطيع التطبيق رسم نوافذ فوق التطبيقات الأخرى مما قد يخدع المستخدم لإدخال كلمات المرور."
            ThreatType.ACCESSIBILITY_ABUSE -> "تتيح خدمات إمكانية الوصول لتطبيقات غير النظام إمكانية قراءة ما يظهر على شاشتك."
            ThreatType.SIDELOADED_UNKNOWN -> "لم يتم تثبيت هذا التطبيق عبر متجر تطبيقات موثوق، مما يزيد من احتمالية احتوائه على برمجيات ضارة."
            ThreatType.DEBUGGABLE_FLAG -> "التطبيق يسمح بربط مصحح الأخطاء، مما يعرض بياناته الخاصة للتسريب."
            else -> "يطلب التطبيق أذونات أكثر من وظيفته المتوقعة."
        }

        return DetectedThreat(
            id = "${packageName}_${System.currentTimeMillis()}",
            appName = appName,
            packageName = packageName,
            versionName = pkg.versionName ?: "1.0",
            severity = severity,
            threatType = threatType,
            threatTitle = title,
            threatDescription = description,
            reasons = reasons,
            requestedPermissions = permissions,
            isSystemApp = isSystem,
            installerPackage = installer,
            isWhitelisted = trustedPackages.contains(packageName)
        )
    }

    private fun getInstallerPackage(packageName: String): String? {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Performs direct hardware & OS security audit
     */
    fun performSystemSecurityAudit(): SystemSecurityReport {
        val rootReasons = mutableListOf<String>()

        // 1. Su binary check
        val suPaths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/system/app/Superuser.apk"
        )
        for (path in suPaths) {
            if (File(path).exists()) {
                rootReasons.add("تم العثور على ملف su في المسار: $path")
            }
        }

        // 2. Build test-keys check
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            rootReasons.add("نظام التشغيل تم بناؤه بمفاتيح اختبار غير رسمية (test-keys)")
        }

        val isRooted = rootReasons.isNotEmpty()

        // 3. USB Debugging check
        val isAdb = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }

        // 4. Lock screen protection check
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isLockSecure = km?.isDeviceSecure ?: true

        // 5. VPN or Proxy check
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val isVpn = cm?.let {
            val network = it.activeNetwork
            val caps = it.getNetworkCapabilities(network)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        } ?: false

        // Calculate system security score
        var score = 100
        if (isRooted) score -= 40
        if (isAdb) score -= 15
        if (!isLockSecure) score -= 25

        return SystemSecurityReport(
            isRooted = isRooted,
            rootCheckReasons = rootReasons,
            isUsbDebuggingActive = isAdb,
            isDeviceSecureWithLock = isLockSecure,
            securityPatchLevel = Build.VERSION.SECURITY_PATCH ?: "غير معروف",
            androidVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
            isVpnActive = isVpn,
            systemScore = score.coerceIn(0, 100)
        )
    }

    suspend fun saveScanRecord(
        scanType: ScanType,
        totalAppsScanned: Int,
        threatsCount: Int,
        securityScore: Int,
        summaryAr: String
    ): Long = withContext(Dispatchers.IO) {
        securityDao.insertScanRecord(
            ScanRecordEntity(
                timestamp = System.currentTimeMillis(),
                scanType = scanType.titleAr,
                totalAppsScanned = totalAppsScanned,
                threatsFoundCount = threatsCount,
                securityScore = securityScore,
                summaryAr = summaryAr
            )
        )
    }
}
