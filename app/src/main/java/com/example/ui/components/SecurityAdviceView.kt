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
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSafeGreen
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarningAmber

@Composable
fun SecurityAdviceView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CyberSurface)
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("security_advice_view")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Tips",
                    tint = CyberWarningAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "إرشادات الأمان والحماية الذكية",
                    color = CyberTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "أفضل الممارسات لتأمين هاتفك من الاختراق",
                    color = CyberTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tip 1: Avoid unverified APKs
        AdviceCardItem(
            icon = Icons.Default.DownloadDone,
            iconTint = CyberWarningAmber,
            title = "احذر من تثبيت ملفات APK الخارجية",
            description = "تحتوي ملفات APK المحملة عبر واتساب أو تليغرام أو مواقع غير رسمية غالباً على تروجان وبرمجيات تجسس خبيثة."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tip 2: OTP Protection
        AdviceCardItem(
            icon = Icons.Default.Key,
            iconTint = CyberPrimary,
            title = "لا تشارك رموز التحقق (OTP) مطلقاً",
            description = "رموز البنوك والرسائل النصية سرية تماماً. أي تطبيق أو مكالمة تطلب منك كود التحقق هي محاولة احتيال وسرقة حسابات."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tip 3: Accessibility & Overlay
        AdviceCardItem(
            icon = Icons.Default.Visibility,
            iconTint = CyberSafeGreen,
            title = "راقب صلاحيات إمكانية الوصول والنوافذ",
            description = "صلاحيات 'الرسم فوق التطبيقات' و'إمكانية الوصول' تمكّن البرمجيات الضارة من قراءة كلمات السر وتجاوز المصادقة."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tip 4: System Updates
        AdviceCardItem(
            icon = Icons.Default.SystemUpdate,
            iconTint = CyberPrimary,
            title = "حافظ على تحديث نظام التشغيل أندرويد",
            description = "تحتوي التحديثات الشهرية على ترقيعات أمنية تسد الثغرات المكتشفة التي قد تستغلها الفيروسات للوصول للروت."
        )
    }
}

@Composable
private fun AdviceCardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceHighlight)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(22.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = CyberTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = CyberTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
