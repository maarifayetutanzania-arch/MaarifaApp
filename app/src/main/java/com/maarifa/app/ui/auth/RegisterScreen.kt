package com.maarifa.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    initialEmail: String = "",
    initialPhoneNumber: String = "",
    provider: AuthProvider = AuthProvider.EMAIL,
    onRegistrationSuccess: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var email by remember { mutableStateOf(initialEmail) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
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

    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC))
    )

    LaunchedEffect(uiState.isSignedIn, uiState.profile) {
        if (uiState.isSignedIn && uiState.profile != null) {
            onRegistrationSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kamilisha Usajili",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Weka taarifa zako ili kuanza kutumia Maarifa App",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Error Message Display
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Jina Buplem / Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Barua Pepe / Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Phone Number Field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Namba ya Simu / Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Region Dropdown Select
        ExposedDropdownMenuBox(
            expanded = expandedRegionDropdown,
            onExpandedChange = { expandedRegionDropdown = !expandedRegionDropdown },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = selectedRegion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Chagua Mkoa / Region") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegionDropdown) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expandedRegionDropdown,
                onDismissRequest = { expandedRegionDropdown = false }
            ) {
                regionsList.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(region) },
                        onClick = {
                            selectedRegion = region
                            expandedRegionDropdown = false
                        }
                    )
                }
            }
        }

        // School Name Field
        OutlinedTextField(
            value = schoolName,
            onValueChange = { schoolName = it },
            label = { Text("Jina la Shule / School Name (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Form Class Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedFormDropdown,
            onExpandedChange = { expandedFormDropdown = !expandedFormDropdown },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            OutlinedTextField(
                value = selectedFormClass.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Darasa / Form Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFormDropdown) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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

        // Submit Registration Button
        Button(
            onClick = {
                viewModel.completeRegistration(
                    uidParam = null, // Inachukua current authenticated UID otomatiki
                    fullName = fullName.trim(),
                    phoneNumber = phoneNumber.trim(),
                    email = email.trim(),
                    provider = provider,
                    role = selectedRole,
                    region = selectedRegion.trim(),
                    schoolName = schoolName.ifBlank { null },
                    formClass = selectedFormClass
                )
            },
            enabled = fullName.isNotBlank() &&
                    phoneNumber.isNotBlank() &&
                    email.isNotBlank() &&
                    selectedRegion.isNotBlank() &&
                    !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Maliza Usajili",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
