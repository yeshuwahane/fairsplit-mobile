package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED,
    VOIDED,
    CANCELLED
}

@Serializable
data class Transaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val epicName: String,
    val date: String, // e.g. "Today", "Yesterday", "24 Sep"
    val time: String = "4:30 PM",
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val fromMember: Member,
    val toMember: Member,
    val epicId: String = ""
)
