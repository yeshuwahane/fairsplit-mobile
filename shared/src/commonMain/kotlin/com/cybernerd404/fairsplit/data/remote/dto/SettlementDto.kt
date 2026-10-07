package com.cybernerd404.fairsplit.data.remote.dto

import com.cybernerd404.fairsplit.domain.model.Settlement
import com.cybernerd404.fairsplit.domain.model.SettlementStatus
import kotlinx.serialization.Serializable

@Serializable
data class SettlementDto(
    val id: String,
    val fromMember: MemberDto,
    val toMember: MemberDto,
    val amount: Double,
    val status: String = "PENDING"
) {
    fun toDomain(currentUserId: String? = null, currentUserName: String? = null): Settlement = Settlement(
        id = id,
        fromMember = fromMember.toDomain(currentUserId, currentUserName),
        toMember = toMember.toDomain(currentUserId, currentUserName),
        amount = amount,
        status = if (status.equals("SETTLED", ignoreCase = true)) SettlementStatus.SETTLED else SettlementStatus.PENDING
    )
}

fun Settlement.toDto(): SettlementDto = SettlementDto(
    id = id,
    fromMember = fromMember.toDto(),
    toMember = toMember.toDto(),
    amount = amount,
    status = status.name
)
