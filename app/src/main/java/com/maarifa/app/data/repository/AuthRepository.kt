package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.maarifa.app.data.model.AccountStatus
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.Teacher
import com.maarifa.app.data.model.TeacherVerificationStatus
import com.maarifa.app.data.model.User
import com.maarifa.app.data.model.UserProfile
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
        Resource.Error(e.message ?: "Ingia imefeli", e)
    }

    suspend fun registerWithEmail(email: String, password: String): Resource<String> = try {
        val result = authService.registerWithEmail(email, password)
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Usajili umefeli", e)
    }

    suspend fun signInWithGoogle(idToken: String): Resource<String> = try {
        val result = authService.signInWithGoogleIdToken(idToken)
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Google sign-in imefeli", e)
    }

    suspend fun confirmOtp(verificationId: String, code: String): Resource<String> = try {
        val result = authService.confirmOtp(verificationId, code)
        Resource.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Uhakiki wa OTP umefeli", e)
    }

    /**
     * Inatengeneza profile ya mtumiaji kwenye Firestore `users/{uid}`.
     * FormClass imewekwa kuwa String? ili walimu wasilazimike kuwa nayo.
     */
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
            schoolName = schoolName?.takeIf { it.isNotBlank() },
            formClass = if (role == UserRole.STUDENT) formClass else null,
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
        Resource.Error(e.message ?: "Imeshindikana kuhifadhi taarifa za akaunti", e)
    }

    /**
     * Njia ya mkato ya kuhifadhi kutumia `UserProfile` model kutoka kwenye UI
     */
    suspend fun saveUserProfile(
        uid: String,
        profile: UserProfile,
        email: String = "",
        phoneNumber: String = "",
        provider: AuthProvider = AuthProvider.EMAIL
    ): Resource<Unit> {
        return createUserProfile(
            uid = uid,
            fullName = profile.fullName,
            phoneNumber = phoneNumber,
            email = email,
            provider = provider,
            role = profile.roleEnum,
            region = profile.region,
            schoolName = profile.schoolName,
            formClass = profile.formClass
        )
    }

    suspend fun fetchUserProfile(uid: String): Resource<User> = try {
        val snap = firestore.collection(FirestorePaths.USERS).document(uid).get().await()
        if (snap.exists()) {
            val user = snap.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Format ya akaunti si sahihi")
            }
        } else {
            Resource.Error("Profile haijapatikana")
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Imeshindikana kupakua taarifa za akaunti", e)
    }
}
