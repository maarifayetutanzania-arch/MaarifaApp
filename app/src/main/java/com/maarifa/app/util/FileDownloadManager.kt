package com.maarifa.app.util

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Downloads approved materials into app-private internal storage
 * (Context.filesDir/downloads) — NEVER external/shared storage, per PRD 8.3 & 9.
 * Files here are automatically sandboxed by Android and are wiped on uninstall;
 * they are excluded from backup/transfer via data_extraction_rules.xml.
 *
 * There is deliberately no share/export helper anywhere in this class or its callers —
 * the only way to view a downloaded file is through ReaderScreen, which renders pages
 * as bitmaps inside the app rather than handing the raw file to another app via an Intent.
 */
class FileDownloadManager(private val context: Context) {

    private val storage = FirebaseStorage.getInstance()

    private val downloadsDir: File
        get() = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }

    fun localFile(materialId: String): File = File(downloadsDir, "$materialId.pdf")

    fun isDownloaded(materialId: String): Boolean = localFile(materialId).exists()

    /**
     * Downloads [fileUrl] (a Firebase Storage download URL) into app-private storage.
     * Only ever call this for materials the user has already been authorized to view
     * while online (status == APPROVED and, for premium content, an active subscription) —
     * enforced by AccessControlUseCase before this is invoked.
     */
    suspend fun download(materialId: String, fileUrl: String, onProgress: (Float) -> Unit = {}): Result<File> {
        return try {
            val ref = storage.getReferenceFromUrl(fileUrl)
            val dest = localFile(materialId)
            val task = ref.getFile(dest)
            task.addOnProgressListener { snapshot ->
                if (snapshot.totalByteCount > 0) {
                    onProgress(snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount.toFloat())
                }
            }
            task.await()
            Result.success(dest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun delete(materialId: String) {
        localFile(materialId).delete()
    }

    fun sizeOnDiskBytes(materialId: String): Long =
        localFile(materialId).let { if (it.exists()) it.length() else 0L }
}
