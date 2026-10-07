package com.cybernerd404.fairsplit.presentation.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.domain.usecase.GetAuthSessionUseCase
import com.cybernerd404.fairsplit.domain.usecase.UpdateProfileUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileSetupUiState {
    object Loading : ProfileSetupUiState()
    object Idle : ProfileSetupUiState()
    object Saving : ProfileSetupUiState()
    data class Success(val profile: UserProfile) : ProfileSetupUiState()
    data class Error(val message: String) : ProfileSetupUiState()
}

class ProfileSetupViewModel(
    private val getAuthSessionUseCase: GetAuthSessionUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ScreenModel {

    private val _uiState = MutableStateFlow<ProfileSetupUiState>(ProfileSetupUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _upiId = MutableStateFlow("")
    val upiId = _upiId.asStateFlow()

    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl = _avatarUrl.asStateFlow()

    private var currentUser: UserProfile? = null

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        screenModelScope.launch {
            _uiState.value = ProfileSetupUiState.Loading
            getAuthSessionUseCase().collect { resource ->
                if (resource.isSuccess()) {
                    val user = resource.data!!
                    currentUser = user
                    _displayName.value = if (user.name.startsWith("User ") && user.name.length <= 10) "" else user.name
                    _email.value = user.email
                    _phone.value = user.phone
                    _upiId.value = user.upiId
                    _avatarUrl.value = user.avatarUrl
                    _uiState.value = ProfileSetupUiState.Idle
                } else {
                    _uiState.value = ProfileSetupUiState.Idle
                }
            }
        }
    }

    fun updateDisplayName(name: String) {
        _displayName.value = name
    }

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updatePhone(phone: String) {
        _phone.value = phone
    }

    fun updateUpiId(upi: String) {
        _upiId.value = upi
    }

    fun setAvatar(urlOrIdentifier: String?) {
        _avatarUrl.value = urlOrIdentifier
    }

    fun removeAvatar() {
        _avatarUrl.value = null
    }

    fun saveProfile() {
        val name = _displayName.value.trim()
        if (name.isBlank()) {
            _uiState.value = ProfileSetupUiState.Error("Display name is required")
            return
        }

        screenModelScope.launch {
            _uiState.value = ProfileSetupUiState.Saving
            val baseUser = currentUser ?: UserProfile(
                id = "usr_${com.cybernerd404.fairsplit.data.utils.currentTimeMillis()}",
                name = name,
                email = _email.value.trim()
            )

            val updated = baseUser.copy(
                name = name,
                email = _email.value.trim(),
                phone = _phone.value.trim(),
                upiId = _upiId.value.trim(),
                avatarInitials = name.take(2).uppercase(),
                avatarUrl = _avatarUrl.value,
                profileCompleted = true
            )

            updateProfileUseCase(updated).collect { resource ->
                if (resource.isSuccess()) {
                    Napier.i(tag = "FAIRSPLIT_PROFILE", message = "Profile setup completed for ${updated.name}")
                    _uiState.value = ProfileSetupUiState.Success(resource.data!!)
                } else {
                    _uiState.value = ProfileSetupUiState.Error(resource.error?.message ?: "Failed to save profile")
                }
            }
        }
    }
}
