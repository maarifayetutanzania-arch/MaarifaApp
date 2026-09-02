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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.google.firebase.auth.FirebaseAuth
import com.maarifa.app.R
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.Routes

private enum class RegisterMethod { EMAIL, PHONE }

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    passedUid: String? = null,
    initialEmail: String = "",
    initialPhoneNumber: String = "",
    provider: AuthProvider = AuthProvider.EMAIL
) {
    val container = maarifaContainer()
    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current

    // Form States
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var registerMethod by remember { mutableStateOf(RegisterMethod.EMAIL) }
    
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var selectedRegion by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var selectedFormClass by remember { mutableStateOf("FORM_1") }

    var expandedRegionDropdown by remember { mutableStateOf(false) }
    var expandedFormDropdown by remember { mutableStateOf(false) }

    val regionsList = listOf(
        "Arusha", "Dar es Salaam", "Dodoma", "Geita", "Iringa", "Kagera", "Katavi",
        "Kigoma", "Kilimanjaro", "Lindi", "Manyara", "Mara", "Mbeya", "Morogoro",
        "Mtwara", "Mwanza", "Njombe", "Pemba", "Pwani", "Rukwa", "Ruvuma",
        "Shinyanga", "Simiyu", "Singida", "Songwe", "Tabora", "Tanga", "Zanzibar"
    )

    val formClassesList = listOf("FORM_1", "FORM_2", "FORM_3", "FORM_4", "FORM_5", "FORM_6")

    val primaryGreen = Color(0xFF1E7F55)
    val lightGreen = Color(0xFF34A853)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(primaryGreen, lightGreen)
    )

    // Google Auth Launcher
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { authViewModel.signInWithGoogleIdToken(it) }
        } catch (_: ApiException) { /* Cancelled */ }
    }

    // OTP Navigation Trigger
    LaunchedEffect(state.otpVerificationId) {
        state.otpVerificationId?.let { navController.navigate(Routes.otp(it)) }
    }

    // Direct Login or Navigate After Auth
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
                            text = "Tengeneza Akaunti",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. UCHAGUZI WA ROLE (Mwanafunzi / Mwalimu)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F8E9))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedRole == UserRole.STUDENT) primaryGreen else Color.Transparent)
                                    .clickable { selectedRole = UserRole.STUDENT }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Mwanafunzi",
                                    color = if (selectedRole == UserRole.STUDENT) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedRole == UserRole.TEACHER) primaryGreen else Color.Transparent)
                                    .clickable { selectedRole = UserRole.TEACHER }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Mwalimu",
                                    color = if (selectedRole == UserRole.TEACHER) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. UCHAGUZI WA METHOD (Email / Simu)
                        TabRow(
                            selectedTabIndex = registerMethod.ordinal,
                            containerColor = Color(0xFFFAFAFA),
                            indicator = { tabPositions ->
                                if (registerMethod.ordinal < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[registerMethod.ordinal]),
                                        color = primaryGreen
                                    )
                                }
                            }
                        ) {
                            Tab(
                                selected = registerMethod == RegisterMethod.EMAIL,
                                onClick = { registerMethod = RegisterMethod.EMAIL },
                                text = { Text("Barua Pepe", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = registerMethod == RegisterMethod.PHONE,
                                onClick = { registerMethod = RegisterMethod.PHONE },
                                text = { Text("Namba ya Simu", fontSize = 12.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // FORM INPUTS
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Full Name
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = { Text("Jina Kamili", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            if (registerMethod == RegisterMethod.EMAIL) {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("Barua Pepe (Email)", color = Color.LightGray) },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                            } else {
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    placeholder = { Text("+255 7XX XXX XXX", color = Color.LightGray) },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }

                            // Password Field
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("Neno la Siri (Password)", color = Color.LightGray) },
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            // Region Dropdown
                            ExposedDropdownMenuBox(
                                expanded = expandedRegionDropdown,
                                onExpandedChange = { expandedRegionDropdown = !expandedRegionDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedRegion,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Chagua Mkoa", color = Color.LightGray) },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegionDropdown) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedRegionDropdown,
                                    onDismissRequest = { expandedRegionDropdown = false }
                                ) {
                                    regionsList.forEach { reg ->
                                        DropdownMenuItem(
                                            text = { Text(reg) },
                                            onClick = {
                                                selectedRegion = reg
                                                expandedRegionDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Optional School Name
                            OutlinedTextField(
                                value = schoolName,
                                onValueChange = { schoolName = it },
                                placeholder = { Text("Shule (Hiyo si lazima)", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            // Form Class (Kama ni Student)
                            if (selectedRole == UserRole.STUDENT) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedFormDropdown,
                                    onExpandedChange = { expandedFormDropdown = !expandedFormDropdown }
                                ) {
                                    OutlinedTextField(
                                        value = selectedFormClass.replace("_", " "),
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = { Text("Darasa / Form", color = Color.LightGray) },
                                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFormDropdown) },
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedFormDropdown,
                                        onDismissRequest = { expandedFormDropdown = false }
                                    ) {
                                        formClassesList.forEach { form ->
                                            DropdownMenuItem(
                                                text = { Text(form.replace("_", " ")) },
                                                onClick = {
                                                    selectedFormClass = form
                                                    expandedFormDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SUBMIT BUTTON
                        Button(
                            onClick = {
                                if (registerMethod == RegisterMethod.PHONE) {
                                    context.findActivity()?.let { activity ->
                                        authViewModel.requestOtp(activity, phoneNumber.trim())
                                    }
                                } else {
                                    authViewModel.registerWithEmail(email.trim(), password)
                                }
                            },
                            enabled = fullName.isNotBlank() && selectedRegion.isNotBlank() && !state.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(buttonGradient, shape = RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Sajili Akaunti", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                            Text("  au sajili kwa  ", fontSize = 12.sp, color = Color.Gray)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // GOOGLE DIRECT REGISTRATION
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                                .clickable {
                                    context.findActivity()?.let { activity ->
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
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        state.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        if (state.isSubmitting) {
                            Spacer(modifier = Modifier.height(10.dp))
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryGreen)
                        }
                    }
                }
            }
        }
    }
}
