package com.cybernerd404.fairsplit.domain.repository

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(): DataResource<List<Transaction>>
    suspend fun getTransactionById(transactionId: String): DataResource<Transaction?>
    suspend fun updateTransaction(transaction: Transaction): DataResource<Transaction>
    suspend fun deleteTransaction(transactionId: String): DataResource<Boolean>
}
