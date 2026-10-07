package com.cybernerd404.fairsplit.domain.repository

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.UserPreferences
import com.cybernerd404.fairsplit.domain.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(): DataResource<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): DataResource<UserProfile>
    suspend fun getUserPreferences(): DataResource<UserPreferences>
    suspend fun updateUserPreferences(preferences: UserPreferences): DataResource<UserPreferences>
}
