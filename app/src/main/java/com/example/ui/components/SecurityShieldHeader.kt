package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanType
import com.example.data.model.SecurityOverviewState
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDangerRed
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSafeGreen
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecurityShieldHeader(
    overviewState: SecurityOverviewState,
    onStartScan: (ScanType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isClean = overviewState.totalThreatsCount == 0
    val hasHighRisk = overviewState.highRiskCount > 0

    val statusColor by animateColorAsState(
        targetValue = when {
            hasHighRisk -> CyberDangerRed
            overviewState.totalThreatsCount > 0 -> CyberWarningAmber
            else -> CyberSafeGreen
        },
        animationSpec = tween(500),
        label = "StatusColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberSurfaceVariant,
                        CyberSurface
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(statusColor.copy(alpha = 0.5f), CyberBorder)
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Shield and circular score meter
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 10.dp.toPx()

                // Background track
                drawCircle(
                    color = CyberBorder,
                    radius = (size.minDimension - strokeWidth) / 2,
                    style = Stroke(width = strokeWidth)
                )

                // Foreground score arc
                val sweep = (overviewState.healthScore / 100f) * 360f
                drawArc(
                    color = statusColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        hasHighRisk -> Icons.Default.Dangerous
                        overviewState.totalThreatsCount > 0 -> Icons.Default.GppMaybe
                        else -> Icons.Default.GppGood
                    },
                    contentDescription = "Shield Status",
                    tint = statusColor,
                    modifier = Modifier.size(46.dp)
                )

                Text(
                    text = "${overviewState.healthScore}%",
                    color = CyberTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "مستوى الأمان",
                    color = CyberTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Title
        Text(
            text = overviewState.statusTitle,
            color = statusColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("status_title")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Status Subtitle
        Text(
            text = overviewState.statusSubtitle,
            color = CyberTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Stats grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSurfaceHighlight)
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                icon = Icons.Default.Apps,
                label = "التطبيقات",
                value = "${overviewState.totalAppsInstalled}"
            )
            StatItem(
                icon = Icons.Default.Shield,
                label = "التهديدات",
                value = "${overviewState.totalThreatsCount}",
                valueColor = if (overviewState.totalThreatsCount > 0) statusColor else CyberSafeGreen
            )
            StatItem(
                icon = Icons.Default.History,
                label = "آخر فحص",
                value = formatLastScan(overviewState.lastScanTimestamp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Scan Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onStartScan(ScanType.QUICK) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("btn_quick_scan"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberPrimary,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Quick Scan",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "فحص سريع",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            OutlinedButton(
                onClick = { onStartScan(ScanType.FULL) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("btn_deep_scan"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyberPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(CyberPrimary, CyberBorder))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Deep Scan",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "فحص شامل",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = CyberTextPrimary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CyberTextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            color = CyberTextMuted,
            fontSize = 11.sp
        )
    }
}

private fun formatLastScan(timestamp: Long?): String {
    if (timestamp == null) return "لم يتم بعد"
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "الآن"
        diff < 3600_000 -> "منذ ${diff / 60_000} د"
        diff < 86400_000 -> "اليوم"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}
