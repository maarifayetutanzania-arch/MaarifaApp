package com.maarifa.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.data.model.Download
import com.maarifa.app.data.model.DownloadLocalStatus
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.common.EmptyState
import com.maarifa.app.ui.common.ErrorState
import com.maarifa.app.ui.common.LoadingState

@Composable
fun DownloadsScreen(onOpen: (String) -> Unit) {
    val container = maarifaContainer()
    val vm: DownloadsViewModel = viewModel(
        factory = SimpleViewModelFactory { DownloadsViewModel(container.downloadRepository, container.authRepository) }
    )
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp)) {
        Text("Downloads", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Stored on this device only, for offline reading inside Maarifa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp, bottom = 18.dp)
        )
        when {
            state.isLoading -> LoadingState()
            state.errorMessage != null -> ErrorState(state.errorMessage!!)
            state.downloads.isEmpty() -> EmptyState("Nothing downloaded yet. Open any material and tap Download.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.downloads, key = { it.downloadId }) { download ->
                    DownloadRow(download, onOpen = { onOpen(download.materialId) }, onRemove = { vm.remove(download.materialId) })
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(download: Download, onOpen: () -> Unit, onRemove: () -> Unit) {
    val (icon, tint, tintContainer, statusText) = when (download.localStatus) {
        DownloadLocalStatus.COMPLETE.name -> DownloadVisual(Icons.Default.CloudDone, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer, "Ready to read offline")
        DownloadLocalStatus.DOWNLOADING.name -> DownloadVisual(Icons.Default.CloudDownload, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer, "Downloading…")
        DownloadLocalStatus.FAILED.name -> DownloadVisual(Icons.Default.ErrorOutline, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer, "Download failed")
        else -> DownloadVisual(Icons.Default.CloudDownload, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant, "Queued")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(tintContainer), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(download.materialId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(statusText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (download.localStatus == DownloadLocalStatus.DOWNLOADING.name) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                }
            }
            if (download.localStatus == DownloadLocalStatus.COMPLETE.name) {
                TextButton(onClick = onOpen) { Text("Open") }
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private data class DownloadVisual(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: androidx.compose.ui.graphics.Color,
    val container: androidx.compose.ui.graphics.Color,
    val label: String
)
