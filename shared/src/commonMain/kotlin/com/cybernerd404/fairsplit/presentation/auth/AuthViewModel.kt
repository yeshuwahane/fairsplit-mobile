package com.cybernerd404.fairsplit.presentation.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.domain.auth.GoogleAuthManager
import com.cybernerd404.fairsplit.domain.auth.GoogleSignInResult
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.domain.notification.PushNotificationManager
import com.cybernerd404.fairsplit.domain.usecase.GoogleSignInUseCase
import com.cybernerd404.fairsplit.domain.usecase.RequestPhoneOtpUseCase
import com.cybernerd404.fairsplit.domain.usecase.VerifyPhoneOtpUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object SendingOtp : AuthUiState()
    object OtpSent : AuthUiState()
    object VerifyingOtp : AuthUiState()
    object GoogleSigningIn : AuthUiState()
    data class Authenticated(val user: UserProfile, val isProfileCompleted: Boolean) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val requestPhoneOtpUseCase: RequestPhoneOtpUseCase,
    private val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val googleAuthManager: GoogleAuthManager,
    private val pushNotificationManager: PushNotificationManager
) : ScreenModel {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode = _otpCode.asStateFlow()

    fun updatePhoneNumber(phone: String) {
        _phoneNumber.value = phone
    }

    fun updateOtpCode(otp: String) {
        _otpCode.value = otp
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun requestOtp() {
        val phone = _phoneNumber.value.trim()
        if (phone.length < 10) {
            _uiState.value = AuthUiState.Error("Please enter a valid 10-digit mobile number")
            return
        }

        val fullPhone = if (!phone.startsWith("+")) "+91$phone" else phone
        screenModelScope.launch {
            _uiState.value = AuthUiState.SendingOtp
            requestPhoneOtpUseCase(fullPhone).collect { resource ->
                if (resource.isSuccess()) {
                    _uiState.value = AuthUiState.OtpSent
                } else if (resource.isError()) {
                    _uiState.value = AuthUiState.Error(resource.error?.message ?: "Failed to send OTP")
                }
            }
        }
    }

    fun verifyOtp() {
        val phone = _phoneNumber.value.trim()
        val otp = _otpCode.value.trim()

        if (otp.length != 6) {
            _uiState.value = AuthUiState.Error("Please enter complete 6-digit OTP (123456)")
            return
        }

        val fullPhone = if (!phone.startsWith("+")) "+91$phone" else phone
        screenModelScope.launch {
            _uiState.value = AuthUiState.VerifyingOtp
            verifyPhoneOtpUseCase(fullPhone, otp).collect { resource ->
                if (resource.isSuccess()) {
                    val authResponse = resource.data!!
                    registerFcmTokenInBackground()
                    val isComplete = authResponse.profileCompleted &&
                            authResponse.user.name.isNotBlank() &&
                            !authResponse.user.name.startsWith("User ")
                    _uiState.value = AuthUiState.Authenticated(
                        user = authResponse.user,
                        isProfileCompleted = isComplete
                    )
                } else if (resource.isError()) {
                    _uiState.value = AuthUiState.Error(resource.error?.message ?: "OTP verification failed")
                }
            }
        }
    }

    fun handleGoogleSignInSuccess(
        idToken: String,
        email: String?,
        displayName: String?,
        photoUrl: String?
    ) {
        screenModelScope.launch {
            _uiState.value = AuthUiState.GoogleSigningIn
            googleSignInUseCase(
                idToken = idToken,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl
            ).collect { resource ->
                if (resource.isSuccess()) {
                    val authResponse = resource.data!!
                    registerFcmTokenInBackground()
                    val isComplete = authResponse.profileCompleted &&
                            authResponse.user.name.isNotBlank() &&
                            !authResponse.user.name.startsWith("User ")
                    _uiState.value = AuthUiState.Authenticated(
                        user = authResponse.user,
                        isProfileCompleted = isComplete
                    )
                } else if (resource.isError()) {
                    _uiState.value = AuthUiState.Error(resource.error?.message ?: "Google login failed")
                }
            }
        }
    }

    fun handleGoogleSignInError(error: String) {
        _uiState.value = AuthUiState.Error(error)
    }

    private fun registerFcmTokenInBackground() {
        screenModelScope.launch {
            try {
                val token = pushNotificationManager.getFcmToken()
                if (!token.isNullOrBlank()) {
                    Napier.i(tag = "FAIRSPLIT_AUTH", message = "FCM token dispatched for authenticated user: $token")
                }
            } catch (e: Exception) {
                Napier.w(tag = "FAIRSPLIT_AUTH", message = "Could not retrieve FCM token: ${e.message}")
            }
        }
    }
}
