package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.AuthResponse
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class RequestPhoneOtpUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(phone: String): Flow<DataResource<Boolean>> {
        return authRepository.requestPhoneOtp(phone)
    }
}

class VerifyPhoneOtpUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(phone: String, otp: String): Flow<DataResource<AuthResponse>> {
        return authRepository.verifyPhoneOtp(phone, otp)
    }
}

class GoogleSignInUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        idToken: String,
        email: String?,
        displayName: String?,
        photoUrl: String?
    ): Flow<DataResource<AuthResponse>> {
        return authRepository.signInWithGoogle(idToken, email, displayName, photoUrl)
    }
}

class LinkGoogleAccountUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(idToken: String, email: String?, displayName: String?): Flow<DataResource<UserProfile>> {
        return authRepository.linkGoogle(idToken, email, displayName)
    }
}

class LinkPhoneAccountUseCase(
    private val authRepository: AuthRepository
) {
    fun requestOtp(phone: String): Flow<DataResource<Boolean>> {
        return authRepository.linkPhone(phone)
    }

    fun verifyOtp(phone: String, otp: String): Flow<DataResource<UserProfile>> {
        return authRepository.verifyLinkPhone(phone, otp)
    }
}

class UpdateProfileUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(profile: UserProfile): Flow<DataResource<UserProfile>> {
        return authRepository.updateUserProfile(profile)
    }
}

class GetAuthSessionUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<DataResource<UserProfile>> {
        return authRepository.getCurrentUser()
    }

    fun hasActiveSession(): Boolean = authRepository.hasActiveSession()
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<DataResource<Boolean>> {
        return authRepository.logout()
    }
}
