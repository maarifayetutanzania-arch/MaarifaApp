package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.maarifa.app.data.model.Teacher
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TeacherRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = firestore.collection(FirestorePaths.TEACHERS)

    /** Live verification status + earnings balance/engagement score, all of which are
     * written server-side (admin approval, scheduled earnings runs) — the teacher app
     * only ever reads this document. */
    fun observeTeacher(teacherId: String): Flow<Resource<Teacher?>> = callbackFlow {
        val registration = collection.document(teacherId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load teacher profile"))
                return@addSnapshotListener
            }
            trySend(Resource.Success(snapshot?.toObject(Teacher::class.java)))
        }
        awaitClose { registration.remove() }
    }

    /** Updates payment receiving details for a teacher */
    suspend fun updateTeacherPaymentInfo(
        teacherId: String,
        paymentMethod: String,
        provider: String,
        accountNumber: String
    ): Resource<Unit> = try {
        val updates = mapOf(
            "paymentMethod" to paymentMethod,
            "paymentProvider" to provider,
            "paymentAccountNumber" to accountNumber
        )
        collection.document(teacherId).update(updates).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Kuhifadhi taarifa za malipo kumeshindikana", e)
    }
}
