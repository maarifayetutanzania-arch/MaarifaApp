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
import androidx.compose.material3.CircularProgressIndicator
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

    val status = state.teacher?.verificationStatus?.uppercase() ?: "PENDING"

    // Auto-navigate pindi tu akaunti inapokuwa verified
    LaunchedEffect(status) {
        if (status == "VERIFIED") {
            onVerified()
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF1E7F55))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Inapakia taarifa za uhakiki...",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        } else {
            val (icon, iconTint, iconBg, title, body) = when (status) {
                "REJECTED" -> Tuple5(
                    Icons.Default.Cancel,
                    Color(0xFFC62828),
                    Color(0xFFFFEBEE),
                    "Application not approved",
                    state.teacher?.verificationNotes?.takeIf { it.isNotBlank() }
                        ?: "Your teacher application wasn't approved. Contact support for details."
                )
                "VERIFIED" -> Tuple5(
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = iconTint
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = body,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    if (status == "VERIFIED") {
                        Spacer(Modifier.height(24.dp))
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
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
