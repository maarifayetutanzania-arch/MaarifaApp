package com.maarifa.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maarifa.app.data.model.Download
import com.maarifa.app.data.model.Material
import com.maarifa.app.data.model.PaymentChannel
import com.maarifa.app.data.model.PlanType
import com.maarifa.app.data.model.Subscription
import com.maarifa.app.data.model.User
import com.maarifa.app.data.repository.AuthRepository
import com.maarifa.app.data.repository.DownloadRepository
import com.maarifa.app.data.repository.EngagementRepository
import com.maarifa.app.data.repository.MaterialRepository
import com.maarifa.app.data.repository.PaymentRepository
import com.maarifa.app.data.repository.SubscriptionRepository
import com.maarifa.app.domain.AccessControlUseCase
import com.maarifa.app.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------- Library ----------------

data class LibraryUiState(
    val isLoading: Boolean = true,
    val allMaterials: List<Material> = emptyList(),
    val visibleMaterials: List<Material> = emptyList(),
    val query: String = "",
    val formFilter: String? = null,
    val subjectFilter: String? = null,
    val errorMessage: String? = null
)

class LibraryViewModel(
    private val materialRepository: MaterialRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    private var libraryJob: Job? = null

    init {
        reload()
    }

    fun reload() {
        libraryJob?.cancel()
        libraryJob = materialRepository.observeLibrary(_state.value.formFilter, _state.value.subjectFilter)
            .onEach { res ->
                _state.update { current ->
                    when (res) {
                        is Resource.Success -> {
                            val filtered = materialRepository.filterBySearch(res.data, current.query)
                            current.copy(
                                isLoading = false,
                                allMaterials = res.data,
                                visibleMaterials = filtered,
                                errorMessage = null
                            )
                        }
                        is Resource.Error -> current.copy(
                            isLoading = false,
                            errorMessage = res.message
                        )
                        Resource.Loading -> current.copy(isLoading = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _state.update { current ->
            current.copy(
                query = query,
                visibleMaterials = materialRepository.filterBySearch(current.allMaterials, query)
            )
        }
    }

    fun setFilters(form: String?, subject: String?) {
        _state.update { it.copy(formFilter = form, subjectFilter = subject) }
        reload()
    }
}

// ---------------- Material detail ----------------

data class MaterialDetailUiState(
    val isLoading: Boolean = true,
    val material: Material? = null,
    val subscription: Subscription? = null,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float? = null,
    val accessDecision: AccessControlUseCase.AccessDecision = AccessControlUseCase.AccessDecision.SubscriptionRequired,
    val errorMessage: String? = null
)

class MaterialDetailViewModel(
    private val materialId: String,
    private val materialRepository: MaterialRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val downloadRepository: DownloadRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MaterialDetailUiState())
    val state: StateFlow<MaterialDetailUiState> = _state.asStateFlow()

    init {
        val userId = authRepository.currentUserId
        viewModelScope.launch {
            when (val res = materialRepository.getMaterial(materialId)) {
                is Resource.Success -> {
                    val downloaded = downloadRepository.isDownloadedLocally(materialId)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            material = res.data,
                            isDownloaded = downloaded
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = res.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
        if (userId != null) {
            subscriptionRepository.observeLatestSubscription(userId).onEach { res ->
                if (res is Resource.Success) {
                    val decision = _state.value.material?.let { AccessControlUseCase.canViewOnline(it, res.data) }
                        ?: AccessControlUseCase.AccessDecision.SubscriptionRequired
                    _state.update {
                        it.copy(subscription = res.data, accessDecision = decision)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun save() = viewModelScope.launch { 
        materialRepository.incrementSaveCount(materialId) 
    }

    fun download() {
        val userId = authRepository.currentUserId ?: return
        val material = _state.value.material ?: return
        if (AccessControlUseCase.canStartDownload(material, _state.value.subscription) != AccessControlUseCase.AccessDecision.Allowed) return
        
        viewModelScope.launch {
            _state.update { it.copy(downloadProgress = 0f) }
            val result = downloadRepository.downloadMaterial(userId, materialId, material.fileUrl) { progress ->
                _state.update { it.copy(downloadProgress = progress) }
            }
            _state.update {
                it.copy(
                    downloadProgress = null,
                    isDownloaded = result is Resource.Success,
                    errorMessage = (result as? Resource.Error)?.message
                )
            }
        }
    }
}

// ---------------- Subscription ----------------

data class SubscriptionUiState(
    val subscription: Subscription? = null,
    val isSubmitting: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionUiState())
    val state: StateFlow<SubscriptionUiState> = _state.asStateFlow()

    init {
        authRepository.currentUserId?.let { uid ->
            subscriptionRepository.observeLatestSubscription(uid).onEach { res ->
                if (res is Resource.Success) {
                    _state.update { it.copy(subscription = res.data) }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun subscribe(plan: PlanType, channel: PaymentChannel, payerAccountOrPhone: String) {
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null, statusMessage = null) }
            when (val res = paymentRepository.initiatePayment(uid, plan, channel, payerAccountOrPhone)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            statusMessage = res.data.instructions ?: "Payment request sent. We'll activate your plan as soon as it's verified."
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(isSubmitting = false, errorMessage = res.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }
}

// ---------------- Downloads ----------------

data class DownloadsUiState(
    val isLoading: Boolean = true,
    val downloads: List<Download> = emptyList(),
    val errorMessage: String? = null
)

class DownloadsViewModel(
    private val downloadRepository: DownloadRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DownloadsUiState())
    val state: StateFlow<DownloadsUiState> = _state.asStateFlow()

    init {
        authRepository.currentUserId?.let { uid ->
            downloadRepository.observeDownloads(uid).onEach { res ->
                _state.update { current ->
                    when (res) {
                        is Resource.Success -> current.copy(isLoading = false, downloads = res.data)
                        is Resource.Error -> current.copy(isLoading = false, errorMessage = res.message)
                        Resource.Loading -> current.copy(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun remove(materialId: String) {
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch { 
            downloadRepository.removeDownload(uid, materialId) 
        }
    }
}

// ---------------- Profile ----------------

data class StudentProfileUiState(
    val user: User? = null, 
    val isLoading: Boolean = true
)

class StudentProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(StudentProfileUiState())
    val state: StateFlow<StudentProfileUiState> = _state.asStateFlow()

    init {
        authRepository.currentUserId?.let { uid ->
            viewModelScope.launch {
                val res = authRepository.fetchUserProfile(uid)
                _state.update {
                    StudentProfileUiState(
                        user = (res as? Resource.Success)?.data,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signOut() = authRepository.signOut()
}
