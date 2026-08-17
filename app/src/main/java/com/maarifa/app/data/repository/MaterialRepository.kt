package com.maarifa.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.maarifa.app.data.model.Material
import com.maarifa.app.data.model.MaterialStatus
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import com.maarifa.app.util.StorageePaths
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MaterialRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val collection get() = firestore.collection(FirestorePaths.MATERIALS)

    /** Real-time library feed — approved content only (PRD 8.2), optionally filtered. */
    fun observeLibrary(form: String? = null, subject: String? = null): Flow<Resource<List<Material>>> = callbackFlow {
        var query: Query = collection.whereEqualTo("status", MaterialStatus.APPROVED.name)
        if (form != null) query = query.whereEqualTo("form", form)
        if (subject != null) query = query.whereEqualTo("subject", subject)
        query = query.orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load library"))
                return@addSnapshotListener
            }
            val items = snapshot?.toObjects(Material::class.java).orEmpty()
            trySend(Resource.Success(items))
        }
        awaitClose { registration.remove() }
    }

    /** Client-side substring filter over title/topic/subject/keyword, applied on top of
     * observeLibrary's snapshot so search is instant and doesn't need a search backend. */
    fun filterBySearch(materials: List<Material>, query: String): List<Material> {
        if (query.isBlank()) return materials
        val q = query.trim().lowercase()
        return materials.filter {
            it.title.lowercase().contains(q) ||
                it.topic.lowercase().contains(q) ||
                it.subject.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
        }
    }

    suspend fun getMaterial(materialId: String): Resource<Material> = try {
        val snap = collection.document(materialId).get().await()
        val material = snap.toObject(Material::class.java)
        if (material != null) Resource.Success(material) else Resource.Error("Material not found")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load material", e)
    }

    fun observeTeacherMaterials(teacherId: String): Flow<Resource<List<Material>>> = callbackFlow {
        val registration = collection
            .whereEqualTo("teacherId", teacherId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load your materials"))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toObjects(Material::class.java).orEmpty()))
            }
        awaitClose { registration.remove() }
    }

    /**
     * PRD 8.6: upload requires title/description/form/subject/topic/file, and always
     * lands in PENDING_REVIEW — teachers cannot self-publish. Admin approval happens in
     * the separate web dashboard, writing status back to APPROVED/REJECTED.
     */
    suspend fun uploadMaterial(
        teacherId: String,
        teacherName: String,
        title: String,
        description: String,
        form: String,
        subject: String,
        topic: String,
        fileUri: Uri,
        fileType: String,
        onProgress: (Float) -> Unit = {}
    ): Resource<String> = try {
        val materialId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("${StorageePaths.MATERIALS_ROOT}/$teacherId/$materialId.pdf")

        val uploadTask = storageRef.putFile(fileUri)
        uploadTask.addOnProgressListener { snap ->
            if (snap.totalByteCount > 0) onProgress(snap.bytesTransferred.toFloat() / snap.totalByteCount.toFloat())
        }
        uploadTask.await()
        val downloadUrl = storageRef.downloadUrl.await().toString()

        val material = Material(
            materialId = materialId,
            teacherId = teacherId,
            teacherName = teacherName,
            title = title,
            description = description,
            form = form,
            subject = subject,
            topic = topic,
            fileUrl = downloadUrl,
            fileType = fileType,
            status = MaterialStatus.PENDING_REVIEW.name
        )
        collection.document(materialId).set(material).await()
        Resource.Success(materialId)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Upload failed", e)
    }

    /** PRD 8.2: student "save" action. Stored as a simple array-union style flag document
     * under the user's saved list, incrementing the material's public save counter. */
    suspend fun incrementSaveCount(materialId: String): Resource<Unit> = try {
        collection.document(materialId)
            .update("saveCount", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not save", e)
    }
}
