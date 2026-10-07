package com.cybernerd404.fairsplit.data.remote.dto

import com.cybernerd404.fairsplit.domain.model.UserPreferences
import com.cybernerd404.fairsplit.domain.model.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferencesDto(
    val currencySymbol: String = "₹",
    val defaultSplitPreference: String = "Equally",
    val notificationsEnabled: Boolean = true,
    val isDarkMode: Boolean = true
) {
    fun toDomain(): UserPreferences = UserPreferences(
        currencySymbol = currencySymbol,
        defaultSplitPreference = defaultSplitPreference,
        notificationsEnabled = notificationsEnabled,
        isDarkMode = isDarkMode
    )
}

fun UserPreferences.toDto(): UserPreferencesDto = UserPreferencesDto(
    currencySymbol = currencySymbol,
    defaultSplitPreference = defaultSplitPreference,
    notificationsEnabled = notificationsEnabled,
    isDarkMode = isDarkMode
)

@Serializable
data class UserProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val upiId: String = "",
    val avatarInitials: String = "ME",
    val avatarUrl: String? = null,
    val profileCompleted: Boolean = false,
    val youOwe: Double = 0.0,
    val othersOweYou: Double = 0.0,
    val netBalance: Double = 0.0
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id,
        name = name,
        email = email,
        phone = phone,
        upiId = upiId,
        avatarInitials = avatarInitials,
        avatarUrl = avatarUrl,
        profileCompleted = profileCompleted,
        youOwe = youOwe,
        othersOweYou = othersOweYou,
        netBalance = netBalance
    )
}

fun UserProfile.toDto(): UserProfileDto = UserProfileDto(
    id = id,
    name = name,
    email = email,
    phone = phone,
    upiId = upiId,
    avatarInitials = avatarInitials,
    avatarUrl = avatarUrl,
    profileCompleted = profileCompleted,
    youOwe = youOwe,
    othersOweYou = othersOweYou,
    netBalance = netBalance
)
