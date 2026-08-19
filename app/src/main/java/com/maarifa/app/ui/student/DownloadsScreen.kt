package com.maarifa.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Maarifa Brand Colors
    val primaryGreen = Color(0xFF1E7F55)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Downloads",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = "Stored on this device only, for offline reading inside Maarifa.",
                fontSize = 14.sp,
                color = Color.DarkGray.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage!!)
                state.downloads.isEmpty() -> EmptyState("Nothing downloaded yet. Open any material and tap Download.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.downloads, key = { it.downloadId }) { download ->
                        DownloadRow(
                            download = download,
                            onOpen = { onOpen(download.materialId) },
                            onRemove = { vm.remove(download.materialId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(download: Download, onOpen: () -> Unit, onRemove: () -> Unit) {
    val primaryGreen = Color(0xFF1E7F55)
    val lightGreenBg = Color(0xFFE8F5E9)

    val visual = when (download.localStatus) {
        DownloadLocalStatus.COMPLETE.name -> DownloadVisual(
            icon = Icons.Default.CloudDone,
            tint = primaryGreen,
            container = lightGreenBg,
            label = "Ready to read offline"
        )
        DownloadLocalStatus.DOWNLOADING.name -> DownloadVisual(
            icon = Icons.Default.CloudDownload,
            tint = Color(0xFF0288D1),
            container = Color(0xFFE1F5FE),
            label = "Downloading…"
        )
        DownloadLocalStatus.FAILED.name -> DownloadVisual(
            icon = Icons.Default.ErrorOutline,
            tint = Color(0xFFD32F2F),
            container = Color(0xFFFFEBEE),
            label = "Download failed"
        )
        else -> DownloadVisual(
            icon = Icons.Default.CloudDownload,
            tint = Color.Gray,
            container = Color(0xFFF5F5F5),
            label = "Queued"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(visual.container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = download.materialId,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = visual.label,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                if (download.localStatus == DownloadLocalStatus.DOWNLOADING.name) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        color = Color(0xFF0288D1),
                        trackColor = Color(0xFFE1F5FE),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }

            if (download.localStatus == DownloadLocalStatus.COMPLETE.name) {
                TextButton(onClick = onOpen) {
                    Text(
                        text = "Open",
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Color.Gray
                )
            }
        }
    }
}

private data class DownloadVisual(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: androidx.compose.ui.graphics.Color,
    val container: androidx.compose.ui.graphics.Color,
    val label: String
)
