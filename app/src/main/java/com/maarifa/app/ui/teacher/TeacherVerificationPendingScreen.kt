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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.common.GradientButton

private data class VerificationUiConfig(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val title: String,
    val body: String
)

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

    // Auto-navigate when account becomes verified
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
            val uiConfig = when (status) {
                "REJECTED" -> VerificationUiConfig(
                    icon = Icons.Default.Cancel,
                    iconTint = Color(0xFFC62828),
                    iconBackground = Color(0xFFFFEBEE),
                    title = "Application not approved",
                    body = state.teacher?.verificationNotes?.takeIf { it.isNotBlank() }
                        ?: "Your teacher application wasn't approved. Contact support for details."
                )
                "VERIFIED" -> VerificationUiConfig(
                    icon = Icons.Default.CheckCircle,
                    iconTint = Color(0xFF1E7F55),
                    iconBackground = Color(0xFFE8F5E9),
                    title = "Application Approved!",
                    body = "Your account is verified. You can now access your dashboard and publish materials."
                )
                else -> VerificationUiConfig(
                    icon = Icons.Default.HourglassTop,
                    iconTint = Color(0xFF1E7F55),
                    iconBackground = Color(0xFFE8F5E9),
                    title = "Verification in progress",
                    body = "Our team is reviewing your teacher application. You'll be notified as soon as you're approved — this usually doesn't take long."
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
                            .background(uiConfig.iconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = uiConfig.icon,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = uiConfig.iconTint
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = uiConfig.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = uiConfig.body,
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
