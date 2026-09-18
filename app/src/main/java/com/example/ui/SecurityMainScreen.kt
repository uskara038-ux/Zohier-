package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScanState
import com.example.data.model.ScanType
import com.example.data.model.ThreatSeverity
import com.example.ui.components.CyberRadarScanner
import com.example.ui.components.ScanHistoryView
import com.example.ui.components.SecurityAdviceView
import com.example.ui.components.SecurityShieldHeader
import com.example.ui.components.SystemAuditCard
import com.example.ui.components.ThreatCard
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDangerRed
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSafeGreen
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarningAmber
import com.example.viewmodel.SecurityScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityMainScreen(
    viewModel: SecurityScannerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var threatFilter by remember { mutableStateOf<ThreatSeverity?>(null) }

    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val overviewState by viewModel.overviewState.collectAsStateWithLifecycle()
    val detectedThreats by viewModel.detectedThreats.collectAsStateWithLifecycle()
    val systemReport by viewModel.systemReport.collectAsStateWithLifecycle()
    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()

    val activeThreatsCount = detectedThreats.count { !it.isWhitelisted }

    // Provide Right-to-Left (RTL) Arabic layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(CyberBackground),
            containerColor = CyberBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CyberPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "App Shield",
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "كاشف الفيروسات",
                                    color = CyberTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "درع الحماية المتكامل للأندرويد",
                                    color = CyberTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberBackground,
                        titleContentColor = CyberTextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = CyberSurface,
                    contentColor = CyberTextPrimary
                ) {
                    // Tab 0: Home & Scan
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Scan"
                            )
                        },
                        label = { Text("الفحص", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberPrimary,
                            indicatorColor = CyberPrimary,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_scan")
                    )

                    // Tab 1: Threats
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            if (activeThreatsCount > 0) {
                                BadgedBox(badge = {
                                    Badge(
                                        containerColor = CyberDangerRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$activeThreatsCount")
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Threats"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Threats"
                                )
                            }
                        },
                        label = { Text("التهديدات", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberPrimary,
                            indicatorColor = CyberPrimary,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_threats")
                    )

                    // Tab 2: System Audit
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "System Audit"
                            )
                        },
                        label = { Text("أمان النظام", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberPrimary,
                            indicatorColor = CyberPrimary,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_system")
                    )

                    // Tab 3: History & Tips
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History & Tips"
                            )
                        },
                        label = { Text("السجل والنصائح", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberPrimary,
                            indicatorColor = CyberPrimary,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_history")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(CyberBackground)
            ) {
                when (selectedTab) {
                    0 -> ScannerHomeTab(
                        scanProgress = scanProgress,
                        overviewState = overviewState,
                        detectedThreats = detectedThreats,
                        systemReport = systemReport,
                        onStartScan = { viewModel.startScan(it) },
                        onStopScan = { viewModel.stopScan() },
                        onViewThreats = { selectedTab = 1 },
                        onViewSystemAudit = { selectedTab = 2 }
                    )
                    1 -> ThreatsTab(
                        threats = detectedThreats,
                        selectedFilter = threatFilter,
                        onFilterChange = { threatFilter = it },
                        onStartScan = {
                            selectedTab = 0
                            viewModel.startScan(ScanType.FULL)
                        },
                        onUninstall = { threat ->
                            viewModel.uninstallApp(context, threat.packageName)
                        },
                        onOpenSettings = { threat ->
                            viewModel.openAppDetails(context, threat.packageName)
                        },
                        onToggleTrust = { threat ->
                            if (threat.isWhitelisted) {
                                viewModel.unTrustApp(threat.packageName)
                            } else {
                                viewModel.trustApp(threat)
                            }
                        }
                    )
                    2 -> SystemAuditTab(
                        report = systemReport,
                        onRefresh = { viewModel.refreshSystemAudit() },
                        onOpenSettings = { viewModel.openSecuritySettings(context) }
                    )
                    3 -> HistoryAndTipsTab(
                        history = scanHistory,
                        onClearHistory = { viewModel.clearAllHistory() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerHomeTab(
    scanProgress: com.example.data.model.ScanProgress,
    overviewState: com.example.data.model.SecurityOverviewState,
    detectedThreats: List<com.example.data.model.DetectedThreat>,
    systemReport: com.example.data.model.SystemSecurityReport,
    onStartScan: (ScanType) -> Unit,
    onStopScan: () -> Unit,
    onViewThreats: () -> Unit,
    onViewSystemAudit: () -> Unit
) {
    val isScanning = scanProgress.state == ScanState.SCANNING
    val activeThreats = detectedThreats.filter { !it.isWhitelisted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isScanning) {
            item {
                CyberRadarScanner(
                    progress = scanProgress,
                    onStopScan = onStopScan
                )
            }
        } else {
            // Main Shield Card
            item {
                SecurityShieldHeader(
                    overviewState = overviewState,
                    onStartScan = onStartScan
                )
            }

            // If active threats exist, show quick alert card
            if (activeThreats.isNotEmpty()) {
                item {
                    ThreatAlertBanner(
                        threatCount = activeThreats.size,
                        onViewThreats = onViewThreats
                    )
                }
            }

            // System Quick Status Row
            item {
                SystemQuickCard(
                    report = systemReport,
                    onViewSystemAudit = onViewSystemAudit
                )
            }

            // Quick Security Tip Highlight
            item {
                QuickTipHighlight()
            }
        }
    }
}

@Composable
private fun ThreatAlertBanner(
    threatCount: Int,
    onViewThreats: () -> Unit
) {
    Card(
        onClick = onViewThreats,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CyberDangerRed.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("threat_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberDangerRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dangerous,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "تم اكتشاف $threatCount تهديد أمني بحاجة للمعالجة!",
                        color = CyberDangerRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "اضغط لمراجعة التطبيقات المشبوهة وحذفها",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Inspect",
                tint = CyberDangerRed
            )
        }
    }
}

@Composable
private fun SystemQuickCard(
    report: com.example.data.model.SystemSecurityReport,
    onViewSystemAudit: () -> Unit
) {
    Card(
        onClick = onViewSystemAudit,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Policy,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "حالة أمان النظام وكسر الحماية (Root)",
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (report.isRooted) "تحذير: صلاحيات الروت مفعّلة" else "النظام سليم ومحمي • تقييم الأمان ${report.systemScore}%",
                        color = if (report.isRooted) CyberDangerRed else CyberSafeGreen,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "التفاصيل",
                color = CyberPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickTipHighlight() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceHighlight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = CyberWarningAmber,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "نصيحة أمنية: لا تقم أبداً بتثبيت تطبيقات خارجية تطلب منك صلاحية إمكانية الوصول (Accessibility).",
                color = CyberTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ThreatsTab(
    threats: List<com.example.data.model.DetectedThreat>,
    selectedFilter: ThreatSeverity?,
    onFilterChange: (ThreatSeverity?) -> Unit,
    onStartScan: () -> Unit,
    onUninstall: (com.example.data.model.DetectedThreat) -> Unit,
    onOpenSettings: (com.example.data.model.DetectedThreat) -> Unit,
    onToggleTrust: (com.example.data.model.DetectedThreat) -> Unit
) {
    val filteredThreats = when (selectedFilter) {
        null -> threats
        else -> threats.filter { it.severity == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter chips row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("الكل (${threats.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberPrimary,
                        selectedLabelColor = Color.Black
                    )
                )

                FilterChip(
                    selected = selectedFilter == ThreatSeverity.HIGH_RISK,
                    onClick = { onFilterChange(ThreatSeverity.HIGH_RISK) },
                    label = { Text("خطير", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberDangerRed,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == ThreatSeverity.MEDIUM_RISK,
                    onClick = { onFilterChange(ThreatSeverity.MEDIUM_RISK) },
                    label = { Text("مشبوه", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberWarningAmber,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        if (filteredThreats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(CyberSafeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GppGood,
                                contentDescription = "All Clear",
                                tint = CyberSafeGreen,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد تهديدات نشطة!",
                            color = CyberTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "هاتفك محمي بالكامل وخالٍ من البرمجيات الضارة",
                            color = CyberTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredThreats, key = { it.packageName }) { threat ->
                ThreatCard(
                    threat = threat,
                    onUninstall = { onUninstall(threat) },
                    onOpenSettings = { onOpenSettings(threat) },
                    onToggleTrust = { onToggleTrust(threat) }
                )
            }
        }
    }
}

@Composable
private fun SystemAuditTab(
    report: com.example.data.model.SystemSecurityReport,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            SystemAuditCard(
                report = report,
                onRefresh = onRefresh,
                onOpenSecuritySettings = onOpenSettings
            )
        }
    }
}

@Composable
private fun HistoryAndTipsTab(
    history: List<com.example.data.db.ScanRecordEntity>,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScanHistoryView(
                historyList = history,
                onClearHistory = onClearHistory
            )
        }

        item {
            SecurityAdviceView()
        }
    }
}
