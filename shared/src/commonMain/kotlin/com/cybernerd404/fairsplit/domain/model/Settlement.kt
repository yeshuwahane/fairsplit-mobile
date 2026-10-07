package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SettlementStatus {
    PENDING,
    SETTLED
}

@Serializable
data class Settlement(
    val id: String,
    val fromMember: Member,
    val toMember: Member,
    val amount: Double,
    val status: SettlementStatus = SettlementStatus.PENDING,
    val isSettled: Boolean = status == SettlementStatus.SETTLED
)
