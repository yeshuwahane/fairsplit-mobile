package com.cybernerd404.fairsplit.data.remote.datasource

import com.cybernerd404.fairsplit.data.remote.dto.*

/**
 * Architectural abstractions for remote REST endpoints.
 * These define the boundary for future Ktor HTTP network calls.
 */

interface RemoteEpicDataSource {
    suspend fun getEpics(): List<EpicDto>
    suspend fun getEpicById(id: String): EpicDto
    suspend fun createEpic(epic: EpicDto): EpicDto
    suspend fun updateEpic(epic: EpicDto): EpicDto
    suspend fun deleteEpic(id: String): Boolean
    suspend fun addMember(epicId: String, member: MemberDto): EpicDto
}

interface RemoteExpenseDataSource {
    suspend fun getExpenses(epicId: String): List<ExpenseDto>
    suspend fun getExpenseById(id: String): ExpenseDto
    suspend fun createExpense(expense: ExpenseDto): ExpenseDto
    suspend fun updateExpense(expense: ExpenseDto): ExpenseDto
    suspend fun deleteExpense(id: String): Boolean
}

interface RemoteTransactionDataSource {
    suspend fun getTransactions(): List<TransactionDto>
    suspend fun getTransactionById(id: String): TransactionDto
    suspend fun recordTransaction(transaction: TransactionDto): TransactionDto
}

interface RemoteActivityDataSource {
    suspend fun getActivities(): List<ActivityDto>
    suspend fun getActivitiesByEpic(epicId: String): List<ActivityDto>
}

interface RemoteUserDataSource {
    suspend fun getUserProfile(): UserProfileDto
    suspend fun updateProfile(profile: UserProfileDto): UserProfileDto
    suspend fun getUserPreferences(): UserPreferencesDto
    suspend fun updatePreferences(preferences: UserPreferencesDto): UserPreferencesDto
}

interface RemoteNotificationDataSource {
    suspend fun registerDeviceToken(registration: DeviceRegistrationDto): Boolean
    suspend fun unregisterDeviceToken(token: String): Boolean
}
