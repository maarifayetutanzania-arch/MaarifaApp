package com.maarifa.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.maarifa.app.R
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.Routes
import com.maarifa.app.ui.common.GradientButton

// 1. Enum re-ordered to match the visual TabRow layout (EMAIL, PHONE, GOOGLE)
private enum class LoginTab { EMAIL, PHONE, GOOGLE }

// Helper function to safely extract Activity from Context
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun LoginScreen(authViewModel: AuthViewModel, navController: NavController) {
    val container = maarifaContainer()
    val state by authViewModel.state.collectAsState()
    var tab by remember { mutableStateOf(LoginTab.EMAIL) }
    val context = LocalContext.current

    // Route forward once Firebase auth succeeds
    LaunchedEffect(state.isSignedIn, state.profile, state.isSubmitting) {
        if (state.isSignedIn && !state.isSubmitting) {
            val dest = if (state.profile == null) {
                Routes.REGISTER
            } else if (state.profile!!.roleEnum == com.maarifa.app.data.model.UserRole.TEACHER) {
                Routes.TEACHER_HOME
            } else {
                Routes.STUDENT_HOME
            }
            navController.navigate(dest) { 
                popUpTo(Routes.WELCOME) { inclusive = true } 
            }
        }
    }

    LaunchedEffect(state.otpVerificationId) {
        state.otpVerificationId?.let { navController.navigate(Routes.otp(it)) }
    }
    
    LaunchedEffect(state.otpAutoCredential) {
        state.otpAutoCredential?.let { authViewModel.signInWithAutoCredential(it) }
    }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { authViewModel.signInWithGoogleIdToken(it) }
        } catch (_: ApiException) { 
            /* user cancelled or failed - silent fallback */ 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sign in", style = MaterialTheme.typography.headlineMedium)

        TabRow(selectedTabIndex = tab.ordinal) {
            Tab(
                selected = tab == LoginTab.EMAIL, 
                onClick = { tab = LoginTab.EMAIL }, 
                text = { Text("Email") }
            )
            Tab(
                selected = tab == LoginTab.PHONE, 
                onClick = { tab = LoginTab.PHONE }, 
                text = { Text("Phone") }
            )
            Tab(
                selected = tab == LoginTab.GOOGLE, 
                onClick = { tab = LoginTab.GOOGLE }, 
                text = { Text("Google") }
            )
        }

        when (tab) {
            LoginTab.EMAIL -> EmailLoginForm(authViewModel)
            LoginTab.PHONE -> PhoneLoginForm(authViewModel)
            LoginTab.GOOGLE -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Continue with your Google account.", style = MaterialTheme.typography.bodyMedium)
                GradientButton(
                    text = "Continue with Google",
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            val client = container.authService.googleSignInClient(
                                activity, 
                                context.getString(R.string.google_web_client_id)
                            )
                            googleLauncher.launch(client.signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        
        if (state.isSubmitting) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun EmailLoginForm(authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        if (isRegisterMode) {
            GradientButton(
                text = "Create account",
                onClick = { authViewModel.registerWithEmail(email, password) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            GradientButton(
                text = "Sign in",
                onClick = { authViewModel.signInWithEmail(email, password) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedButton(
            onClick = { isRegisterMode = !isRegisterMode },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegisterMode) "I already have an account" else "New here? Create an account")
        }
    }
}

@Composable
private fun PhoneLoginForm(authViewModel: AuthViewModel) {
    var phone by remember { mutableStateOf("+255") }
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number") },
            placeholder = { Text("+255 7XX XXX XXX") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        GradientButton(
            text = "Send verification code",
            onClick = {
                context.findActivity()?.let { activity ->
                    authViewModel.requestOtp(activity, phone)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
