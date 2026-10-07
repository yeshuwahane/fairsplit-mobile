package com.cybernerd404.fairsplit.presentation.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.auth.GoogleAuthManager
import com.cybernerd404.fairsplit.domain.auth.GoogleSignInResult
import com.cybernerd404.fairsplit.domain.model.UserPreferences
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.domain.usecase.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val linkGoogleAccountUseCase: LinkGoogleAccountUseCase,
    private val linkPhoneAccountUseCase: LinkPhoneAccountUseCase,
    private val googleAuthManager: GoogleAuthManager
) : ScreenModel {

    private val _userProfile = MutableStateFlow<DataResource<UserProfile>>(DataResource.loading())
    val userProfile = _userProfile.asStateFlow()

    private val _userPreferences = MutableStateFlow<DataResource<UserPreferences>>(DataResource.loading())
    val userPreferences = _userPreferences.asStateFlow()

    private val _linkingMessage = MutableStateFlow<String?>(null)
    val linkingMessage = _linkingMessage.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        screenModelScope.launch {
            _userProfile.value = getUserProfileUseCase()
            _userPreferences.value = getUserPreferencesUseCase()
        }
    }

    fun updateProfile(name: String, email: String, phone: String, upiId: String, avatarUrl: String?) {
        val current = _userProfile.value.data ?: LocalDatabase.getUserProfile() ?: UserProfile(
            id = "usr_me",
            name = name,
            email = email,
            phone = phone,
            upiId = upiId
        )
        val updated = current.copy(
            name = name,
            email = email,
            phone = phone,
            upiId = upiId,
            avatarUrl = avatarUrl,
            avatarInitials = if (current.avatarInitials.isNotBlank()) current.avatarInitials else name.take(2).uppercase().ifEmpty { "ME" }
        )
        Napier.d(tag = "FAIRSPLIT_SETTINGS", message = "Updating profile: name=$name, avatarUrl=$avatarUrl")
        screenModelScope.launch {
            val result = updateUserProfileUseCase(updated)
            _userProfile.value = result
            if (result.isSuccess()) {
                _linkingMessage.value = "Profile updated successfully!"
            }
        }
    }

    fun linkGoogleWithCredentials(idToken: String, email: String?, displayName: String?, photoUrl: String?) {
        screenModelScope.launch {
            _linkingMessage.value = "Connecting with Google..."
            linkGoogleAccountUseCase(idToken, email, displayName).collect { resource ->
                if (resource.isSuccess()) {
                    val current = resource.data!!
                    val updated = if (!photoUrl.isNullOrBlank() && current.avatarUrl.isNullOrBlank()) {
                        current.copy(avatarUrl = photoUrl)
                    } else current
                    _userProfile.value = DataResource.success(updated)
                    _linkingMessage.value = "Google account (${email ?: "connected"}) linked successfully!"
                    updateUserProfileUseCase(updated)
                } else {
                    _linkingMessage.value = resource.error?.message ?: "Failed to link Google account"
                }
            }
        }
    }

    fun handleGoogleLinkError(error: String) {
        _linkingMessage.value = error
    }

    fun linkGoogleAccount() {
        screenModelScope.launch {
            _linkingMessage.value = "Connecting with Google..."
            when (val result = googleAuthManager.signIn()) {
                is GoogleSignInResult.Success -> {
                    val creds = result.credentials
                    linkGoogleWithCredentials(creds.idToken, creds.email, creds.displayName, creds.photoUrl)
                }
                is GoogleSignInResult.Error -> {
                    _linkingMessage.value = result.message
                }
                is GoogleSignInResult.Cancelled -> {
                    _linkingMessage.value = null
                }
            }
        }
    }

    fun linkPhoneAccount(phone: String, otp: String) {
        screenModelScope.launch {
            _linkingMessage.value = "Verifying phone..."
            linkPhoneAccountUseCase.verifyOtp(phone, otp).collect { resource ->
                if (resource.isSuccess()) {
                    _userProfile.value = DataResource.success(resource.data!!)
                    _linkingMessage.value = "Phone linked successfully!"
                } else {
                    _linkingMessage.value = resource.error?.message ?: "Failed to link phone"
                }
            }
        }
    }

    fun clearLinkingMessage() {
        _linkingMessage.value = null
    }

    fun toggleDarkMode(enabled: Boolean) {
        val current = _userPreferences.value.data ?: return
        val updated = current.copy(isDarkMode = enabled)
        screenModelScope.launch {
            _userPreferences.value = updateUserPreferencesUseCase(updated)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val current = _userPreferences.value.data ?: return
        val updated = current.copy(notificationsEnabled = enabled)
        screenModelScope.launch {
            _userPreferences.value = updateUserPreferencesUseCase(updated)
        }
    }
}
