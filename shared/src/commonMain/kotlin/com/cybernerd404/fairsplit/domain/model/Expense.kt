package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MemberShare(
    val member: Member,
    val shareAmount: Double
)

@Serializable
data class Expense(
    val id: String,
    val epicId: String,
    val epicName: String,
    val title: String,
    val amount: Double,
    val paidBy: Member,
    val participants: List<Member>,
    val date: String, // e.g. "Today", "Yesterday", "28 Sep"
    val category: ExpenseCategory,
    val note: String? = null,
    val receiptUrl: String? = null,
    val shares: List<MemberShare> = emptyList(),
    val yourShare: Double = 0.0,
    val yourBalanceImpact: Double = 0.0, // positive = you paid, others owe you; negative = you owe
    val createdBy: Member = paidBy,
    val createdAtTime: String = "Just now"
) {
    val isSettlement: Boolean
        get() = title.startsWith("Settlement:", ignoreCase = true) ||
                (category == ExpenseCategory.OTHER && participants.size == 1 && participants[0].id != paidBy.id)
}

