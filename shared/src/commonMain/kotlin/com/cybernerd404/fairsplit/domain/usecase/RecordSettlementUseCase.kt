package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.domain.repository.ExpenseRepository

class RecordSettlementUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        epicId: String,
        payerId: String,
        receiverId: String,
        amount: Double,
        note: String? = null,
        receiptUrl: String? = null
    ): DataResource<Transaction> {
        return expenseRepository.recordSettlement(epicId, payerId, receiverId, amount, note, receiptUrl)
    }
}
