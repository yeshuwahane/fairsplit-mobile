package com.cybernerd404.fairsplit.data.remote.dto

import com.cybernerd404.fairsplit.domain.model.Member
import kotlinx.serialization.Serializable

@Serializable
data class MemberDto(
    val id: String,
    val name: String,
    val avatarInitials: String = "",
    val avatarColorHex: Long = 0xFF6366F1,
    val avatarUrl: String? = null,
    val totalSpent: Double = 0.0,
    val balance: Double = 0.0,
    val isCurrentUser: Boolean = false,
    val upiId: String? = null
) {
    fun toDomain(currentUserId: String? = null, currentUserName: String? = null): Member {
        val myId = currentUserId ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.id
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.id
        val myName = currentUserName ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.name
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.name

        val isMe = this.isCurrentUser ||
                (myId.isNotBlank() && id == myId) ||
                (myName.isNotBlank() && name.equals(myName, ignoreCase = true))

        val resolvedUpi = if (isMe && upiId.isNullOrBlank()) {
            com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.upiId?.ifBlank { null }
        } else {
            upiId
        }

        return Member(
            id = id,
            name = name,
            avatarInitials = if (avatarInitials.isNotBlank()) avatarInitials else name.take(2).uppercase(),
            avatarColorHex = avatarColorHex,
            avatarUrl = avatarUrl,
            totalSpent = totalSpent,
            balance = balance,
            isCurrentUser = isMe,
            upiId = resolvedUpi
        )
    }
}

fun Member.toDto(): MemberDto = MemberDto(
    id = id,
    name = name,
    avatarInitials = avatarInitials,
    avatarColorHex = avatarColorHex,
    avatarUrl = avatarUrl,
    totalSpent = totalSpent,
    balance = balance,
    isCurrentUser = isCurrentUser,
    upiId = upiId
)
