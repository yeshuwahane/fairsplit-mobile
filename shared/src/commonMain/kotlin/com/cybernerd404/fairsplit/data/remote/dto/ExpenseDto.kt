package com.cybernerd404.fairsplit.data.remote.dto

import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory
import com.cybernerd404.fairsplit.domain.model.MemberShare
import kotlinx.serialization.Serializable

@Serializable
data class MemberShareDto(
    val member: MemberDto,
    val shareAmount: Double
) {
    fun toDomain(currentUserId: String? = null, currentUserName: String? = null): MemberShare = MemberShare(
        member = member.toDomain(currentUserId, currentUserName),
        shareAmount = shareAmount
    )
}

fun MemberShare.toDto(): MemberShareDto = MemberShareDto(
    member = member.toDto(),
    shareAmount = shareAmount
)

@Serializable
data class ExpenseDto(
    val id: String,
    val epicId: String,
    val epicName: String = "",
    val title: String,
    val amount: Double,
    val paidBy: MemberDto,
    val participants: List<MemberDto> = emptyList(),
    val date: String = "Today",
    val category: String = "FOOD_AND_DRINKS",
    val note: String? = null,
    val receiptUrl: String? = null,
    val shares: List<MemberShareDto> = emptyList(),
    val yourShare: Double = 0.0,
    val yourBalanceImpact: Double = 0.0,
    val createdBy: MemberDto? = null,
    val createdAtTime: String = "Just now"
) {
    fun toDomain(currentUserId: String? = null, currentUserName: String? = null): Expense {
        val catEnum = try {
            ExpenseCategory.valueOf(category)
        } catch (_: Exception) {
            ExpenseCategory.OTHER
        }

        val myId = currentUserId ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.id
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.id
        val myName = currentUserName ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.name
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.name

        val domainPaidBy = paidBy.toDomain(myId, myName)
        val domainParticipants = participants.map { it.toDomain(myId, myName) }
        val domainShares = shares.map { it.toDomain(myId, myName) }

        val myShare = domainShares.firstOrNull { it.member.isCurrentUser }?.shareAmount
            ?: if (domainParticipants.any { it.isCurrentUser }) {
                if (domainParticipants.isNotEmpty()) amount / domainParticipants.size else 0.0
            } else 0.0

        val myImpact = when {
            domainPaidBy.isCurrentUser -> (amount - myShare)
            domainParticipants.any { it.isCurrentUser } -> -myShare
            else -> 0.0
        }

        return Expense(
            id = id,
            epicId = epicId,
            epicName = epicName,
            title = title,
            amount = amount,
            paidBy = domainPaidBy,
            participants = domainParticipants,
            date = date,
            category = catEnum,
            note = note,
            receiptUrl = receiptUrl,
            shares = domainShares,
            yourShare = myShare,
            yourBalanceImpact = myImpact,
            createdBy = createdBy?.toDomain(myId, myName) ?: domainPaidBy,
            createdAtTime = createdAtTime
        )
    }
}

fun Expense.toDto(): ExpenseDto = ExpenseDto(
    id = id,
    epicId = epicId,
    epicName = epicName,
    title = title,
    amount = amount,
    paidBy = paidBy.toDto(),
    participants = participants.map { it.toDto() },
    date = date,
    category = category.name,
    note = note,
    receiptUrl = receiptUrl,
    shares = shares.map { it.toDto() },
    yourShare = yourShare,
    yourBalanceImpact = yourBalanceImpact,
    createdBy = createdBy.toDto(),
    createdAtTime = createdAtTime
)
