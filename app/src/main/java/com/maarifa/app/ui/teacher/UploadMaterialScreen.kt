package com.maarifa.app.ui.teacher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.data.model.FormClass
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.common.GradientButton
import com.maarifa.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMaterialScreen(onUploaded: () -> Unit) {
    val container = maarifaContainer()
    val vm: UploadMaterialViewModel = viewModel(
        factory = SimpleViewModelFactory {
            UploadMaterialViewModel(container.materialRepository, container.authRepository)
        }
    )
    val state by vm.state.collectAsState()
    var teacherName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        container.authRepository.currentUserId?.let { uid ->
            val res = container.authRepository.fetchUserProfile(uid)
            if (res is Resource.Success) teacherName = res.data.fullName
        }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var form by remember { mutableStateOf(FormClass.FORM_1) }
    var formMenuExpanded by remember { mutableStateOf(false) }
    var fileUri by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> fileUri = uri }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            title = ""
            description = ""
            subject = ""
            topic = ""
            fileUri = null
            vm.resetStatus()
            onUploaded()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Upload material",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject (e.g. Physics)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            label = { Text("Topic (e.g. Thermodynamics)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = formMenuExpanded,
            onExpandedChange = { formMenuExpanded = !formMenuExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = form.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Form / Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formMenuExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = formMenuExpanded,
                onDismissRequest = { formMenuExpanded = false }
            ) {
                FormClass.entries.forEach { fc ->
                    DropdownMenuItem(
                        text = { Text(fc.label) },
                        onClick = {
                            form = fc
                            formMenuExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { filePicker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = fileUri?.lastPathSegment ?: "Choose PDF file",
                maxLines = 1
            )
        }

        state.progress?.let { progress ->
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        GradientButton(
            text = if (state.isSubmitting) "Uploading..." else "Submit for review",
            onClick = {
                val selectedUri = fileUri ?: return@GradientButton
                vm.upload(
                    teacherName = teacherName.ifBlank { "Teacher" },
                    title = title,
                    description = description,
                    form = form.name,
                    subject = subject,
                    topic = topic,
                    fileUri = selectedUri
                )
            },
            enabled = title.isNotBlank() && subject.isNotBlank() && topic.isNotBlank() && fileUri != null && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
