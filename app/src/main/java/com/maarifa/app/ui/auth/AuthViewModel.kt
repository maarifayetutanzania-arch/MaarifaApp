package com.maarifa.app.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.maarifa.app.data.model.AuthProvider
import com.maarifa.app.data.model.User
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.data.remote.FirebaseAuthService
import com.maarifa.app.data.repository.AuthRepository
import com.maarifa.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class AuthUiState(
    val checkingSession: Boolean = true,
    val isSignedIn: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isAwaitingEmailVerification: Boolean = false,
    val profile: User? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val otpVerificationId: String? = null,
    val otpAutoCredential: PhoneAuthCredential? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val authService: FirebaseAuthService
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val uid = authRepository.currentUserId
        if (uid == null) {
            _state.value = _state.value.copy(checkingSession = false, isSignedIn = false)
            return
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        val isVerified = currentUser?.isEmailVerified ?: false

        viewModelScope.launch {
            when (val result = authRepository.fetchUserProfile(uid)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    checkingSession = false,
                    isSignedIn = true,
                    isEmailVerified = isVerified,
                    profile = result.data
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    checkingSession = false,
                    isSignedIn = true,
                    isEmailVerified = isVerified,
                    profile = null
                )
                Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _state.value = _state.value.copy(successMessage = null)
    }

    fun registerWithEmail(email: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        when (val result = authRepository.registerWithEmail(email, password)) {
            is Resource.Success -> {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    isAwaitingEmailVerification = true
                )
            }
            is Resource.Error -> _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = result.message
            )
            Resource.Loading -> Unit
        }
    }

    fun verifyAndCompleteRegistration(
        fullName: String,
        phoneNumber: String,
        email: String,
        role: UserRole,
        region: String,
        schoolName: String?,
        formClass: String?,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        when (val checkResult = authRepository.checkIsEmailVerified()) {
            is Resource.Success -> {
                if (checkResult.data) {
                    val uid = authRepository.currentUserId 
                        ?: FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    
                    val profileResult = authRepository.createUserProfile(
                        uid = uid,
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        email = email,
                        provider = AuthProvider.EMAIL,
                        role = role,
                        region = region,
                        schoolName = schoolName,
                        formClass = formClass
                    )

                    when (profileResult) {
                        is Resource.Success -> {
                            loadProfileAfterAuth(uid)
                            _state.value = _state.value.copy(
                                isAwaitingEmailVerification = false,
                                isEmailVerified = true
                            )
                            onSuccess()
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isSubmitting = false,
                                errorMessage = profileResult.message
                            )
                        }
                        Resource.Loading -> Unit
                    }
                } else {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        errorMessage = "Bado hujathibitisha barua pepe yako. Fungua barua pepe uliyotumiwa kisha ubonyeze link ya uhakiki."
                    )
                }
            }
            is Resource.Error -> {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = checkResult.message
                )
            }
            Resource.Loading -> Unit
        }
    }

    fun resendVerificationEmail() = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        when (val result = authRepository.sendEmailVerification()) {
            is Resource.Success -> {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    successMessage = "Barua pepe ya uhakiki imetumwa tena! Angalia Inbox au Spam."
                )
            }
            is Resource.Error -> {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = result.message
                )
            }
            Resource.Loading -> Unit
        }
    }

    fun signInWithEmail(email: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        when (val result = authRepository.signInWithEmail(email, password)) {
            is Resource.Success -> loadProfileAfterAuth(result.data)
            is Resource.Error -> _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = result.message
            )
            Resource.Loading -> Unit
        }
    }

    fun signInWithPhone(phoneNumber: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        when (val result = authRepository.signInWithPhone(phoneNumber, password)) {
            is Resource.Success -> loadProfileAfterAuth(result.data)
            is Resource.Error -> _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = result.message
            )
            Resource.Loading -> Unit
        }
    }

    fun signInWithGoogleIdToken(idToken: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        when (val result = authRepository.signInWithGoogle(idToken)) {
            is Resource.Success -> loadProfileAfterAuth(result.data)
            is Resource.Error -> _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = result.message
            )
            Resource.Loading -> Unit
        }
    }

    fun requestOtp(activity: Activity, phoneNumber: String) {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        authService.requestOtp(activity, phoneNumber)
            .onEach { event ->
                when (event) {
                    is FirebaseAuthService.OtpEvent.CodeSent ->
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            otpVerificationId = event.verificationId,
                            errorMessage = null
                        )
                    is FirebaseAuthService.OtpEvent.AutoVerified ->
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            otpAutoCredential = event.credential
                        )
                    is FirebaseAuthService.OtpEvent.Failed ->
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            errorMessage = event.message
                        )
                }
            }
            .catch { e ->
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "Imeshindikana kutuma OTP"
                )
            }
            .launchIn(viewModelScope)
    }

    fun signInWithAutoCredential(credential: PhoneAuthCredential) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        try {
            val result = authService.signInWithPhoneCredential(credential)
            loadProfileAfterAuth(result.user?.uid.orEmpty())
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = e.message ?: "Imeshindikana kuingia"
            )
        }
    }

    fun confirmOtp(verificationId: String, code: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        when (val result = authRepository.confirmOtp(verificationId, code)) {
            is Resource.Success -> loadProfileAfterAuth(result.data)
            is Resource.Error -> _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = result.message
            )
            Resource.Loading -> Unit
        }
    }

    fun sendPasswordResetEmail(email: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            _state.value = _state.value.copy(
                isSubmitting = false,
                successMessage = "Barua pepe ya kubadili nenosiri imetumwa."
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = e.message ?: "Imeshindikana kutuma barua pepe ya kubadili nenosiri"
            )
        }
    }

    private suspend fun loadProfileAfterAuth(uid: String) {
        if (uid.isBlank()) {
            _state.value = _state.value.copy(
                isSubmitting = false,
                isSignedIn = false,
                errorMessage = "User ID haipatikani"
            )
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val isVerified = currentUser?.isEmailVerified ?: false

        when (val result = authRepository.fetchUserProfile(uid)) {
            is Resource.Success -> _state.value = _state.value.copy(
                isSubmitting = false,
                isSignedIn = true,
                isEmailVerified = isVerified,
                profile = result.data,
                otpVerificationId = null,
                otpAutoCredential = null
            )
            is Resource.Error -> _state.value = _state.value.copy(
                isSubmitting = false,
                isSignedIn = true,
                isEmailVerified = isVerified,
                profile = null,
                otpVerificationId = null,
                otpAutoCredential = null
            )
            Resource.Loading -> Unit
        }
    }

    fun completeRegistration(
        uidParam: String?,
        fullName: String,
        phoneNumber: String,
        email: String,
        provider: AuthProvider,
        role: UserRole,
        region: String,
        schoolName: String?,
        formClass: String? = null
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)

        val activeUid = uidParam?.takeIf { it.isNotBlank() }
            ?: authRepository.currentUserId
            ?: FirebaseAuth.getInstance().currentUser?.uid

        if (activeUid.isNullOrBlank()) {
            _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = "Authentication failed: User ID not found. Tafadhali ingia tena."
            )
            return@launch
        }

        val result = authRepository.createUserProfile(
            activeUid,
            fullName,
            phoneNumber,
            email,
            provider,
            role,
            region,
            schoolName,
            formClass
        )

        when (result) {
            is Resource.Success -> loadProfileAfterAuth(activeUid)
            is Resource.Error -> _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = result.message
            )
            Resource.Loading -> Unit
        }
    }

    fun signOut() {
        authRepository.signOut()
        _state.value = AuthUiState(checkingSession = false, isSignedIn = false)
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val authService: FirebaseAuthService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository, authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
