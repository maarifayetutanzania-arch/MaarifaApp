package com.maarifa.app.ui.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.data.model.Material
import com.maarifa.app.data.model.MaterialStatus
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.common.EmptyState
import com.maarifa.app.ui.common.ErrorState
import com.maarifa.app.ui.common.LoadingState
import com.maarifa.app.ui.common.StatusPill

@Composable
fun TeacherMaterialsScreen() {
    val container = maarifaContainer()
    val vm: TeacherMaterialsViewModel = viewModel(
        factory = SimpleViewModelFactory { TeacherMaterialsViewModel(container.materialRepository, container.authRepository) }
    )
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp)) {
        Text("My materials", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Track approval status and reach for everything you've uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp, bottom = 18.dp)
        )
        when {
            state.isLoading -> LoadingState()
            state.errorMessage != null -> ErrorState(state.errorMessage!!)
            state.materials.isEmpty() -> EmptyState("You haven't uploaded anything yet.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.materials, key = { it.materialId }) { TeacherMaterialRow(it) }
            }
        }
    }
}

@Composable
private fun TeacherMaterialRow(material: Material) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(material.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(end = 8.dp))
                val (bg, fg, label) = when (material.status) {
                    MaterialStatus.APPROVED.name -> Triple(Color(0xFFEBF2EC), Color(0xFF1F6B45), "Approved")
                    MaterialStatus.REJECTED.name -> Triple(Color(0xFFF7E5E1), Color(0xFFB6503F), "Rejected")
                    else -> Triple(Color(0xFFF8F1E2), Color(0xFF9A7530), "Pending review")
                }
                StatusPill(label, bg, fg)
            }
            Text(
                "${material.subject} • ${material.topic}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("${material.uniqueReaderCount} readers", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${material.totalReadCount} reads", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (material.status == MaterialStatus.REJECTED.name && material.rejectionReason.isNotBlank()) {
                Text(material.rejectionReason, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
