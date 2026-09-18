package com.example.data.model

enum class ThreatSeverity(val titleAr: String, val level: Int) {
    SAFE("آمن", 0),
    LOW_RISK("مخاطر منخفضة", 1),
    MEDIUM_RISK("تطبيق مشبوه", 2),
    HIGH_RISK("تهديد خطير", 3),
    CRITICAL("برمجية خبيثة حرجة", 4)
}

enum class ThreatType {
    ADWARE_TRACKER,
    SUSPICIOUS_PERMISSIONS,
    OVERLAY_HIJACKING,
    SMS_INTERCEPTOR,
    ACCESSIBILITY_ABUSE,
    SIDELOADED_UNKNOWN,
    DEBUGGABLE_FLAG,
    ROOT_SYSTEM_VULNERABILITY,
    UNKNOWN_SOURCE_INSTALLER
}

data class DetectedThreat(
    val id: String,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val severity: ThreatSeverity,
    val threatType: ThreatType,
    val threatTitle: String,
    val threatDescription: String,
    val reasons: List<String>,
    val requestedPermissions: List<String>,
    val isSystemApp: Boolean,
    val installerPackage: String?,
    val isWhitelisted: Boolean = false,
    val detectedAt: Long = System.currentTimeMillis()
)

data class SystemSecurityReport(
    val isRooted: Boolean = false,
    val rootCheckReasons: List<String> = emptyList(),
    val isUsbDebuggingActive: Boolean = false,
    val isDeviceSecureWithLock: Boolean = true,
    val securityPatchLevel: String = "",
    val androidVersion: String = "",
    val isVpnActive: Boolean = false,
    val systemScore: Int = 100
)

enum class ScanType(val titleAr: String) {
    QUICK("فحص سريع"),
    FULL("فحص شامل"),
    SYSTEM("فحص أمان النظام")
}

enum class ScanState {
    IDLE,
    SCANNING,
    COMPLETED,
    STOPPED
}

data class ScanProgress(
    val state: ScanState = ScanState.IDLE,
    val scanType: ScanType = ScanType.QUICK,
    val progressPercent: Float = 0f,
    val currentScannedCount: Int = 0,
    val totalAppsToScan: Int = 0,
    val currentAppName: String = "",
    val currentPackageName: String = "",
    val currentPhaseText: String = "",
    val threatsFoundSoFar: Int = 0
)

data class SecurityOverviewState(
    val lastScanTimestamp: Long? = null,
    val healthScore: Int = 100, // 0 - 100
    val totalAppsInstalled: Int = 0,
    val totalThreatsCount: Int = 0,
    val highRiskCount: Int = 0,
    val mediumRiskCount: Int = 0,
    val statusTitle: String = "الهاتف محمي وآمن",
    val statusSubtitle: String = "لم يتم العثور على تهديدات نشطة"
)
