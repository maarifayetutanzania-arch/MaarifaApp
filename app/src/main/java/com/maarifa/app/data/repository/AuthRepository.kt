package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.maarifa.app.data.model.AccountStatus
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.Teacher
import com.maarifa.app.data.model.TeacherVerificationStatus
import com.maarifa.app.data.model.User
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.data.remote.FirebaseAuthService
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val authService: FirebaseAuthService,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId get() = authService.currentUserId
    val isSignedIn get() = authService.isSignedIn

    fun signOut() = authService.signOut()

    suspend fun signInWithEmail(email: String, password: String): Resource<String> = try {
        val result = authService.signInWithEmail(email, password)
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Sign-in failed", e)
    }

    suspend fun signInWithPhone(phoneNumber: String, password: String): Resource<String> = try {
        val result = authService.signInWithEmail(phoneNumber, password) 
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Phone sign-in failed", e)
    }

    suspend fun registerWithEmail(email: String, password: String): Resource<String> = try {
        val result = authService.registerWithEmail(email, password)
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Registration failed", e)
    }

    suspend fun signInWithGoogle(idToken: String): Resource<String> = try {
        val result = authService.signInWithGoogleIdToken(idToken)
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Google sign-in failed", e)
    }

    suspend fun confirmOtp(verificationId: String, code: String): Resource<String> = try {
        val result = authService.confirmOtp(verificationId, code)
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "OTP verification failed", e)
    }

    suspend fun createUserProfile(
        uid: String,
        fullName: String,
        phoneNumber: String,
        email: String,
        provider: AuthProvider,
        role: UserRole,
        region: String,
        schoolName: String?,
        formClass: String? = null
    ): Resource<Unit> = try {
        val user = User(
            userId = uid,
            fullName = fullName,
            phoneNumber = phoneNumber,
            email = email,
            authProvider = provider.name,
            role = role.name,
            region = region,
            schoolName = schoolName.orEmpty(),
            formClass = if (role == UserRole.STUDENT) formClass.orEmpty() else "",
            status = AccountStatus.ACTIVE.name
        )
        firestore.collection(FirestorePaths.USERS).document(uid).set(user).await()

        if (role == UserRole.TEACHER) {
            val teacher = Teacher(
                teacherId = uid,
                userId = uid,
                verificationStatus = TeacherVerificationStatus.PENDING.name
            )
            firestore.collection(FirestorePaths.TEACHERS).document(uid).set(teacher).await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not create profile", e)
    }

    suspend fun fetchUserProfile(uid: String): Resource<User> = try {
        val snap = firestore.collection(FirestorePaths.USERS).document(uid).get().await()
        if (snap.exists()) {
            val user = snap.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Profile format is invalid")
            }
        } else {
            Resource.Error("Profile not found")
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not load profile", e)
    }
}

