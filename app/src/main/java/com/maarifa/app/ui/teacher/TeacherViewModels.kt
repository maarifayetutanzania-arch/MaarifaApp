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
                _state.value = _state.value.copy(user = (res as? Resource.Success)?.data)
            }
            teacherRepository.observeTeacher(uid).onEach { res ->
                _state.value = when (res) {
                    is Resource.Success -> _state.value.copy(isLoading = false, teacher = res.data)
                    is Resource.Error -> _state.value.copy(isLoading = false, errorMessage = res.message)
                    Resource.Loading -> _state.value.copy(isLoading = true)
                }
            }.launchIn(viewModelScope)
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
                teacherId = uid, teacherName = teacherName, title = title, description = description,
                form = form, subject = subject, topic = topic, fileUri = fileUri, fileType = MaterialFileType.PDF.name
            ) { progress -> _state.value = _state.value.copy(progress = progress) }

            _state.value = when (result) {
                is Resource.Success -> UploadUiState(successMessage = "Uploaded — pending admin review.")
                is Resource.Error -> UploadUiState(errorMessage = result.message)
                Resource.Loading -> _state.value
            }
        }
    }

    fun resetStatus() { _state.value = UploadUiState() }
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
                _state.value = when (res) {
                    is Resource.Success -> _state.value.copy(isLoading = false, materials = res.data)
                    is Resource.Error -> _state.value.copy(isLoading = false, errorMessage = res.message)
                    Resource.Loading -> _state.value.copy(isLoading = true)
                }
            }.launchIn(viewModelScope)
        }
    }
}

// ---------------- Earnings / payouts ----------------

data class TeacherEarningsUiState(
    val isLoading: Boolean = true,
    val teacher: Teacher? = null,
    val payouts: List<Payout> = emptyList(),
    val errorMessage: String? = null
)

class TeacherEarningsViewModel(
    private val teacherRepository: TeacherRepository,
    private val payoutRepository: PayoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TeacherEarningsUiState())
    val state: StateFlow<TeacherEarningsUiState> = _state.asStateFlow()

    init {
        authRepository.currentUserId?.let { uid ->
            teacherRepository.observeTeacher(uid).onEach { res ->
                if (res is Resource.Success) _state.value = _state.value.copy(isLoading = false, teacher = res.data)
            }.launchIn(viewModelScope)

            payoutRepository.observePayouts(uid).onEach { res ->
                if (res is Resource.Success) _state.value = _state.value.copy(payouts = res.data)
            }.launchIn(viewModelScope)
        }
    }
}
