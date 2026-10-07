package com.cybernerd404.fairsplit.data.remote.dto

import com.cybernerd404.fairsplit.domain.model.ActivityEntry
import com.cybernerd404.fairsplit.domain.model.ActivityType
import kotlinx.serialization.Serializable

@Serializable
data class ActivityDto(
    val id: String,
    val epicId: String,
    val epicName: String,
    val type: String,
    val title: String,
    val description: String,
    val amount: Double? = null,
    val actor: MemberDto,
    val timestamp: String = "Just now",
    val date: String = "Today",
    val receiptUrl: String? = null,
    val expenseId: String? = null
) {
    fun toDomain(currentUserId: String? = null, currentUserName: String? = null): ActivityEntry {
        val myId = currentUserId ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.id
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.id
        val myName = currentUserName ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.name
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.name

        return ActivityEntry(
            id = id,
            epicId = epicId,
            epicName = epicName,
            type = when (type) {
                "MEMBER_JOINED" -> ActivityType.MEMBER_ADDED
                else -> try {
                    ActivityType.valueOf(type)
                } catch (_: Exception) {
                    ActivityType.EXPENSE_ADDED
                }
            },
            title = title,
            description = description,
            amount = amount,
            actor = actor.toDomain(myId, myName),
            timestamp = timestamp,
            date = date,
            receiptUrl = receiptUrl,
            expenseId = expenseId
        )
    }
}

fun ActivityEntry.toDto(): ActivityDto = ActivityDto(
    id = id,
    epicId = epicId,
    epicName = epicName,
    type = type.name,
    title = title,
    description = description,
    amount = amount,
    actor = actor.toDto(),
    timestamp = timestamp,
    date = date,
    receiptUrl = receiptUrl,
    expenseId = expenseId
)
