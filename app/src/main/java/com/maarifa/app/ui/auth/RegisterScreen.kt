package com.maarifa.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.FormClass
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.di.AppContainer
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.Routes
import com.maarifa.app.ui.common.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(authViewModel: AuthViewModel, navController: NavController) {
    val container = maarifaContainer()
    val state by authViewModel.state.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Complete your profile", style = MaterialTheme.typography.headlineMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = role == UserRole.STUDENT, 
                onClick = { role = UserRole.STUDENT }, 
                label = { Text("I'm a Student") },
                enabled = !state.isSubmitting
            )
            FilterChip(
                selected = role == UserRole.TEACHER, 
                onClick = { role = UserRole.TEACHER }, 
                label = { Text("I'm a Teacher") },
                enabled = !state.isSubmitting
            )
        }

        OutlinedTextField(
            value = fullName, 
            onValueChange = { fullName = it }, 
            label = { Text("Full name") }, 
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            singleLine = true
        )

        OutlinedTextField(
            value = phoneNumber, 
            onValueChange = { phoneNumber = it }, 
            label = { Text("Phone number") }, 
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = !state.isSubmitting,
            singleLine = true
        )

        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email") }, 
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !state.isSubmitting,
            singleLine = true
        )

        OutlinedTextField(
            value = region, 
            onValueChange = { region = it }, 
            label = { Text("Region") }, 
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            singleLine = true
        )

        OutlinedTextField(
            value = schoolName,
            onValueChange = { schoolName = it },
            label = { Text("School name (optional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            singleLine = true
        )

        if (role == UserRole.STUDENT) {
            ExposedDropdownMenuBox(
                expanded = formMenuExpanded,
                onExpandedChange = { if (!state.isSubmitting) formMenuExpanded = !formMenuExpanded }
            ) {
                OutlinedTextField(
                    value = formClass.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Form / class") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = !state.isSubmitting
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

        GradientButton(
            text = "Continue",
            onClick = {
                val uid = container.authRepository.currentUserId ?: return@GradientButton
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
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { 
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) 
        }
        
        if (state.isSubmitting) {
            CircularProgressIndicator()
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
