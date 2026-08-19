package com.maarifa.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maarifa.app.ui.common.GradientButton

@Composable
fun OtpVerificationScreen(authViewModel: AuthViewModel, verificationId: String) {
    val state by authViewModel.state.collectAsState()
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Enter verification code", style = MaterialTheme.typography.headlineMedium)
        Text("We texted a 6-digit code to your phone.", style = MaterialTheme.typography.bodyMedium)
        
        OutlinedTextField(
            value = code,
            onValueChange = { input ->
                // Ruhusu namba tu na usizidi tarakimu 6
                if (input.length <= 6 && input.all { it.isDigit() }) {
                    code = input
                }
            },
            label = { Text("6-digit code") },
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        GradientButton(
            text = "Verify",
            onClick = { authViewModel.confirmOtp(verificationId, code) },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.length == 6 && !state.isSubmitting
        )

        state.errorMessage?.let { 
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) 
        }
        
        if (state.isSubmitting) {
            CircularProgressIndicator()
        }
    }
}
