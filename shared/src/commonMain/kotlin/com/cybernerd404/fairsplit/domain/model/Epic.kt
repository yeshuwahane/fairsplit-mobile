package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EpicStatus {
    ACTIVE,
    SETTLED
}

@Serializable
data class Epic(
    val id: String,
    val name: String,
    val description: String = "",
    val totalSpent: Double,
    val memberCount: Int,
    val members: List<Member>,
    val yourSpent: Double,
    val yourBalance: Double, // positive = you get, negative = you owe, 0 = settled
    val status: EpicStatus = EpicStatus.ACTIVE,
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val createdAt: String = "Sep 2026",
    val inviteCode: String = ""
)
