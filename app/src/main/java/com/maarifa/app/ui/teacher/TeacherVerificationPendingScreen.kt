package com.maarifa.app.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.data.model.TeacherVerificationStatus
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.common.GradientButton

@Composable
fun TeacherVerificationPendingScreen(onVerified: () -> Unit) {
    val container = maarifaContainer()
    val vm: TeacherDashboardViewModel = viewModel(
        factory = SimpleViewModelFactory { TeacherDashboardViewModel(container.teacherRepository, container.authRepository) }
    )
    val state by vm.state.collectAsState()
    val status = state.teacher?.verificationStatus

    LaunchedEffect(status) {
        if (status == TeacherVerificationStatus.VERIFIED.name) onVerified()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.padding(bottom = 16.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            when (status) {
                TeacherVerificationStatus.REJECTED.name -> "Application not approved"
                else -> "Verification in progress"
            },
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            when (status) {
                TeacherVerificationStatus.REJECTED.name -> state.teacher?.verificationNotes?.takeIf { it.isNotBlank() }
                    ?: "Your teacher application wasn't approved. Contact support for details."
                else -> "Our team is reviewing your teacher application. You'll be notified as soon as you're approved — this usually doesn't take long."
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )
        if (status == TeacherVerificationStatus.VERIFIED.name) {
            GradientButton(text = "Continue to dashboard", onClick = onVerified, modifier = Modifier.fillMaxWidth())
        }
    }
}
