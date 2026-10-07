package com.cybernerd404.fairsplit.data.remote.dto

import com.cybernerd404.fairsplit.domain.model.CategoryBreakdown
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.EpicStatus
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory
import kotlinx.serialization.Serializable

@Serializable
data class CategoryBreakdownDto(
    val category: String,
    val amount: Double,
    val percentage: Float
) {
    fun toDomain(): CategoryBreakdown {
        val catEnum = try {
            ExpenseCategory.valueOf(category)
        } catch (_: Exception) {
            ExpenseCategory.OTHER
        }
        return CategoryBreakdown(
            category = catEnum,
            amount = amount,
            percentage = percentage
        )
    }
}

fun CategoryBreakdown.toDto(): CategoryBreakdownDto = CategoryBreakdownDto(
    category = category.name,
    amount = amount,
    percentage = percentage
)

@Serializable
data class EpicDto(
    val id: String,
    val name: String,
    val description: String = "",
    val totalSpent: Double = 0.0,
    val memberCount: Int = 0,
    val members: List<MemberDto> = emptyList(),
    val yourSpent: Double = 0.0,
    val yourBalance: Double = 0.0,
    val status: String = "ACTIVE",
    val categoryBreakdowns: List<CategoryBreakdownDto> = emptyList(),
    val recentExpenses: List<ExpenseDto> = emptyList(),
    val settlements: List<SettlementDto> = emptyList(),
    val createdAt: String = "Sep 2026",
    val inviteCode: String = ""
) {
    fun toDomain(currentUserId: String? = null, currentUserName: String? = null): Epic {
        val myId = currentUserId ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.id
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.id
        val myName = currentUserName ?: com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.name
            ?: com.cybernerd404.fairsplit.data.local.MockDataProvider.currentUserProfile.name

        val domainMembers = members.map { it.toDomain(myId, myName) }
        val domainExpenses = recentExpenses.map { it.toDomain(myId, myName) }

        val userMember = domainMembers.firstOrNull { it.isCurrentUser }
        val userSpent = userMember?.totalSpent ?: 0.0
        val userBalance = userMember?.balance ?: 0.0

        return Epic(
            id = id,
            name = name,
            description = description,
            totalSpent = totalSpent,
            memberCount = if (memberCount > 0) memberCount else domainMembers.size,
            members = domainMembers,
            yourSpent = userSpent,
            yourBalance = userBalance,
            status = if (status.equals("SETTLED", ignoreCase = true)) EpicStatus.SETTLED else EpicStatus.ACTIVE,
            categoryBreakdowns = categoryBreakdowns.map { it.toDomain() },
            recentExpenses = domainExpenses,
            settlements = settlements.map { it.toDomain(myId, myName) },
            createdAt = createdAt,
            inviteCode = inviteCode
        )
    }
}

fun Epic.toDto(): EpicDto = EpicDto(
    id = id,
    name = name,
    description = description,
    totalSpent = totalSpent,
    memberCount = memberCount,
    members = members.map { it.toDto() },
    yourSpent = yourSpent,
    yourBalance = yourBalance,
    status = status.name,
    categoryBreakdowns = categoryBreakdowns.map { it.toDto() },
    recentExpenses = recentExpenses.map { it.toDto() },
    settlements = settlements.map { it.toDto() },
    createdAt = createdAt,
    inviteCode = inviteCode
)
