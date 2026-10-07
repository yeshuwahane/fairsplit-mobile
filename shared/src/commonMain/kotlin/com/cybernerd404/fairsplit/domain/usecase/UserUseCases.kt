package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.UserPreferences
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.domain.repository.UserRepository

class GetUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): DataResource<UserProfile> {
        return userRepository.getUserProfile()
    }
}

class GetUserPreferencesUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): DataResource<UserPreferences> {
        return userRepository.getUserPreferences()
    }
}

class UpdateUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(profile: UserProfile): DataResource<UserProfile> {
        return userRepository.updateUserProfile(profile)
    }
}

class UpdateUserPreferencesUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(preferences: UserPreferences): DataResource<UserPreferences> {
        return userRepository.updateUserPreferences(preferences)
    }
}
