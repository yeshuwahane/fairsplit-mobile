package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory
import com.cybernerd404.fairsplit.domain.repository.ExpenseRepository

class GetExpensesForEpicUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(epicId: String): DataResource<List<Expense>> {
        return expenseRepository.getExpensesForEpic(epicId)
    }
}

class GetExpenseDetailUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(expenseId: String): DataResource<Expense> {
        return expenseRepository.getExpenseById(expenseId)
    }
}

class AddExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(
        epicId: String,
        title: String,
        amount: Double,
        payerId: String,
        participantIds: List<String>,
        category: ExpenseCategory,
        note: String?,
        receiptUrl: String?
    ): DataResource<Expense> {
        return expenseRepository.addExpense(
            epicId = epicId,
            title = title,
            amount = amount,
            payerId = payerId,
            participantIds = participantIds,
            category = category,
            note = note,
            receiptUrl = receiptUrl
        )
    }
}

class EditExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(
        expenseId: String,
        title: String,
        amount: Double,
        payerId: String,
        participantIds: List<String>,
        category: ExpenseCategory,
        note: String?,
        receiptUrl: String?
    ): DataResource<Expense> {
        return expenseRepository.editExpense(
            expenseId = expenseId,
            title = title,
            amount = amount,
            payerId = payerId,
            participantIds = participantIds,
            category = category,
            note = note,
            receiptUrl = receiptUrl
        )
    }
}

class DeleteExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(expenseId: String): DataResource<Boolean> {
        return expenseRepository.deleteExpense(expenseId)
    }
}
