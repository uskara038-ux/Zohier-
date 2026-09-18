package com.example.ui.components

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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SystemSecurityReport
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDangerRed
import com.example.ui.theme.CyberDangerRedContainer
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSafeGreen
import com.example.ui.theme.CyberSafeGreenContainer
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarningAmber
import com.example.ui.theme.CyberWarningContainer

@Composable
fun SystemAuditCard(
    report: SystemSecurityReport,
    onRefresh: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CyberSurface)
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("system_audit_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "System Security",
                        tint = CyberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تدقيق أمان نظام أندرويد",
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "كشف الثغرات وامتيازات الروت",
                        color = CyberTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.testTag("btn_refresh_system_audit")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = CyberPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Check 1: Root status
        AuditRow(
            title = "صلاحيات الجذر (Root)",
            subtitle = if (report.isRooted) "تم اكتشاف ملفات الروت (الجهاز معرض للاختراق)" else "لم يتم العثور على صلاحيات روت (آمن)",
            isSecure = !report.isRooted,
            icon = Icons.Default.BugReport
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Check 2: Lock Screen
        AuditRow(
            title = "حماية قفل الشاشة",
            subtitle = if (report.isDeviceSecureWithLock) "قفل الشاشة مفعّل ومحمي برمز أو بصمة" else "قفل الشاشة غير مفعّل (يوصى بتفعيله)",
            isSecure = report.isDeviceSecureWithLock,
            icon = Icons.Default.Lock
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Check 3: USB Debugging
        AuditRow(
            title = "تصحيح أخطاء USB (ADB)",
            subtitle = if (report.isUsbDebuggingActive) "وضع المطور وADB مفعّل (قد يسمح بنقل ملفات خارجي)" else "تصحيح USB معطل (الوضع الآمن)",
            isSecure = !report.isUsbDebuggingActive,
            isWarningOnly = true,
            icon = Icons.Default.Android
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Check 4: Network & VPN
        AuditRow(
            title = "نفق الشبكة المشفر (VPN)",
            subtitle = if (report.isVpnActive) "اتصال VPN نشط لحماية حركة البيانات" else "الاتصال المباشر بالإنترنت",
            isSecure = true,
            icon = Icons.Default.VpnLock
        )

        Spacer(modifier = Modifier.height(16.dp))

        // OS version & patch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurfaceHighlight)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = report.androidVersion,
                    color = CyberTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "مستوى التحديث الأمني: ${report.securityPatchLevel}",
                    color = CyberTextMuted,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSafeGreenContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "نظام رسمي",
                    color = CyberSafeGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open System Security settings button
        OutlinedButton(
            onClick = onOpenSecuritySettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("btn_open_security_settings"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CyberPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "فتح إعدادات حماية الهاتف",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AuditRow(
    title: String,
    subtitle: String,
    isSecure: Boolean,
    isWarningOnly: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val statusColor = when {
        isSecure -> CyberSafeGreen
        isWarningOnly -> CyberWarningAmber
        else -> CyberDangerRed
    }

    val statusBg = when {
        isSecure -> CyberSafeGreenContainer
        isWarningOnly -> CyberWarningContainer
        else -> CyberDangerRedContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceHighlight.copy(alpha = 0.5f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyberTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = CyberTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(statusBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSecure) Icons.Default.Check else if (isWarningOnly) Icons.Default.Warning else Icons.Default.Close,
                contentDescription = "Status",
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
