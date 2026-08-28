package com.maarifa.app.ui.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    passedUid: String? = null,
    initialEmail: String = "",
    initialPhoneNumber: String = "",
    provider: AuthProvider = AuthProvider.EMAIL
) {
    val state by viewModel.state.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var email by remember { mutableStateOf(initialEmail) }
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
    val disabledButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFA5D6A7), Color(0xFFC8E6C9))
    )

    val isFormValid = fullName.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            email.isNotBlank() &&
            selectedRegion.isNotBlank() &&
            !state.isSubmitting

    // Baada ya kusajili vizuri, piga onRegistrationSuccess
    LaunchedEffect(state.isSignedIn, state.profile, state.isSubmitting) {
        if (state.isSignedIn && state.profile != null && !state.isSubmitting) {
            onRegistrationSuccess()
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
                        // Avatar
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
                            text = "Kamilisha Usajili",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )

                        Text(
                            text = "Weka taarifa zako kuanza kutumia Maarifa App",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            // Full Name
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = { Text("Jina Kamili / Full Name", color = Color.LightGray) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )

                            // Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("Barua Pepe / Email", color = Color.LightGray) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )

                            // Phone
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                placeholder = { Text("Namba ya Simu / Phone Number", color = Color.LightGray) },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
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
                                    placeholder = { Text("Chagua Mkoa / Region", color = Color.LightGray) },
                                    leadingIcon = {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegionDropdown)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color(0xFFFAFAFA),
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = primaryGreen
                                    )
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

                            // School Name
                            OutlinedTextField(
                                value = schoolName,
                                onValueChange = { schoolName = it },
                                placeholder = { Text("Shule / School Name (Optional)", color = Color.LightGray) },
                                leadingIcon = {
                                    Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = primaryGreen
                                )
                            )

                            // Form Class Dropdown
                            ExposedDropdownMenuBox(
                                expanded = expandedFormDropdown,
                                onExpandedChange = { expandedFormDropdown = !expandedFormDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedFormClass.replace("_", " "),
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Darasa / Form Class", color = Color.LightGray) },
                                    leadingIcon = {
                                        Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray)
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFormDropdown)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color(0xFFFAFAFA),
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = primaryGreen
                                    )
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val activeUid = passedUid
                                    ?: FirebaseAuth.getInstance().currentUser?.uid

                                viewModel.completeRegistration(
                                    uidParam = activeUid,
                                    fullName = fullName.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    email = email.trim(),
                                    provider = provider,
                                    role = UserRole.STUDENT,
                                    region = selectedRegion.trim(),
                                    schoolName = schoolName.ifBlank { null },
                                    formClass = selectedFormClass
                                )
                            },
                            enabled = isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    brush = if (isFormValid) buttonGradient else disabledButtonGradient,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Maliza Usajili",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Error Message
                        state.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
