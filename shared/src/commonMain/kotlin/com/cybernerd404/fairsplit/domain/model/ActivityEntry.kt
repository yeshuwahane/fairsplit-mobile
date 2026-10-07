package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType {
    EXPENSE_ADDED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    SETTLEMENT_RECORDED,
    SETTLEMENT_DELETED,
    EPIC_CREATED,
    MEMBER_ADDED
}

@Serializable
data class ActivityEntry(
    val id: String,
    val epicId: String,
    val epicName: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val amount: Double? = null,
    val actor: Member,
    val timestamp: String = "Just now",
    val date: String = "Today",
    val receiptUrl: String? = null,
    val expenseId: String? = null
)
