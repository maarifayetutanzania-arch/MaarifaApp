package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.maarifa.app.data.model.Teacher
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
}
