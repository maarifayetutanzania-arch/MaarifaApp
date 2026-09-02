package com.maarifa.app.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.maarifa.app.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var formClass by remember { mutableStateOf("Form I") }

    var roleExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(state.otpVerificationId) {
        if (!state.otpVerificationId.isNullOrBlank()) {
            onNavigateToOtp(phoneNumber)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tengeneza Akaunti MPYA", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Jina Bufe (Full Name)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Barua Pepe (Email)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Namba ya Simu (+255...)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nenosiri (Password)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Mkoa (Region)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = schoolName,
            onValueChange = { schoolName = it },
            label = { Text("Jina la Shule (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role Dropdown
        ExposedDropdownMenuBox(
            expanded = roleExpanded,
            onExpandedChange = { roleExpanded = !roleExpanded }
        ) {
            OutlinedTextField(
                value = selectedRole.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Aina ya Akaunti") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = roleExpanded,
                onDismissRequest = { roleExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Mwanafunzi (STUDENT)") },
                    onClick = {
                        selectedRole = UserRole.STUDENT
                        roleExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Mwalimu (TEACHER)") },
                    onClick = {
                        selectedRole = UserRole.TEACHER
                        roleExpanded = false
                    }
                )
            }
        }

        if (selectedRole == UserRole.STUDENT) {
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = classExpanded,
                onExpandedChange = { classExpanded = !classExpanded }
            ) {
                OutlinedTextField(
                    value = formClass,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kidato (Form)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = classExpanded,
                    onDismissRequest = { classExpanded = false }
                ) {
                    listOf("Form I", "Form II", "Form III", "Form IV", "Form V", "Form VI").forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f) },
                            onClick = {
                                formClass = f
                                classExpanded = false
                            }
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = err, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    authViewModel.registerWithEmail(email, password)
                } else if (phoneNumber.isNotBlank() && context is Activity) {
                    authViewModel.requestOtp(context, phoneNumber)
                }
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Sajili Akaunti")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Tayari una akaunti? Ingia hapa")
        }
    }
}

