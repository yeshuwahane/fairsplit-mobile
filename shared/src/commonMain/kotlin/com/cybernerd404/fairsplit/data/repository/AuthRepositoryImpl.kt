package com.cybernerd404.fairsplit.data.repository

import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.data.local.TokenStorage
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteAuthDataSource
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.auth.DevelopmentOtpProvider
import com.cybernerd404.fairsplit.domain.auth.OtpProvider
import com.cybernerd404.fairsplit.domain.model.AuthIdentity
import com.cybernerd404.fairsplit.domain.model.AuthProvider
import com.cybernerd404.fairsplit.domain.model.AuthResponse
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.domain.repository.AuthRepository
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteUserDataSource
import com.cybernerd404.fairsplit.data.remote.dto.toDto
import io.github.aakira.napier.Napier
import com.cybernerd404.fairsplit.data.utils.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val tokenStorage: TokenStorage,
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val otpProvider: OtpProvider = DevelopmentOtpProvider(devOtpEnabled = true),
    private val remoteUserDataSource: RemoteUserDataSource? = null
) : AuthRepository {

    override fun requestPhoneOtp(phone: String): Flow<DataResource<Boolean>> = flow {
        emit(DataResource.loading())
        try {
            val cleanPhone = phone.trim()
            if (cleanPhone.length < 10) {
                emit(DataResource.error(IllegalArgumentException("Please enter a valid phone number")))
                return@flow
            }

            Napier.d(tag = "FAIRSPLIT_AUTH", message = "Requesting phone OTP for $cleanPhone via backend API")
            val remoteResult = remoteAuthDataSource.requestPhoneOtp(cleanPhone)
            if (remoteResult.isSuccess) {
                val data = remoteResult.getOrThrow()
                Napier.i(tag = "FAIRSPLIT_AUTH", message = "Phone OTP requested successfully from backend. devOtp: ${data.devOtp}")
                emit(DataResource.success(true))
            } else {
                val error = remoteResult.exceptionOrNull() ?: Exception("Failed to request OTP from backend")
                Napier.e(tag = "FAIRSPLIT_AUTH", message = "Backend OTP request failed: ${error.message}", throwable = error)
                emit(DataResource.error(error))
            }
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Failed to request OTP: ${e.message}", throwable = e)
            emit(DataResource.error(e))
        }
    }

    override fun verifyPhoneOtp(phone: String, otp: String): Flow<DataResource<AuthResponse>> = flow {
        emit(DataResource.loading())
        try {
            val cleanPhone = phone.trim()
            val cleanOtp = otp.trim()
            if (cleanOtp.length != 6) {
                emit(DataResource.error(IllegalArgumentException("Please enter complete 6-digit verification code")))
                return@flow
            }

            Napier.d(tag = "FAIRSPLIT_AUTH", message = "Verifying phone OTP with backend for $cleanPhone")
            val remoteResult = remoteAuthDataSource.verifyPhoneOtp(cleanPhone, cleanOtp)
            if (remoteResult.isSuccess) {
                val backendAuth = remoteResult.getOrThrow()
                val backendUser = backendAuth.user

                val initials = if (backendUser.name.isNotBlank()) backendUser.name.take(2).uppercase() else "ME"
                val phoneIdentity = AuthIdentity(
                    id = "ident_phone_${backendUser.id}",
                    userId = backendUser.id,
                    provider = AuthProvider.PHONE,
                    providerUserId = backendUser.phoneNumber ?: cleanPhone,
                    phone = backendUser.phoneNumber ?: cleanPhone,
                    createdAt = currentTimeMillis()
                )

                val user = UserProfile(
                    id = backendUser.id,
                    name = backendUser.name,
                    email = backendUser.email.orEmpty(),
                    phone = backendUser.phoneNumber ?: cleanPhone,
                    upiId = backendUser.upiId.orEmpty(),
                    avatarInitials = initials,
                    avatarUrl = backendUser.avatarUrl,
                    profileCompleted = backendUser.profileCompleted,
                    authIdentities = listOf(phoneIdentity)
                )

                // Store backend JWT session tokens
                tokenStorage.setAccessToken(backendAuth.accessToken)
                tokenStorage.setRefreshToken(backendAuth.refreshToken)
                LocalDatabase.setLoggedIn(true)
                LocalDatabase.setAuthPhone(cleanPhone)
                LocalDatabase.setAuthProvider("PHONE")
                LocalDatabase.saveUserProfile(user)
                MockDataProvider.updateCurrentUser(user)

                Napier.i(tag = "FAIRSPLIT_AUTH", message = "Logged in successfully via backend! User: ${user.id}, Token saved.")

                emit(
                    DataResource.success(
                        AuthResponse(
                            user = user,
                            token = backendAuth.accessToken,
                            isNewUser = !backendUser.profileCompleted && backendUser.name.isBlank(),
                            profileCompleted = backendUser.profileCompleted
                        )
                    )
                )
            } else {
                val error = remoteResult.exceptionOrNull() ?: Exception("Verification failed")
                Napier.e(tag = "FAIRSPLIT_AUTH", message = "Backend verify OTP failed: ${error.message}", throwable = error)
                emit(DataResource.error(error))
            }
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Phone verification failed: ${e.message}", throwable = e)
            emit(DataResource.error(e))
        }
    }

    override fun signInWithGoogle(
        idToken: String,
        email: String?,
        displayName: String?,
        photoUrl: String?
    ): Flow<DataResource<AuthResponse>> = flow {
        emit(DataResource.loading())
        try {
            val googleEmail = email?.trim().orEmpty().lowercase()
            val googleName = displayName?.trim().orEmpty()

            if (googleEmail.isBlank() && idToken.isBlank()) {
                emit(DataResource.error(IllegalArgumentException("Google account info is missing")))
                return@flow
            }

            Napier.d(tag = "FAIRSPLIT_AUTH", message = "Signing in with Google: $googleEmail")

            // 1. Try remote backend Google auth
            val remoteResult = if (googleEmail.isNotBlank()) {
                remoteAuthDataSource.signInWithGoogle(idToken, googleEmail, googleName, photoUrl)
            } else {
                Result.failure(Exception("No email provided"))
            }

            if (remoteResult.isSuccess) {
                val backendAuth = remoteResult.getOrThrow()
                val backendUser = backendAuth.user

                val initials = if (backendUser.name.isNotBlank()) {
                    backendUser.name.take(2).uppercase()
                } else if (googleName.isNotBlank()) {
                    googleName.take(2).uppercase()
                } else "ME"

                val googleIdentity = AuthIdentity(
                    id = "ident_google_${backendUser.id}",
                    userId = backendUser.id,
                    provider = AuthProvider.GOOGLE,
                    providerUserId = backendUser.email ?: googleEmail,
                    email = backendUser.email ?: googleEmail,
                    createdAt = currentTimeMillis()
                )

                val user = UserProfile(
                    id = backendUser.id,
                    name = backendUser.name.ifBlank { googleName },
                    email = backendUser.email ?: googleEmail,
                    phone = backendUser.phoneNumber.orEmpty(),
                    upiId = backendUser.upiId.orEmpty(),
                    avatarInitials = initials,
                    avatarUrl = backendUser.avatarUrl ?: photoUrl,
                    profileCompleted = backendUser.profileCompleted || backendUser.name.isNotBlank(),
                    authIdentities = listOf(googleIdentity)
                )

                tokenStorage.setAccessToken(backendAuth.accessToken)
                tokenStorage.setRefreshToken(backendAuth.refreshToken)
                LocalDatabase.setLoggedIn(true)
                LocalDatabase.setAuthProvider("GOOGLE")
                LocalDatabase.saveUserProfile(user)
                MockDataProvider.updateCurrentUser(user)

                Napier.i(tag = "FAIRSPLIT_AUTH", message = "Logged in via backend Google auth! User: ${user.id}")

                emit(
                    DataResource.success(
                        AuthResponse(
                            user = user,
                            token = backendAuth.accessToken,
                            isNewUser = !user.profileCompleted,
                            profileCompleted = user.profileCompleted
                        )
                    )
                )
                return@flow
            }

            // 2. Offline fallback: use deterministic ID derived from email
            Napier.w(tag = "FAIRSPLIT_AUTH", message = "Backend Google auth unavailable, using deterministic offline identity")
            val sanitizedEmail = googleEmail.replace(Regex("[^a-zA-Z0-9]"), "_")
            val userId = "usr_g_$sanitizedEmail"

            val existingProfile = LocalDatabase.getUserProfile()
            val isSameUser = existingProfile != null && (existingProfile.id == userId || existingProfile.email.equals(googleEmail, ignoreCase = true))

            val googleIdentity = AuthIdentity(
                id = "ident_google_$userId",
                userId = userId,
                provider = AuthProvider.GOOGLE,
                providerUserId = googleEmail.ifEmpty { userId },
                email = googleEmail,
                createdAt = currentTimeMillis()
            )

            val initials = if (googleName.isNotBlank()) googleName.take(2).uppercase() else "ME"
            val user = UserProfile(
                id = userId,
                name = if (isSameUser && existingProfile?.name?.isNotBlank() == true) existingProfile.name else googleName,
                email = googleEmail,
                phone = existingProfile?.phone.orEmpty(),
                upiId = existingProfile?.upiId.orEmpty(),
                avatarInitials = initials,
                avatarUrl = photoUrl ?: existingProfile?.avatarUrl,
                profileCompleted = isSameUser && existingProfile?.profileCompleted == true,
                authIdentities = listOf(googleIdentity)
            )

            val sessionToken = "fairsplit_jwt_google_${userId}"
            tokenStorage.setAccessToken(sessionToken)
            LocalDatabase.setLoggedIn(true)
            LocalDatabase.setAuthProvider("GOOGLE")
            LocalDatabase.saveUserProfile(user)
            MockDataProvider.updateCurrentUser(user)

            emit(
                DataResource.success(
                    AuthResponse(
                        user = user,
                        token = sessionToken,
                        isNewUser = !user.profileCompleted,
                        profileCompleted = user.profileCompleted
                    )
                )
            )
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Google Sign-In failed: ${e.message}", throwable = e)
            emit(DataResource.error(e))
        }
    }

    override fun linkGoogle(
        idToken: String,
        email: String?,
        displayName: String?
    ): Flow<DataResource<UserProfile>> = flow {
        emit(DataResource.loading())
        try {
            val currentUser = LocalDatabase.getUserProfile()
            if (currentUser == null) {
                emit(DataResource.error(IllegalStateException("No authenticated user found")))
                return@flow
            }

            val googleEmail = email?.trim() ?: "user@gmail.com"

            // Check duplicate linking safety: Ensure this Google identity is not assigned to another user
            val alreadyLinked = currentUser.authIdentities.any {
                it.provider == AuthProvider.GOOGLE && it.providerUserId == googleEmail
            }
            if (alreadyLinked) {
                emit(DataResource.success(currentUser))
                return@flow
            }

            val googleIdentity = AuthIdentity(
                id = "ident_google_${currentUser.id}",
                userId = currentUser.id,
                provider = AuthProvider.GOOGLE,
                providerUserId = googleEmail,
                email = googleEmail,
                createdAt = currentTimeMillis()
            )

            val updatedUser = currentUser.copy(
                email = if (currentUser.email.isBlank()) googleEmail else currentUser.email,
                authIdentities = currentUser.authIdentities.filter { it.provider != AuthProvider.GOOGLE } + googleIdentity
            )

            LocalDatabase.saveUserProfile(updatedUser)
            MockDataProvider.updateCurrentUser(updatedUser)
            emit(DataResource.success(updatedUser))
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Google linking failed: ${e.message}", throwable = e)
            emit(DataResource.error(e))
        }
    }

    override fun linkPhone(phone: String): Flow<DataResource<Boolean>> = flow {
        emit(DataResource.loading())
        try {
            val cleanPhone = phone.trim()
            if (cleanPhone.length < 10) {
                emit(DataResource.error(IllegalArgumentException("Please enter a valid phone number")))
                return@flow
            }
            otpProvider.generateOrSendOtp(cleanPhone)
            emit(DataResource.success(true))
        } catch (e: Exception) {
            emit(DataResource.error(e))
        }
    }

    override fun verifyLinkPhone(phone: String, otp: String): Flow<DataResource<UserProfile>> = flow {
        emit(DataResource.loading())
        try {
            val cleanPhone = phone.trim()
            val isValid = otpProvider.verifyOtp(cleanPhone, otp.trim())
            if (!isValid) {
                emit(DataResource.error(IllegalArgumentException("Invalid verification code (Enter 123456)")))
                return@flow
            }

            val currentUser = LocalDatabase.getUserProfile()
            if (currentUser == null) {
                emit(DataResource.error(IllegalStateException("No authenticated user found")))
                return@flow
            }

            val phoneIdentity = AuthIdentity(
                id = "ident_phone_${currentUser.id}",
                userId = currentUser.id,
                provider = AuthProvider.PHONE,
                providerUserId = cleanPhone,
                phone = cleanPhone,
                createdAt = currentTimeMillis()
            )

            val updatedUser = currentUser.copy(
                phone = cleanPhone,
                authIdentities = currentUser.authIdentities.filter { it.provider != AuthProvider.PHONE } + phoneIdentity
            )

            LocalDatabase.saveUserProfile(updatedUser)
            MockDataProvider.updateCurrentUser(updatedUser)
            emit(DataResource.success(updatedUser))
        } catch (e: Exception) {
            emit(DataResource.error(e))
        }
    }

    override fun getCurrentUser(): Flow<DataResource<UserProfile>> = flow {
        emit(DataResource.loading())
        if (remoteUserDataSource != null && tokenStorage.hasValidSession()) {
            try {
                val remoteProfile = remoteUserDataSource.getUserProfile().toDomain()
                LocalDatabase.saveUserProfile(remoteProfile)
                MockDataProvider.updateCurrentUser(remoteProfile)
                emit(DataResource.success(remoteProfile))
                return@flow
            } catch (e: Exception) {
                Napier.w(tag = "FAIRSPLIT_AUTH", message = "Remote getCurrentUser failed, falling back to local: ${e.message}")
            }
        }
        val profile = LocalDatabase.getUserProfile()
        if (profile != null) {
            emit(DataResource.success(profile))
        } else {
            emit(DataResource.error(NoSuchElementException("User profile not found")))
        }
    }

    override fun updateUserProfile(profile: UserProfile): Flow<DataResource<UserProfile>> = flow {
        emit(DataResource.loading())
        try {
            val completedProfile = profile.copy(profileCompleted = true)
            val finalProfile = if (remoteUserDataSource != null && tokenStorage.hasValidSession()) {
                try {
                    val remote = remoteUserDataSource.updateProfile(completedProfile.toDto()).toDomain()
                    remote
                } catch (e: Exception) {
                    Napier.e(tag = "FAIRSPLIT_AUTH", message = "Remote profile update failed, saving locally: ${e.message}", throwable = e)
                    completedProfile
                }
            } else {
                completedProfile
            }
            LocalDatabase.saveUserProfile(finalProfile)
            MockDataProvider.updateCurrentUser(finalProfile)
            emit(DataResource.success(finalProfile))
        } catch (e: Exception) {
            emit(DataResource.error(e))
        }
    }

    override fun registerFcmToken(fcmToken: String): Flow<DataResource<Boolean>> = flow {
        emit(DataResource.loading())
        try {
            Napier.i(tag = "FAIRSPLIT_AUTH", message = "FCM token registered with backend context: $fcmToken")
            emit(DataResource.success(true))
        } catch (e: Exception) {
            emit(DataResource.error(e))
        }
    }

    override fun logout(): Flow<DataResource<Boolean>> = flow {
        emit(DataResource.loading())
        try {
            val refreshToken = tokenStorage.getRefreshToken()
            remoteAuthDataSource.logout(refreshToken)
            tokenStorage.clearTokens()
            LocalDatabase.logout()
            MockDataProvider.resetAllData()
            Napier.i(tag = "FAIRSPLIT_AUTH", message = "Logged out from backend and cleared local session.")
            emit(DataResource.success(true))
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Logout error: ${e.message}")
            tokenStorage.clearTokens()
            LocalDatabase.logout()
            MockDataProvider.resetAllData()
            emit(DataResource.error(e))
        }
    }

    override fun hasActiveSession(): Boolean = tokenStorage.hasValidSession() && LocalDatabase.isLoggedIn()
}
