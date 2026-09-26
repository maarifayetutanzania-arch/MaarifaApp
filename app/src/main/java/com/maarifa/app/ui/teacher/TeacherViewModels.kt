package com.maarifa.app.ui.teacher

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maarifa.app.data.model.Material
import com.maarifa.app.data.model.MaterialFileType
import com.maarifa.app.data.model.Payout
import com.maarifa.app.data.model.Teacher
import com.maarifa.app.data.model.User
import com.maarifa.app.data.repository.AuthRepository
import com.maarifa.app.data.repository.MaterialRepository
import com.maarifa.app.data.repository.PayoutRepository
import com.maarifa.app.data.repository.TeacherRepository
import com.maarifa.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------- Dashboard (verification status + live stats) ----------------

data class TeacherDashboardUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val teacher: Teacher? = null,
    val errorMessage: String? = null
)

class TeacherDashboardViewModel(
    private val teacherRepository: TeacherRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TeacherDashboardUiState())
    val state: StateFlow<TeacherDashboardUiState> = _state.asStateFlow()

    init {
        val uid = authRepository.currentUserId
        if (uid != null) {
            viewModelScope.launch {
                val res = authRepository.fetchUserProfile(uid)
                if (res is Resource.Success) {
                    _state.update { it.copy(user = res.data) }
                }
            }
            teacherRepository.observeTeacher(uid).onEach { res ->
                _state.update { currentState ->
                    when (res) {
                        is Resource.Success -> {
                            val teacherData = res.data ?: Teacher(teacherId = uid, userId = uid)
                            currentState.copy(isLoading = false, teacher = teacherData, errorMessage = null)
                        }
                        is Resource.Error -> currentState.copy(isLoading = false, errorMessage = res.message)
                        Resource.Loading -> currentState.copy(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            _state.update { it.copy(isLoading = false, errorMessage = "User session expired.") }
        }
    }

    fun signOut() = authRepository.signOut()
}

// ---------------- Upload ----------------

data class UploadUiState(
    val isSubmitting: Boolean = false,
    val progress: Float? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class UploadMaterialViewModel(
    private val materialRepository: MaterialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state.asStateFlow()

    fun upload(
        teacherName: String,
        title: String,
        description: String,
        form: String,
        subject: String,
        topic: String,
        fileUri: Uri
    ) {
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _state.value = UploadUiState(isSubmitting = true, progress = 0f)
            val result = materialRepository.uploadMaterial(
                teacherId = uid,
                teacherName = teacherName,
                title = title,
                description = description,
                form = form,
                subject = subject,
                topic = topic,
                fileUri = fileUri,
                fileType = MaterialFileType.PDF.name
            ) { progress ->
                _state.update { it.copy(progress = progress) }
            }

            _state.value = when (result) {
                is Resource.Success -> UploadUiState(successMessage = "Uploaded — pending admin review.")
                is Resource.Error -> UploadUiState(errorMessage = result.message)
                Resource.Loading -> _state.value
            }
        }
    }

    fun resetStatus() {
        _state.value = UploadUiState()
    }
}

// ---------------- Teacher's own materials ----------------

data class TeacherMaterialsUiState(
    val isLoading: Boolean = true,
    val materials: List<Material> = emptyList(),
    val errorMessage: String? = null
)

class TeacherMaterialsViewModel(
    private val materialRepository: MaterialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TeacherMaterialsUiState())
    val state: StateFlow<TeacherMaterialsUiState> = _state.asStateFlow()

    init {
        authRepository.currentUserId?.let { uid ->
            materialRepository.observeTeacherMaterials(uid).onEach { res ->
                _state.update { currentState ->
                    when (res) {
                        is Resource.Success -> currentState.copy(isLoading = false, materials = res.data ?: emptyList(), errorMessage = null)
                        is Resource.Error -> currentState.copy(isLoading = false, errorMessage = res.message)
                        Resource.Loading -> currentState.copy(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        } ?: run {
            _state.update { it.copy(isLoading = false, errorMessage = "User session expired.") }
        }
    }
}

// ---------------- Earnings / payouts ----------------

data class TeacherEarningsUiState(
    val isLoading: Boolean = true,
    val isSavingPayment: Boolean = false,
    val teacher: Teacher? = null,
    val payouts: List<Payout> = emptyList(),
    val errorMessage: String? = null,
    val saveSuccessMessage: String? = null
)

class TeacherEarningsViewModel(
    private val teacherRepository: TeacherRepository,
    private val payoutRepository: PayoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TeacherEarningsUiState())
    val state: StateFlow<TeacherEarningsUiState> = _state.asStateFlow()

    init {
        val uid = authRepository.currentUserId
        if (uid != null) {
            // TEST: observe teacher TU (bila payouts)
            teacherRepository.observeTeacher(uid).onEach { res ->
                when (res) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                teacher = res.data ?: Teacher(teacherId = uid, userId = uid),
                                payouts = emptyList(),
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(isLoading = false, errorMessage = res.message)
                        }
                    }
                    Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            _state.update {
                it.copy(isLoading = false, errorMessage = "User session expired.")
            }
        }
    }

    fun savePaymentInfo(method: String, provider: String, accountNumber: String) {
        // leave empty for now
    }
}
