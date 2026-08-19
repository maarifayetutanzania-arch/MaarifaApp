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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.maarifa.app.R
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.FormClass
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.di.AppContainer
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.Routes

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(authViewModel: AuthViewModel, navController: NavController) {
    val container = maarifaContainer()
    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(container.authService.auth.currentUser?.email.orEmpty()) }
    var role by remember { mutableStateOf(UserRole.STUDENT) }
    var region by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var formClass by remember { mutableStateOf(FormClass.FORM_1) }
    var formMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.profile, state.isSubmitting) {
        if (state.profile != null && !state.isSubmitting) {
            val dest = if (state.profile!!.roleEnum == UserRole.TEACHER) {
                Routes.TEACHER_VERIFICATION_PENDING
            } else {
                Routes.STUDENT_HOME
            }
            navController.navigate(dest) {
                popUpTo(Routes.REGISTER) { inclusive = true }
            }
        }
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

    // Maarifa Brand Colors
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

                // Card Box Container
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
                        // User Avatar Icon
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
                            text = "Sign Up",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Role Selector Chips (Student / Teacher)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = role == UserRole.STUDENT,
                                onClick = { role = UserRole.STUDENT },
                                label = { Text("Student", fontWeight = FontWeight.Medium) },
                                enabled = !state.isSubmitting,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = role == UserRole.TEACHER,
                                onClick = { role = UserRole.TEACHER },
                                label = { Text("Teacher", fontWeight = FontWeight.Medium) },
                                enabled = !state.isSubmitting,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name Input
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Name", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = { Text("Enter your name", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                enabled = !state.isSubmitting,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number Input
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Phone Number", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                placeholder = { Text("+255 7XX XXX XXX", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                enabled = !state.isSubmitting,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Input
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("Enter your email", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                enabled = !state.isSubmitting,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Region Input
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Region", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = region,
                                onValueChange = { region = it },
                                placeholder = { Text("e.g. Dar es Salaam, Dodoma", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                enabled = !state.isSubmitting,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // School Name Input (Optional)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("School Name (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = schoolName,
                                onValueChange = { schoolName = it },
                                placeholder = { Text("Enter your school name", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                enabled = !state.isSubmitting,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )
                        }

                        // Form Class Dropdown (Only for Students)
                        if (role == UserRole.STUDENT) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Form / Class", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                ExposedDropdownMenuBox(
                                    expanded = formMenuExpanded,
                                    onExpandedChange = { if (!state.isSubmitting) formMenuExpanded = !formMenuExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = formClass.label,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formMenuExpanded) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        enabled = !state.isSubmitting,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = Color(0xFFFAFAFA),
                                            focusedContainerColor = Color.White,
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedBorderColor = primaryGreen
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = formMenuExpanded,
                                        onDismissRequest = { formMenuExpanded = false }
                                    ) {
                                        FormClass.entries.forEach { fc ->
                                            DropdownMenuItem(
                                                text = { Text(fc.label) },
                                                onClick = {
                                                    formClass = fc
                                                    formMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val uid = container.authRepository.currentUserId ?: return@Button
                                val provider = detectProvider(container)
                                authViewModel.completeRegistration(
                                    uid = uid,
                                    fullName = fullName.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    email = email.trim(),
                                    provider = provider,
                                    role = role,
                                    region = region.trim(),
                                    schoolName = schoolName.ifBlank { null },
                                    formClass = formClass.name
                                )
                            },
                            enabled = fullName.isNotBlank() && phoneNumber.isNotBlank() && region.isNotBlank() && !state.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(buttonGradient, shape = RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Or Continue With Divider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                            Text(
                                text = "  or continue with  ",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Only Button (NO FACEBOOK)
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

                        // Footer Link
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Already have an account?",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Log In >",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                modifier = Modifier.clickable {
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.REGISTER) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Error Message Display
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

private fun detectProvider(container: AppContainer): AuthProvider {
    val providerId = container.authService.auth.currentUser?.providerData
        ?.map { it.providerId }
        ?.firstOrNull { it != "firebase" }
        ?: "password"

    return when {
        providerId.contains("google") -> AuthProvider.GOOGLE
        providerId.contains("phone") -> AuthProvider.PHONE
        else -> AuthProvider.EMAIL
    }
}
