package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.maarifa.app.data.model.Download
import com.maarifa.app.data.model.DownloadLocalStatus
import com.maarifa.app.util.FileDownloadManager
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DownloadRepository(
    private val fileDownloadManager: FileDownloadManager,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = firestore.collection(FirestorePaths.DOWNLOADS)

    fun observeDownloads(userId: String): Flow<Resource<List<Download>>> = callbackFlow {
        val registration = collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load downloads"))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toObjects(Download::class.java).orEmpty()))
            }
        awaitClose { registration.remove() }
    }

    /** Downloads the file to app-private storage AND records it in Firestore so it shows
     * up across the user's devices and survives app reinstall as "needs re-download". */
    suspend fun downloadMaterial(
        userId: String,
        materialId: String,
        fileUrl: String,
        onProgress: (Float) -> Unit = {}
    ): Resource<Unit> {
        val docId = "${userId}_$materialId"
        return try {
            collection.document(docId).set(
                Download(downloadId = docId, userId = userId, materialId = materialId, localStatus = DownloadLocalStatus.DOWNLOADING.name)
            ).await()

            val result = fileDownloadManager.download(materialId, fileUrl, onProgress)
            if (result.isSuccess) {
                collection.document(docId).update(
                    mapOf(
                        "localStatus" to DownloadLocalStatus.COMPLETE.name,
                        "localFileName" to "$materialId.pdf",
                        "bytesTotal" to fileDownloadManager.sizeOnDiskBytes(materialId)
                    )
                ).await()
                Resource.Success(Unit)
            } else {
                collection.document(docId).update("localStatus", DownloadLocalStatus.FAILED.name).await()
                Resource.Error(result.exceptionOrNull()?.message ?: "Download failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Download failed", e)
        }
    }

    fun isDownloadedLocally(materialId: String) = fileDownloadManager.isDownloaded(materialId)
    fun localFile(materialId: String) = fileDownloadManager.localFile(materialId)

    suspend fun removeDownload(userId: String, materialId: String): Resource<Unit> = try {
        fileDownloadManager.delete(materialId)
        collection.document("${userId}_$materialId").delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not remove download", e)
    }

    suspend fun markOpened(userId: String, materialId: String) {
        try {
            collection.document("${userId}_$materialId")
                .update("lastOpenedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
                .await()
        } catch (_: Exception) { /* non-critical */ }
    }
}
