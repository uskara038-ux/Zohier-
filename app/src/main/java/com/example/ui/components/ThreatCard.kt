package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetectedThreat
import com.example.data.model.ThreatSeverity
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDangerRed
import com.example.ui.theme.CyberDangerRedContainer
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSafeGreen
import com.example.ui.theme.CyberSafeGreenContainer
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarningAmber
import com.example.ui.theme.CyberWarningContainer

@Composable
fun ThreatCard(
    threat: DetectedThreat,
    onUninstall: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTrust: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val (badgeBg, badgeTextColor, severityLabel) = when {
        threat.isWhitelisted -> Triple(CyberSafeGreenContainer, CyberSafeGreen, "تطبيق موثوق به")
        threat.severity == ThreatSeverity.CRITICAL -> Triple(CyberDangerRedContainer, CyberDangerRed, "برمجية خبيثة حرجة")
        threat.severity == ThreatSeverity.HIGH_RISK -> Triple(CyberDangerRedContainer, CyberDangerRed, "تهديد عالي الخطورة")
        threat.severity == ThreatSeverity.MEDIUM_RISK -> Triple(CyberWarningContainer, CyberWarningAmber, "تطبيق مشبوه")
        else -> Triple(CyberSurfaceHighlight, CyberPrimary, "مخاطر منخفضة")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberSurface)
            .border(
                width = 1.dp,
                color = if (threat.isWhitelisted) CyberSafeGreen.copy(alpha = 0.3f) else CyberBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
            .testTag("threat_card_${threat.packageName}")
    ) {
        // Header: App name and severity badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (threat.isWhitelisted) Icons.Default.VerifiedUser else Icons.Default.Warning,
                        contentDescription = "Threat Icon",
                        tint = badgeTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = threat.appName,
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = threat.packageName,
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Severity Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = severityLabel,
                    color = badgeTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Threat Title & Description
        Text(
            text = threat.threatTitle,
            color = if (threat.isWhitelisted) CyberTextPrimary else badgeTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = threat.threatDescription,
            color = CyberTextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        // Reasons bullet list
        if (threat.reasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            threat.reasons.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = reason,
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Expandable permissions info
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberSurfaceVariant)
                    .padding(12.dp)
            ) {
                Text(
                    text = "الأذونات المصرحة (${threat.requestedPermissions.size}):",
                    color = CyberTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                threat.requestedPermissions.take(8).forEach { perm ->
                    val cleanPerm = perm.substringAfterLast(".")
                    Text(
                        text = "- $cleanPerm",
                        color = CyberTextMuted,
                        fontSize = 11.sp
                    )
                }
                if (threat.requestedPermissions.size > 8) {
                    Text(
                        text = "+ ${threat.requestedPermissions.size - 8} أذونات إضافية...",
                        color = CyberPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Toggle Expand
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Show less" else "Show more",
                    tint = CyberTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Remediation button (Delete / Settings)
            Button(
                onClick = onUninstall,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("btn_remediate_${threat.packageName}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberDangerRed,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Uninstall",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "إلغاء التثبيت",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Open app settings button
            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .height(42.dp)
                    .testTag("btn_settings_${threat.packageName}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyberPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Settings",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "الإعدادات", fontSize = 12.sp)
            }

            // Whitelist / Trust button
            OutlinedButton(
                onClick = onToggleTrust,
                modifier = Modifier
                    .height(42.dp)
                    .testTag("btn_trust_${threat.packageName}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (threat.isWhitelisted) CyberTextMuted else CyberSafeGreen
                )
            ) {
                Icon(
                    imageVector = if (threat.isWhitelisted) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
                    contentDescription = "Trust",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (threat.isWhitelisted) "إلغاء الثقة" else "ثقة",
                    fontSize = 12.sp
                )
            }
        }
    }
}
