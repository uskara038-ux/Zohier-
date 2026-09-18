package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanProgress
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDangerRed
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun CyberRadarScanner(
    progress: ScanProgress,
    onStopScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    // Radar rotation animation
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweep"
    )

    // Pulse ripple animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberSurface, CyberSurfaceVariant)
                )
            )
            .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Radar circle view
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background grid rings and sweeping radar beam
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .rotate(angle)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2

                // Outer radar ring
                drawCircle(
                    color = CyberPrimary.copy(alpha = 0.35f),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Middle ring
                drawCircle(
                    color = CyberPrimary.copy(alpha = 0.2f),
                    radius = radius * 0.65f,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Inner ring
                drawCircle(
                    color = CyberPrimary.copy(alpha = 0.15f),
                    radius = radius * 0.35f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Crosshairs
                drawLine(
                    color = CyberPrimary.copy(alpha = 0.2f),
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = CyberPrimary.copy(alpha = 0.2f),
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = 1.dp.toPx()
                )

                // Radar beam sweep arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            CyberPrimary.copy(alpha = 0.05f),
                            CyberPrimary.copy(alpha = 0.45f)
                        ),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 75f,
                    useCenter = true
                )
            }

            // Center glowing shield icon with pulse
            Box(
                modifier = Modifier
                    .size((64 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceHighlight)
                    .border(2.dp, CyberPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Radar Shield",
                    tint = CyberPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress percentage display
        val percentageText = (progress.progressPercent * 100).toInt().coerceIn(0, 100)
        Text(
            text = "$percentageText%",
            color = CyberPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("scan_percentage_text")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Current scanned app name
        Text(
            text = progress.currentAppName.ifEmpty { "جارٍ فحص الملفات والتطبيقات..." },
            color = CyberTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .testTag("scanned_app_name")
        )

        if (progress.currentPackageName.isNotEmpty()) {
            Text(
                text = progress.currentPackageName,
                color = CyberTextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Phase description text
        Text(
            text = progress.currentPhaseText,
            color = CyberTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Linear Progress bar
        LinearProgressIndicator(
            progress = { progress.progressPercent.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CyberPrimary,
            trackColor = CyberBorder,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scanned stats row
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "التطبيقات: ${progress.currentScannedCount} / ${progress.totalAppsToScan}",
                color = CyberTextSecondary,
                fontSize = 13.sp
            )

            if (progress.threatsFoundSoFar > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Threat alert",
                        tint = CyberDangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " تهديدات: ${progress.threatsFoundSoFar}",
                        color = CyberDangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Stop scan button
        ElevatedButton(
            onClick = onStopScan,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = CyberSurfaceHighlight,
                contentColor = CyberDangerRed
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("btn_stop_scan")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Stop",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = "إلغاء الفحص", fontWeight = FontWeight.Medium)
        }
    }
}
