package com.maarifa.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.navigation.Routes

// Extension function ya kupata Activity kutoka kwenye Context
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun OtpVerificationScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    verificationId: String
) {
    val state by authViewModel.state.collectAsState()
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    val primaryGreen = Color(0xFF1E7F55)
    val lightGreen = Color(0xFF34A853)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(primaryGreen, lightGreen)
    )

    // Elekeza mtumiaji kwenye Home Screen akishafanikiwa kuingia
    LaunchedEffect(state.isSignedIn, state.profile, state.isSubmitting) {
        if (state.isSignedIn && !state.isSubmitting) {
            val profile = state.profile
            if (profile != null) {
                val dest = if (profile.roleEnum == UserRole.TEACHER) Routes.TEACHER_HOME else Routes.STUDENT_HOME
                navController.navigate(dest) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
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
                    .padding(horizontal = 24.dp, vertical = 20.dp),
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Thibitisha Namba ya Simu",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ingiza kodi ya tarakimu 6 tuliyokutumia kwa njia ya SMS.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            placeholder = { Text("123456", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    authViewModel.verifyOtp(verificationId, otpCode.trim())
                                }
                            },
                            enabled = otpCode.length == 6 && !state.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(buttonGradient, shape = RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Thibitisha", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        state.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        if (state.isSubmitting) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryGreen)
                        }
                    }
                }
            }
        }
    }
}
