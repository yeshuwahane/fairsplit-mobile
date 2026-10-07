package com.cybernerd404.fairsplit.data.repository

import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.data.local.TokenStorage
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteUserDataSource
import com.cybernerd404.fairsplit.data.remote.dto.toDto
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.data.utils.safeApiCall
import com.cybernerd404.fairsplit.domain.model.UserPreferences
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.domain.repository.UserRepository
import io.github.aakira.napier.Napier

class UserRepositoryImpl(
    private val remoteUserDataSource: RemoteUserDataSource,
    private val tokenStorage: TokenStorage
) : UserRepository {

    override suspend fun getUserProfile(): DataResource<UserProfile> = safeApiCall {
        if (tokenStorage.hasValidSession()) {
            try {
                val remoteProfile = remoteUserDataSource.getUserProfile().toDomain()
                LocalDatabase.saveUserProfile(remoteProfile)
                MockDataProvider.updateCurrentUser(remoteProfile)
                return@safeApiCall remoteProfile
            } catch (e: Exception) {
                Napier.w(tag = "FAIRSPLIT_PROFILE", message = "Remote getUserProfile failed, falling back to local: ${e.message}")
            }
        }
        LocalDatabase.getUserProfile() ?: MockDataProvider.currentUserProfile
    }

    override suspend fun updateUserProfile(profile: UserProfile): DataResource<UserProfile> = safeApiCall {
        val toUpdate = profile.copy(profileCompleted = true)
        val finalProfile = if (tokenStorage.hasValidSession()) {
            try {
                val remoteProfile = remoteUserDataSource.updateProfile(toUpdate.toDto()).toDomain()
                remoteProfile
            } catch (e: Exception) {
                Napier.e(tag = "FAIRSPLIT_PROFILE", message = "Remote updateUserProfile failed, saving locally: ${e.message}", throwable = e)
                toUpdate
            }
        } else {
            toUpdate
        }
        LocalDatabase.saveUserProfile(finalProfile)
        MockDataProvider.updateCurrentUser(finalProfile)
        finalProfile
    }

    override suspend fun getUserPreferences(): DataResource<UserPreferences> = safeApiCall {
        MockDataProvider.currentUserPreferences
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences): DataResource<UserPreferences> = safeApiCall {
        MockDataProvider.currentUserPreferences = preferences
        preferences
    }
}
