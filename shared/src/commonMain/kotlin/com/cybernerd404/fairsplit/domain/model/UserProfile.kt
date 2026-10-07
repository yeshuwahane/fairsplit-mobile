package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AuthProvider {
    PHONE,
    GOOGLE
}

@Serializable
data class AuthIdentity(
    val id: String,
    val userId: String,
    val provider: AuthProvider,
    val providerUserId: String, // Phone number e.g. "+919876543210" or Google Sub ID
    val email: String? = null,
    val phone: String? = null,
    val createdAt: Long = 0L
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val upiId: String = "",
    val avatarInitials: String = "",
    val avatarUrl: String? = null,
    val profileCompleted: Boolean = false,
    val authIdentities: List<AuthIdentity> = emptyList(),
    val youOwe: Double = 0.0,
    val othersOweYou: Double = 0.0,
    val netBalance: Double = 0.0,
    val createdAt: Long = 0L
)

@Serializable
data class UserPreferences(
    val currencySymbol: String = "₹",
    val defaultSplitPreference: String = "Equally",
    val notificationsEnabled: Boolean = true,
    val isDarkMode: Boolean = true
)

@Serializable
data class AuthResponse(
    val user: UserProfile,
    val token: String,
    val isNewUser: Boolean,
    val profileCompleted: Boolean
)
