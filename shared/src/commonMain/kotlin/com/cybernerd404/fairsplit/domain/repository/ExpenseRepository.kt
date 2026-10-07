package com.cybernerd404.fairsplit.domain.repository

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory
import com.cybernerd404.fairsplit.domain.model.Transaction

interface ExpenseRepository {
    suspend fun getExpensesForEpic(epicId: String): DataResource<List<Expense>>
    suspend fun getRecentExpenses(limit: Int = 10): DataResource<List<Expense>>
    suspend fun getExpenseById(id: String): DataResource<Expense>
    suspend fun addExpense(
        epicId: String,
        title: String,
        amount: Double,
        payerId: String,
        participantIds: List<String>,
        category: ExpenseCategory,
        note: String?,
        receiptUrl: String?
    ): DataResource<Expense>
    suspend fun editExpense(
        expenseId: String,
        title: String,
        amount: Double,
        payerId: String,
        participantIds: List<String>,
        category: ExpenseCategory,
        note: String?,
        receiptUrl: String?
    ): DataResource<Expense>
    suspend fun deleteExpense(expenseId: String): DataResource<Boolean>
    suspend fun recordSettlement(
        epicId: String,
        payerId: String,
        receiverId: String,
        amount: Double,
        note: String?,
        receiptUrl: String? = null
    ): DataResource<Transaction>
}

