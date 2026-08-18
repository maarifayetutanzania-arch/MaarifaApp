package com.maarifa.app.data.remote

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/** Thin wrapper around FirebaseAuth covering all three PRD 8.1 sign-in methods. */
class FirebaseAuthService {

    // Tumia 'by lazy' hapa ili kuzuia crash wakati wa app startup
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUserId: String? get() = auth.currentUser?.uid
    val isSignedIn: Boolean get() = auth.currentUser != null

    fun signOut() = auth.signOut()

    // ---------- Email / password ----------

    suspend fun registerWithEmail(email: String, password: String): AuthResult =
        auth.createUserWithEmailAndPassword(email, password).await()

    suspend fun signInWithEmail(email: String, password: String): AuthResult =
        auth.signInWithEmailAndPassword(email, password).await()

    // ---------- Google ----------

    fun googleSignInClient(activity: Activity, webClientId: String): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, options)
    }

    suspend fun signInWithGoogleIdToken(idToken: String): AuthResult {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await()
    }

    // ---------- Phone / OTP ----------

    sealed class OtpEvent {
        data class CodeSent(val verificationId: String) : OtpEvent()
        data class AutoVerified(val credential: PhoneAuthCredential) : OtpEvent()
        data class Failed(val message: String) : OtpEvent()
    }

    fun requestOtp(activity: Activity, phoneNumber: String) = callbackFlow<OtpEvent> {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(OtpEvent.AutoVerified(credential))
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                trySend(OtpEvent.Failed(e.message ?: "Phone verification failed"))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(OtpEvent.CodeSent(verificationId))
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose { }
    }

    suspend fun confirmOtp(verificationId: String, code: String): AuthResult {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return auth.signInWithCredential(credential).await()
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): AuthResult =
        auth.signInWithCredential(credential).await()
}
