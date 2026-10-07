package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.domain.repository.TransactionRepository

class GetTransactionsUseCase(private val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(): DataResource<List<Transaction>> {
        return transactionRepository.getTransactions()
    }
}

class GetTransactionByIdUseCase(private val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(transactionId: String): DataResource<Transaction?> {
        return transactionRepository.getTransactionById(transactionId)
    }
}

class UpdateTransactionUseCase(private val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction): DataResource<Transaction> {
        return transactionRepository.updateTransaction(transaction)
    }
}

class DeleteTransactionUseCase(private val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(transactionId: String): DataResource<Boolean> {
        return transactionRepository.deleteTransaction(transactionId)
    }
}
