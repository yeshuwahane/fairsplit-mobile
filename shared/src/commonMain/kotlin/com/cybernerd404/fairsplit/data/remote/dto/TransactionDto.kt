package com.cybernerd404.fairsplit.data.remote.dto

import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.domain.model.TransactionStatus
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val epicName: String,
    val date: String,
    val time: String = "4:30 PM",
    val status: String = "COMPLETED",
    val fromMember: MemberDto,
    val toMember: MemberDto,
    val epicId: String = ""
) {
    fun toDomain(currentUserId: String? = null, currentUserName: String? = null): Transaction {
        val myId = currentUserId ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.id
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.id
        val myName = currentUserName ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.name
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.name

        val from = fromMember.toDomain(myId, myName)
        val to = toMember.toDomain(myId, myName)

        val txTitle = when {
            from.isCurrentUser -> "You paid ${to.name}"
            to.isCurrentUser -> "${from.name} paid You"
            else -> "${from.name} paid ${to.name}"
        }

        return Transaction(
            id = id,
            title = txTitle,
            subtitle = subtitle,
            amount = amount,
            epicName = epicName,
            date = date,
            time = time,
            status = try {
                TransactionStatus.valueOf(status)
            } catch (_: Exception) {
                TransactionStatus.COMPLETED
            },
            fromMember = from,
            toMember = to,
            epicId = epicId
        )
    }
}

fun Transaction.toDto(): TransactionDto = TransactionDto(
    id = id,
    title = title,
    subtitle = subtitle,
    amount = amount,
    epicName = epicName,
    date = date,
    time = time,
    status = status.name,
    fromMember = fromMember.toDto(),
    toMember = toMember.toDto(),
    epicId = epicId
)
