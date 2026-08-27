package com.maarifa.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.maarifa.app.R
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.Routes

private enum class LoginTab { EMAIL, PHONE }

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

    // Navigation and Session Logic
    LaunchedEffect(state.isSignedIn, state.profile, state.isSubmitting) {
        if (state.isSignedIn && !state.isSubmitting) {
            val dest = if (state.profile == null) {
                Routes.REGISTER
            } else if (state.profile!!.roleEnum == UserRole.TEACHER) {
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
            /* cancelled */
        }
    }

    // Brand Colors
    val primaryGreen = Color(0xFF1E7F55)
    val lightGreen = Color(0xFF34A853)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(primaryGreen, lightGreen)
    )

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
                Spacer(modifier = Modifier.height(10.dp))

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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Ingia (Login)",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TabRow(
                            selectedTabIndex = tab.ordinal,
                            containerColor = Color(0xFFF1F8E9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            indicator = { tabPositions ->
                                if (tab.ordinal < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[tab.ordinal]),
                                        color = primaryGreen
                                    )
                                }
                            }
                        ) {
                            Tab(
                                selected = tab == LoginTab.EMAIL,
                                onClick = { tab = LoginTab.EMAIL },
                                text = {
                                    Text(
                                        "Email",
                                        fontWeight = if (tab == LoginTab.EMAIL) FontWeight.Bold else FontWeight.Normal,
                                        color = if (tab == LoginTab.EMAIL) primaryGreen else Color.Gray
                                    )
                                }
                            )
                            Tab(
                                selected = tab == LoginTab.PHONE,
                                onClick = { tab = LoginTab.PHONE },
                                text = {
                                    Text(
                                        "Simu",
                                        fontWeight = if (tab == LoginTab.PHONE) FontWeight.Bold else FontWeight.Normal,
                                        color = if (tab == LoginTab.PHONE) primaryGreen else Color.Gray
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        when (tab) {
                            LoginTab.EMAIL -> EmailLoginForm(authViewModel, buttonGradient, primaryGreen)
                            LoginTab.PHONE -> PhoneLoginForm(authViewModel, buttonGradient)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                            Text(
                                text = "  au ingia kwa  ",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                                .clickable {
                                    val activity = context.findActivity()
                                    if (activity != null) {
                                        val client = container.authService.googleSignInClient(
                                            activity,
                                            context.getString(R.string.google_web_client_id)
                                        )
                                        googleLauncher.launch(client.signInIntent)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Sign In",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Huna akaunti bado?",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tengeneza Akaunti >",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                modifier = Modifier.clickable {
                                    // Imetumika Routes.REGISTER kutoka kwenye Routes.kt yako
                                    navController.navigate(Routes.REGISTER)
                                }
                            )
                        }

                        state.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (state.isSubmitting) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = primaryGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailLoginForm(
    authViewModel: AuthViewModel,
    buttonGradient: Brush,
    primaryGreen: Color
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column {
            Text("Barua Pepe", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Ingiza email yako", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFFAFAFA),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = primaryGreen
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Neno la Siri", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                Text(
                    "Umesahau?",
                    fontSize = 11.sp,
                    color = primaryGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* Reset password flow */ }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Ingiza neno la siri", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFFAFAFA),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = primaryGreen
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = { authViewModel.signInWithEmail(email.trim(), password) },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(buttonGradient, shape = RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Ingia", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun PhoneLoginForm(
    authViewModel: AuthViewModel,
    buttonGradient: Brush
) {
    var phone by remember { mutableStateOf("+255") }
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column {
            Text("Namba ya Simu", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("+255 7XX XXX XXX", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFFAFAFA),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                context.findActivity()?.let { activity ->
                    authViewModel.requestOtp(activity, phone.trim())
                }
            },
            enabled = phone.length >= 10,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(buttonGradient, shape = RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Tuma Namba ya Uhakiki", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
