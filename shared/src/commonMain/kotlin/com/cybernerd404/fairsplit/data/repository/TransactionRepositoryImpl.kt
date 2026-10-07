package com.cybernerd404.fairsplit.data.repository

import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSource
import com.cybernerd404.fairsplit.data.remote.dto.toDto
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.data.utils.safeApiCall
import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.domain.repository.TransactionRepository

class TransactionRepositoryImpl(
    private val syncDataSource: RemoteSyncDataSource? = null
) : TransactionRepository {

    override suspend fun getTransactions(): DataResource<List<Transaction>> = safeApiCall {
        try {
            val remoteTxs = syncDataSource?.getTransactions()
            if (remoteTxs != null) {
                MockDataProvider.allTransactions.clear()
                MockDataProvider.allTransactions.addAll(remoteTxs.map { it.toDomain() })
                MockDataProvider.persistToLocalDatabase()
            }
        } catch (_: Exception) {}
        MockDataProvider.allTransactions
    }

    override suspend fun getTransactionById(transactionId: String): DataResource<Transaction?> = safeApiCall {
        val local = MockDataProvider.allTransactions.find { it.id == transactionId }
        if (local != null) return@safeApiCall local
        // If not in local cache, fetch from remote
        try {
            val remoteList = syncDataSource?.getTransactions() ?: emptyList()
            remoteList.forEach { dto ->
                val tx = dto.toDomain()
                val idx = MockDataProvider.allTransactions.indexOfFirst { it.id == tx.id }
                if (idx >= 0) {
                    MockDataProvider.allTransactions[idx] = tx
                } else {
                    MockDataProvider.allTransactions.add(0, tx)
                }
            }
            MockDataProvider.persistToLocalDatabase()
            MockDataProvider.allTransactions.find { it.id == transactionId }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): DataResource<Transaction> = safeApiCall {
        // 1. Update local cache
        val idx = MockDataProvider.allTransactions.indexOfFirst { it.id == transaction.id }
        if (idx >= 0) {
            MockDataProvider.allTransactions[idx] = transaction
        } else {
            MockDataProvider.allTransactions.add(0, transaction)
        }

        // Update corresponding settlement expense in MockDataProvider if present
        if (transaction.epicId.isNotEmpty()) {
            val list = MockDataProvider.expensesByEpicId[transaction.epicId]
            val expIdx = list?.indexOfFirst { it.isSettlement && it.paidBy.id == transaction.fromMember.id && it.participants.firstOrNull()?.id == transaction.toMember.id }
            if (list != null && expIdx != null && expIdx >= 0) {
                val oldExp = list[expIdx]
                list[expIdx] = oldExp.copy(
                    amount = transaction.amount,
                    note = transaction.subtitle
                )
            }
            MockDataProvider.recalculateEpicById(transaction.epicId)
        }
        MockDataProvider.persistToLocalDatabase()

        // 2. Sync with remote backend
        try {
            syncDataSource?.updateTransaction(transaction.toDto())
        } catch (_: Exception) {}

        transaction
    }

    override suspend fun deleteTransaction(transactionId: String): DataResource<Boolean> = safeApiCall {
        val tx = MockDataProvider.allTransactions.find { it.id == transactionId }
        if (tx != null) {
            MockDataProvider.allTransactions.remove(tx)
            if (tx.epicId.isNotEmpty()) {
                val list = MockDataProvider.expensesByEpicId[tx.epicId]
                list?.removeAll { it.isSettlement && it.amount == tx.amount }
                MockDataProvider.recalculateEpicById(tx.epicId)
            }
            MockDataProvider.persistToLocalDatabase()
        }

        // Always call remote backend void endpoint
        try {
            syncDataSource?.deleteTransaction(transactionId)
        } catch (_: Exception) {}

        true
    }
}
