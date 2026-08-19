package com.maarifa.app.ui.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.data.model.TeacherVerificationStatus
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.common.GradientButton

@Composable
fun TeacherVerificationPendingScreen(onVerified: () -> Unit) {
    val container = maarifaContainer()
    val vm: TeacherDashboardViewModel = viewModel(
        factory = SimpleViewModelFactory {
            TeacherDashboardViewModel(container.teacherRepository, container.authRepository)
        }
    )
    val state by vm.state.collectAsState()
    val status = state.teacher?.verificationStatus

    LaunchedEffect(status) {
        if (status == TeacherVerificationStatus.VERIFIED.name) onVerified()
    }

    // Maarifa Brand Design Specs
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )

    val (icon, iconTint, iconBgColor, titleText, bodyText) = when (status) {
        TeacherVerificationStatus.REJECTED.name -> Tuple5(
            Icons.Default.Cancel,
            Color(0xFFC62828),
            Color(0xFFFFEBEE),
            "Application not approved",
            state.teacher?.verificationNotes?.takeIf { it.isNotBlank() }
                ?: "Your teacher application wasn't approved. Contact support for details."
        )
        TeacherVerificationStatus.VERIFIED.name -> Tuple5(
            Icons.Default.CheckCircle,
            Color(0xFF1E7F55),
            Color(0xFFE8F5E9),
            "Application Approved!",
            "Your account is verified. You can now access your dashboard and publish materials."
        )
        else -> Tuple5(
            Icons.Default.HourglassTop,
            Color(0xFF1E7F55),
            Color(0xFFE8F5E9),
            "Verification in progress",
            "Our team is reviewing your teacher application. You'll be notified as soon as you're approved — this usually doesn't take long."
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = iconTint
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = titleText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = bodyText,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                if (status == TeacherVerificationStatus.VERIFIED.name) {
                    Spacer(modifier = Modifier.height(24.dp))
                    GradientButton(
                        text = "Continue to dashboard",
                        onClick = onVerified,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Internal data holder for UI property mapping
private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
