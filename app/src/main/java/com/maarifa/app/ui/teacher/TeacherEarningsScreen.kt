package com.maarifa.app.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maarifa.app.data.model.Payout
import com.maarifa.app.data.model.Teacher
import com.maarifa.app.data.repository.AuthRepository
import com.maarifa.app.data.repository.PayoutRepository
import com.maarifa.app.data.repository.TeacherRepository
import com.maarifa.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

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
            // TEST: teacher TU (bila payouts)
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
