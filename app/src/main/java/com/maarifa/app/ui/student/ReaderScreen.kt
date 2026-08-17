package com.maarifa.app.ui.student

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maarifa.app.di.maarifaContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders every page of the locally-downloaded PDF to a Bitmap using Android's built-in
 * PdfRenderer, then draws a translucent "<name> · <phone>" watermark across each page.
 * Nothing here ever hands the raw file to another app (no Intent.ACTION_SEND / VIEW), so
 * there is no share sheet, no "open with", and no export path — satisfying PRD 8.3/12.
 * This is a best-effort deterrent, not DRM: screenshots and photos of the screen can't be
 * fully prevented by any app (see PRD 16 risks).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(materialId: String, onBack: () -> Unit) {
    val container = maarifaContainer()
    val context = LocalContext.current
    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var pfd by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    val watermarkText = remember {
        val uid = container.authRepository.currentUserId.orEmpty()
        "Maarifa 2026 · ${if (uid.length >= 6) uid.takeLast(6) else uid}"
    }

    LaunchedEffect(materialId) {
        container.authRepository.currentUserId?.let { uid ->
            container.engagementRepository.recordOpen(uid, materialId, teacherId = "")
            container.downloadRepository.markOpened(uid, materialId)
        }
        val file = container.downloadRepository.localFile(materialId)
        if (!file.exists()) {
            loadError = "This document hasn't been downloaded yet."
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pfd = descriptor
                val renderer = PdfRenderer(descriptor)
                val rendered = mutableListOf<Bitmap>()
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(AndroidColor.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    stampWatermark(bitmap, watermarkText)
                    rendered.add(bitmap)
                    page.close()
                }
                renderer.close()
                withContext(Dispatchers.Main) { pages = rendered }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { loadError = e.message ?: "Could not open document" }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { pfd?.close() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reader", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loadError != null -> Text(
                    loadError.orEmpty(),
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )
                pages.isEmpty() -> Text("Opening document…", modifier = Modifier.padding(24.dp))
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pages) { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun stampWatermark(bitmap: Bitmap, text: String) {
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = AndroidColor.argb(60, 20, 20, 20)
        textSize = bitmap.width / 18f
        isAntiAlias = true
    }
    canvas.save()
    canvas.rotate(-30f, bitmap.width / 2f, bitmap.height / 2f)
    var y = 0f
    while (y < bitmap.height * 1.5f) {
        canvas.drawText(text, -bitmap.width / 2f, y, paint)
        y += paint.textSize * 4
    }
    canvas.restore()
}
