package com.maarifa.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    onVerificationSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    var otpCode by remember { mutableStateOf("") }
    val state by authViewModel.state.collectAsState()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onVerificationSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Weka Namba ya Uhakiki (OTP)", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Ilipelekwa kwa: $phoneNumber")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            label = { Text("Msimbo wa OTP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = err, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val verificationId = state.otpVerificationId.orEmpty()
                authViewModel.confirmOtp(verificationId, otpCode)
            },
            enabled = !state.isSubmitting && otpCode.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Thibitisha OTP")
            }
        }
    }
}
