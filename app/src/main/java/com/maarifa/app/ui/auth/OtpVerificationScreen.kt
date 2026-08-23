package com.maarifa.app.ui.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maarifa.app.ui.common.GradientButton

@Composable
fun OtpVerificationScreen(
    authViewModel: AuthViewModel,
    verificationId: String,
    phoneNumber: String = ""
) {
    val state by authViewModel.state.collectAsState()
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current

    val primaryGreen = Color(0xFF1E7F55)
    val lightGreen = Color(0xFF34A853)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )

    // Kama mfumo umesoma SMS yenyewe (Auto-Verified)
    LaunchedEffect(state.otpAutoCredential) {
        state.otpAutoCredential?.let { credential ->
            authViewModel.signInWithAutoCredential(credential)
        }
    }

    // Auto-submit code pale tu mtumiaji anapomaliza kuingiza namba 6
    LaunchedEffect(code) {
        if (code.length == 6 && !state.isSubmitting) {
            val activeVerificationId = state.otpVerificationId ?: verificationId
            authViewModel.confirmOtp(activeVerificationId, code)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundGradient)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF81C784), primaryGreen)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Thibitisha Namba",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )

                        Text(
                            text = if (phoneNumber.isNotBlank()) 
                                "Tumetuma msimbo wa tarakimu 6 kwenda $phoneNumber" 
                            else 
                                "Weka msimbo wa tarakimu 6 uliotumiwa kwenye simu yako.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    code = input
                                }
                            },
                            placeholder = { Text("000000", color = Color.LightGray) },
                            enabled = !state.isSubmitting,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = primaryGreen
                            )
                        )

                        GradientButton(
                            text = if (state.isSubmitting) "Inathibitisha..." else "Thibitisha",
                            onClick = {
                                val activeVerificationId = state.otpVerificationId ?: verificationId
                                authViewModel.confirmOtp(activeVerificationId, code)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = code.length == 6 && !state.isSubmitting
                        )

                        // Tuma Tena Code (Resend OTP Option)
                        TextButton(
                            onClick = {
                                if (phoneNumber.isNotBlank() && context is Activity) {
                                    authViewModel.requestOtp(context, phoneNumber)
                                }
                            },
                            enabled = !state.isSubmitting
                        ) {
                            Text(
                                text = "Hukuipata code? Tuma Tena",
                                color = primaryGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Error Display
                        state.errorMessage?.let { err ->
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                color = primaryGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
