package com.cybernerd404.fairsplit.domain.repository

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.AuthResponse
import com.cybernerd404.fairsplit.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun requestPhoneOtp(phone: String): Flow<DataResource<Boolean>>
    fun verifyPhoneOtp(phone: String, otp: String): Flow<DataResource<AuthResponse>>
    fun signInWithGoogle(idToken: String, email: String?, displayName: String?, photoUrl: String?): Flow<DataResource<AuthResponse>>
    fun linkGoogle(idToken: String, email: String?, displayName: String?): Flow<DataResource<UserProfile>>
    fun linkPhone(phone: String): Flow<DataResource<Boolean>>
    fun verifyLinkPhone(phone: String, otp: String): Flow<DataResource<UserProfile>>
    fun getCurrentUser(): Flow<DataResource<UserProfile>>
    fun updateUserProfile(profile: UserProfile): Flow<DataResource<UserProfile>>
    fun registerFcmToken(fcmToken: String): Flow<DataResource<Boolean>>
    fun logout(): Flow<DataResource<Boolean>>
    fun hasActiveSession(): Boolean
}
