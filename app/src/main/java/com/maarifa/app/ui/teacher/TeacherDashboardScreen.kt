package com.maarifa.app.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherDashboardViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
            
            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage ?: "Kuna makosa yametokea",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                val status = state.teacher?.verificationStatus?.uppercase()
                // Inakubali VERIFIED au APPROVED (kuzuia migogoro ya namna admin inavyo-save)
                val isVerified = status == "VERIFIED" || status == "APPROVED"

                if (isVerified) {
                    // 1. Walimu walioidhinishwa wanaona Dashboard Halisi
                    TeacherMainDashboardContent(
                        teacher = state.teacher,
                        user = state.user,
                        onSignOut = { viewModel.signOut() }
                    )
                } else {
                    // 2. Walimu wanaosubiri wanaona "Verification in progress"
                    VerificationPendingScreen(
                        status = status ?: "PENDING"
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationPendingScreen(status: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFDCFCE7), shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⌛", fontSize = 28.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (status == "REJECTED") "Ombi Lako Limekataliwa" else "Verification in progress",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (status == "REJECTED") 
                    "Sio maombi yote yanayokubaliwa. Tafadhali wasiliana na usaidizi kwa maelezo zaidi." 
                    else "Our team is reviewing your teacher application. You'll be notified as soon as you're approved — this usually doesn't take long.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TeacherMainDashboardContent(
    teacher: com.maarifa.app.data.model.Teacher?,
    user: com.maarifa.app.data.model.User?,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Karibu, ${user?.fullName ?: "Mwalimu"}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Salio Lako: TZS ${teacher?.balance ?: 0.0}",
            fontSize = 16.sp,
            color = Color(0xFF10B981),
            fontWeight = FontWeight.SemiBold
        )
        
        // Hapa utaweka Grid ya Features za Upload, Payouts, View Uploaded Materials nk.
    }
}
