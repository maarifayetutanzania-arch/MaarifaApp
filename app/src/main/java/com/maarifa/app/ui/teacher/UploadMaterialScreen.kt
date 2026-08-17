package com.maarifa.app.ui.teacher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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

@Composable
fun UploadMaterialScreen(onUploaded: () -> Unit) {
    val container = maarifaContainer()
    val vm: UploadMaterialViewModel = viewModel(
        factory = SimpleViewModelFactory { UploadMaterialViewModel(container.materialRepository, container.authRepository) }
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

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> fileUri = uri }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) onUploaded()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Upload material", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth())

        Column {
            Text("Form / class", style = MaterialTheme.typography.labelMedium)
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clickable { formMenuExpanded = true }
                    .padding(vertical = 12.dp)
            ) {
                Text(form.label, style = MaterialTheme.typography.bodyLarge)
                DropdownMenu(expanded = formMenuExpanded, onDismissRequest = { formMenuExpanded = false }) {
                    FormClass.entries.forEach { fc ->
                        DropdownMenuItem(text = { Text(fc.label) }, onClick = { form = fc; formMenuExpanded = false })
                    }
                }
            }
            HorizontalDivider()
        }

        OutlinedButton(onClick = { filePicker.launch("application/pdf") }, modifier = Modifier.fillMaxWidth()) {
            Text(fileUri?.lastPathSegment ?: "Choose PDF file")
        }

        if (state.progress != null) {
            LinearProgressIndicator(progress = { state.progress!! }, modifier = Modifier.fillMaxWidth())
        }

        GradientButton(
            text = "Submit for review",
            onClick = {
                vm.upload(
                    teacherName = teacherName,
                    title = title, description = description, form = form.name, subject = subject, topic = topic,
                    fileUri = fileUri!!
                )
            },
            enabled = title.isNotBlank() && subject.isNotBlank() && topic.isNotBlank() && fileUri != null && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
