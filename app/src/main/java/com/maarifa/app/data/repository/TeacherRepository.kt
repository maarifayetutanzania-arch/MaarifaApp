package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    /**
     * Live verification status + earnings balance/engagement score.
     * Inatumia real-time SnapshotListener ili Admin aki-verify kwenye web,
     * kioo cha simu ya Mwalimu kinabadilika PAPO HAPO bila ku-restart app.
     */
    fun observeTeacher(teacherId: String): Flow<Resource<Teacher?>> = callbackFlow {
        if (teacherId.isBlank()) {
            trySend(Resource.Error("Invalid Teacher ID"))
            close()
            return@callbackFlow
        }

        val registration = collection.document(teacherId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load teacher profile"))
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                trySend(Resource.Success(null))
                return@addSnapshotListener
            }

            val teacher = try {
                snapshot.toObject(Teacher::class.java)?.copy(teacherId = snapshot.id)
            } catch (e: Exception) {
                null
            }

            trySend(Resource.Success(teacher))
        }

        awaitClose { registration.remove() }
    }

    /** Single fetch kwa ajili ya background tasks zisizohitaji live listener */
    suspend fun getTeacherOnce(teacherId: String): Resource<Teacher?> = try {
        val snapshot = collection.document(teacherId).get().await()
        if (snapshot.exists()) {
            val teacher = snapshot.toObject(Teacher::class.java)?.copy(teacherId = snapshot.id)
            Resource.Success(teacher)
        } else {
            Resource.Success(null)
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch teacher profile", e)
    }

    /** Updates payment receiving details for a teacher safely using SetOptions.merge() */
    suspend fun updateTeacherPaymentInfo(
        teacherId: String,
        paymentMethod: String,
        provider: String,
        accountNumber: String
    ): Resource<Unit> = try {
        val updates = mapOf(
            "teacherId" to teacherId,
            "paymentMethod" to paymentMethod,
            "paymentProvider" to provider,
            "paymentAccountNumber" to accountNumber,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        collection.document(teacherId)
            .set(updates, SetOptions.merge())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Kuhifadhi taarifa za malipo kumeshindikana", e)
    }

    /** Helper ya kusaidia Admin ku-update Status moja kwa moja (kama inatumika kwenye test/local side) */
    suspend fun updateVerificationStatus(
        teacherId: String,
        status: String
    ): Resource<Unit> = try {
        val updates = mapOf(
            "verificationStatus" to status.uppercase(),
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        collection.document(teacherId)
            .set(updates, SetOptions.merge())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Kusasisha status kumeshindikana", e)
    }
}
