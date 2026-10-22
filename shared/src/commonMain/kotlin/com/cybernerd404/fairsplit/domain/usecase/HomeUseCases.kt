package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.EpicStatus
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.domain.engine.ExpenseCalculationEngine
import com.cybernerd404.fairsplit.domain.repository.EpicRepository
import com.cybernerd404.fairsplit.domain.repository.ExpenseRepository
import com.cybernerd404.fairsplit.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class HomeOverviewData(
    val userProfile: UserProfile,
    val activeEpics: List<Epic>,
    val recentExpenses: List<Expense>
)

class GetHomeOverviewUseCase(
    private val userRepository: UserRepository,
    private val epicRepository: EpicRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(): DataResource<HomeOverviewData> = coroutineScope {
        val userDeferred = async { userRepository.getUserProfile() }
        val epicsDeferred = async { epicRepository.getEpics(EpicStatus.ACTIVE) }
        val expensesDeferred = async { expenseRepository.getRecentExpenses(5) }

        val userResult = userDeferred.await()
        val epicsResult = epicsDeferred.await()
        val expensesResult = expensesDeferred.await()

        val rawUser = userResult.data ?: UserProfile(
            id = "usr_me",
            name = "You",
            email = "",
            phone = "",
            upiId = "",
            avatarInitials = "ME",
            youOwe = 0.0,
            othersOweYou = 0.0,
            netBalance = 0.0
        )
        val epics = (epicsResult.data ?: emptyList()).distinctBy {
            if (it.inviteCode.isNotBlank()) it.inviteCode.lowercase() else it.id
        }
        val expenses = expensesResult.data ?: emptyList()

        val calculatedUser = ExpenseCalculationEngine.calculateUserSummary(epics, rawUser)
        LocalDatabase.saveUserProfile(calculatedUser)
        MockDataProvider.updateCurrentUser(calculatedUser)

        DataResource.success(
            HomeOverviewData(
                userProfile = calculatedUser,
                activeEpics = epics,
                recentExpenses = expenses
            )
        )
    }
}
