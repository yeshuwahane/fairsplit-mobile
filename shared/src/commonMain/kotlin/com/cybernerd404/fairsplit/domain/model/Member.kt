package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    val name: String,
    val avatarInitials: String = name.take(2).uppercase(),
    val avatarColorHex: Long = 0xFF6366F1,
    val avatarUrl: String? = null,
    val totalSpent: Double = 0.0,
    val balance: Double = 0.0, // positive = is owed money, negative = owes money
    val isCurrentUser: Boolean = false,
    val upiId: String? = null
)
