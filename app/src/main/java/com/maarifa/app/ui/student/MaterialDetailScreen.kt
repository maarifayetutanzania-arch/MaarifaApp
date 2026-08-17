package com.maarifa.app.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.domain.AccessControlUseCase
import com.maarifa.app.ui.common.ErrorState
import com.maarifa.app.ui.common.GradientButton
import com.maarifa.app.ui.common.LoadingState

@Composable
fun MaterialDetailScreen(materialId: String, onOpenReader: (String) -> Unit, onNeedsSubscription: () -> Unit) {
    val container = maarifaContainer()
    val vm: MaterialDetailViewModel = viewModel(
        factory = SimpleViewModelFactory {
            MaterialDetailViewModel(materialId, container.materialRepository, container.subscriptionRepository, container.downloadRepository, container.authRepository)
        }
    )
    val state by vm.state.collectAsState()
    var wantsToRead by remember { mutableStateOf(false) }

    // Once a just-in-time download finishes, jump straight into the reader.
    LaunchedEffect(state.isDownloaded, state.downloadProgress) {
        if (state.isDownloaded && state.downloadProgress == null && wantsToRead) {
            wantsToRead = false
            onOpenReader(materialId)
        }
    }

    when {
        state.isLoading -> LoadingState()
        state.errorMessage != null && state.material == null -> ErrorState(state.errorMessage!!)
        state.material != null -> {
            val material = state.material!!
            Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(material.title, style = MaterialTheme.typography.headlineMedium)
                Text("${material.subject} • ${material.topic} • ${material.form.replace("_", " ")}", style = MaterialTheme.typography.bodyMedium)
                Text(material.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                Text("By ${material.teacherName}", style = MaterialTheme.typography.labelMedium)

                when (state.accessDecision) {
                    AccessControlUseCase.AccessDecision.Allowed -> {
                        if (state.downloadProgress != null) {
                            LinearProgressIndicator(progress = { state.downloadProgress!! }, modifier = Modifier.fillMaxWidth())
                        }
                        GradientButton(
                            text = if (state.isDownloaded) "Read" else "Download & Read",
                            onClick = {
                                wantsToRead = true
                                if (state.isDownloaded) onOpenReader(materialId) else vm.download()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    AccessControlUseCase.AccessDecision.SubscriptionRequired,
                    AccessControlUseCase.AccessDecision.SubscriptionExpired -> {
                        Text("A subscription is required to access this material.", color = MaterialTheme.colorScheme.error)
                        GradientButton(text = "View plans", onClick = onNeedsSubscription, modifier = Modifier.fillMaxWidth())
                    }
                    AccessControlUseCase.AccessDecision.MaterialNotApproved -> {
                        Text("This material is not currently available.", color = MaterialTheme.colorScheme.error)
                    }
                }

                OutlinedButton(onClick = vm::save, modifier = Modifier.fillMaxWidth()) { Text("Save for later") }
            }
        }
    }
}
