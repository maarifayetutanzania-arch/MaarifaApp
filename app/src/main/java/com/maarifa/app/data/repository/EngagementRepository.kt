package com.maarifa.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maarifa.app.data.model.Engagement
import com.maarifa.app.util.AppConfig
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.tasks.await
import java.util.Date

class EngagementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = firestore.collection(FirestorePaths.ENGAGEMENT)

    /**
     * Called whenever a student opens a material (online or offline-downloaded).
     * PRD 8.7: dedupes obvious rapid re-reads client-side (server does the authoritative
     * dedupe again — see functions/src/engagement.ts) by checking lastAccessedAt before
     * incrementing readCount.
     */
    suspend fun recordOpen(userId: String, materialId: String, teacherId: String): Resource<Unit> = try {
        val docId = "${userId}_$materialId"
        val docRef = collection.document(docId)
        val existing = docRef.get().await().toObject(Engagement::class.java)

        val now = Date()
        val minGapMs = AppConfig.MIN_ENGAGEMENT_READ_GAP_MINUTES * 60_000L
        val isDuplicateBurst = existing?.lastAccessedAt != null &&
            (now.time - existing.lastAccessedAt!!.time) < minGapMs

        if (existing == null) {
            docRef.set(
                Engagement(
                    engagementId = docId, userId = userId, materialId = materialId,
                    teacherId = teacherId, readCount = 1, lastAccessedAt = now
                )
            ).await()
        } else if (!isDuplicateBurst) {
            docRef.update(
                mapOf(
                    "readCount" to FieldValue.increment(1),
                    "lastAccessedAt" to now
                )
            ).await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not record engagement", e)
    }

    suspend fun updateProgress(userId: String, materialId: String, progressPercent: Int, additionalSeconds: Long): Resource<Unit> = try {
        val docId = "${userId}_$materialId"
        collection.document(docId).update(
            mapOf(
                "progressPercent" to progressPercent.coerceIn(0, 100),
                "readingTimeSeconds" to FieldValue.increment(additionalSeconds)
            )
        ).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not update progress", e)
    }

    suspend fun forMaterial(materialId: String): Resource<List<Engagement>> = try {
        val snap = collection.whereEqualTo("materialId", materialId).get().await()
        Resource.Success(snap.toObjects(Engagement::class.java))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load engagement", e)
    }
}
