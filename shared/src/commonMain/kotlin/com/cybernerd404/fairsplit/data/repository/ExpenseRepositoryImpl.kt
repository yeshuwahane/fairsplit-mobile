package com.cybernerd404.fairsplit.data.repository

import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.data.utils.getCurrentFormattedDate
import com.cybernerd404.fairsplit.data.utils.getCurrentFormattedTime
import com.cybernerd404.fairsplit.data.utils.safeApiCall
import com.cybernerd404.fairsplit.domain.engine.ExpenseCalculationEngine
import com.cybernerd404.fairsplit.domain.model.*
import com.cybernerd404.fairsplit.domain.repository.ExpenseRepository

import com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSource
import com.cybernerd404.fairsplit.data.remote.dto.toDto

class ExpenseRepositoryImpl(
    private val syncDataSource: RemoteSyncDataSource? = null
) : ExpenseRepository {

    override suspend fun getExpensesForEpic(epicId: String): DataResource<List<Expense>> = safeApiCall {
        try {
            val remoteList = syncDataSource?.getExpenses(epicId)
            if (remoteList != null) {
                val domainList = remoteList.map { it.toDomain() }.toMutableList()
                MockDataProvider.expensesByEpicId[epicId] = domainList
                MockDataProvider.recalculateEpicById(epicId)
                MockDataProvider.persistToLocalDatabase()
            }
        } catch (_: Exception) {}

        MockDataProvider.expensesByEpicId[epicId] ?: emptyList()
    }

    override suspend fun getRecentExpenses(limit: Int): DataResource<List<Expense>> = safeApiCall {
        MockDataProvider.expensesByEpicId.values.flatten().take(limit)
    }

    override suspend fun getExpenseById(id: String): DataResource<Expense> = safeApiCall {
        MockDataProvider.expensesByEpicId.values.flatten().firstOrNull { it.id == id }
            ?: throw Exception("Expense with id $id not found")
    }

    override suspend fun addExpense(
        epicId: String,
        title: String,
        amount: Double,
        payerId: String,
        participantIds: List<String>,
        category: ExpenseCategory,
        note: String?,
        receiptUrl: String?
    ): DataResource<Expense> = safeApiCall {
        val epic = MockDataProvider.allEpics.firstOrNull { it.id == epicId }
            ?: throw Exception("Epic with id $epicId not found")

        val payer = epic.members.firstOrNull { it.id == payerId }
            ?: epic.members.firstOrNull { it.isCurrentUser }
            ?: epic.members.first()

        val participants = epic.members.filter { participantIds.contains(it.id) }.ifEmpty { epic.members }
        val calculatedShares = ExpenseCalculationEngine.calculateEqualShares(amount, participants)

        val yourShare = calculatedShares.firstOrNull { it.member.isCurrentUser }?.shareAmount ?: 0.0
        val yourImpact = if (payer.isCurrentUser) (amount - yourShare) else -yourShare
        val currentUserMember = epic.members.firstOrNull { it.isCurrentUser }
            ?: Member(id = MockDataProvider.currentUserProfile.id, name = MockDataProvider.currentUserProfile.name, isCurrentUser = true)

        val newExpenseId = "exp_${title.lowercase().replace(" ", "_")}_${(1000..9999).random()}"
        val newExpense = Expense(
            id = newExpenseId,
            epicId = epicId,
            epicName = epic.name,
            title = title,
            amount = amount,
            paidBy = payer,
            participants = participants,
            date = getCurrentFormattedDate(),
            category = category,
            note = note,
            receiptUrl = receiptUrl,
            shares = calculatedShares,
            yourShare = yourShare,
            yourBalanceImpact = yourImpact,
            createdBy = currentUserMember,
            createdAtTime = getCurrentFormattedTime()
        )

        val list = MockDataProvider.expensesByEpicId.getOrPut(epicId) { mutableListOf() }
        list.add(0, newExpense)

        // Automatically recalculate Epic totals, member balances, and settlements
        MockDataProvider.recalculateEpicById(epicId)

        val actEntry = MockDataProvider.logActivity(
            epicId = epicId,
            epicName = epic.name,
            type = ActivityType.EXPENSE_ADDED,
            title = "Expense Added",
            description = "${currentUserMember.name} added '$title' (₹${amount.toInt()}) split with ${participants.size} members",
            amount = amount,
            actor = currentUserMember,
            receiptUrl = receiptUrl,
            expenseId = newExpenseId
        )

        try {
            syncDataSource?.addExpense(newExpense.toDto())
            syncDataSource?.addActivity(actEntry.toDto())
        } catch (_: Exception) {}

        newExpense
    }

    override suspend fun editExpense(
        expenseId: String,
        title: String,
        amount: Double,
        payerId: String,
        participantIds: List<String>,
        category: ExpenseCategory,
        note: String?,
        receiptUrl: String?
    ): DataResource<Expense> = safeApiCall {
        // Find which epic this expense belongs to
        var targetEpicId: String? = null
        var targetIndex = -1

        for ((epicId, list) in MockDataProvider.expensesByEpicId) {
            val idx = list.indexOfFirst { it.id == expenseId }
            if (idx >= 0) {
                targetEpicId = epicId
                targetIndex = idx
                break
            }
        }

        if (targetEpicId == null || targetIndex < 0) {
            throw Exception("Expense not found")
        }

        val epic = MockDataProvider.allEpics.firstOrNull { it.id == targetEpicId }
            ?: throw Exception("Epic not found")

        val payer = epic.members.firstOrNull { it.id == payerId }
            ?: epic.members.firstOrNull { it.isCurrentUser }
            ?: epic.members.first()

        val participants = epic.members.filter { participantIds.contains(it.id) }.ifEmpty { epic.members }
        val calculatedShares = ExpenseCalculationEngine.calculateEqualShares(amount, participants)

        val yourShare = calculatedShares.firstOrNull { it.member.isCurrentUser }?.shareAmount ?: 0.0
        val yourImpact = if (payer.isCurrentUser) (amount - yourShare) else -yourShare

        val oldExpense = MockDataProvider.expensesByEpicId[targetEpicId]!![targetIndex]
        val updatedExpense = oldExpense.copy(
            title = title,
            amount = amount,
            paidBy = payer,
            participants = participants,
            category = category,
            note = note,
            receiptUrl = receiptUrl ?: oldExpense.receiptUrl,
            shares = calculatedShares,
            yourShare = yourShare,
            yourBalanceImpact = yourImpact
        )

        MockDataProvider.expensesByEpicId[targetEpicId]!![targetIndex] = updatedExpense

        // Recalculate
        MockDataProvider.recalculateEpicById(targetEpicId)

        val currentUserMember = epic.members.firstOrNull { it.isCurrentUser }
            ?: Member(id = MockDataProvider.currentUserProfile.id, name = MockDataProvider.currentUserProfile.name, isCurrentUser = true)

        // Audit Trail Activity
        val actEntry = MockDataProvider.logActivity(
            epicId = targetEpicId,
            epicName = epic.name,
            type = ActivityType.EXPENSE_UPDATED,
            title = "Expense Updated",
            description = "${currentUserMember.name} updated '$title' to ₹${amount.toInt()}",
            amount = amount,
            actor = currentUserMember,
            receiptUrl = receiptUrl ?: oldExpense.receiptUrl,
            expenseId = expenseId
        )

        try {
            syncDataSource?.addExpense(updatedExpense.toDto())
            syncDataSource?.addActivity(actEntry.toDto())
        } catch (_: Exception) {}

        updatedExpense
    }

    override suspend fun deleteExpense(expenseId: String): DataResource<Boolean> = safeApiCall {
        var targetEpicId: String? = null
        var deletedExpense: Expense? = null

        for ((epicId, list) in MockDataProvider.expensesByEpicId) {
            val found = list.firstOrNull { it.id == expenseId }
            if (found != null) {
                deletedExpense = found
                list.remove(found)
                targetEpicId = epicId
                break
            }
        }

        if (targetEpicId != null && deletedExpense != null) {
            val epic = MockDataProvider.allEpics.firstOrNull { it.id == targetEpicId }
            val actor = epic?.members?.firstOrNull { it.isCurrentUser }
                ?: Member(id = MockDataProvider.currentUserProfile.id, name = MockDataProvider.currentUserProfile.name, isCurrentUser = true)
            val isSettle = deletedExpense.isSettlement

            val actEntry = MockDataProvider.logActivity(
                epicId = targetEpicId,
                epicName = epic?.name ?: "Epic",
                type = if (isSettle) ActivityType.SETTLEMENT_DELETED else ActivityType.EXPENSE_DELETED,
                title = if (isSettle) "Settlement Removed" else "Expense Deleted",
                description = "${actor.name} deleted ${if (isSettle) "settlement" else "expense"} '${deletedExpense.title}' (₹${deletedExpense.amount.toInt()})",
                amount = deletedExpense.amount,
                actor = actor
            )

            MockDataProvider.recalculateEpicById(targetEpicId)

            try {
                syncDataSource?.deleteExpense(targetEpicId, expenseId)
                syncDataSource?.addActivity(actEntry.toDto())
            } catch (_: Exception) {}
        }

        true
    }

    override suspend fun recordSettlement(
        epicId: String,
        payerId: String,
        receiverId: String,
        amount: Double,
        note: String?,
        receiptUrl: String?
    ): DataResource<Transaction> = safeApiCall {
        val epic = MockDataProvider.allEpics.firstOrNull { it.id == epicId }
            ?: throw Exception("Epic with id $epicId not found")

        val payer = epic.members.firstOrNull { it.id == payerId }
            ?: throw Exception("Payer not found in epic")

        val receiver = epic.members.firstOrNull { it.id == receiverId }
            ?: throw Exception("Receiver not found in epic")

        val currentUserMember = epic.members.firstOrNull { it.isCurrentUser }
            ?: Member(id = MockDataProvider.currentUserProfile.id, name = MockDataProvider.currentUserProfile.name, isCurrentUser = true)

        // 1. Create a settlement expense where payer paid and receiver is the sole recipient
        val settlementExpenseId = "exp_settle_${(1000..9999).random()}"
        val share = MemberShare(member = receiver, shareAmount = amount)
        val yourShare = if (receiver.isCurrentUser) amount else 0.0
        val yourImpact = when {
            payer.isCurrentUser -> amount
            receiver.isCurrentUser -> -amount
            else -> 0.0
        }

        val settlementExpense = Expense(
            id = settlementExpenseId,
            epicId = epicId,
            epicName = epic.name,
            title = "Settlement: ${payer.name} → ${receiver.name}",
            amount = amount,
            paidBy = payer,
            participants = listOf(receiver),
            date = getCurrentFormattedDate(),
            category = ExpenseCategory.OTHER,
            note = note ?: "Debt settlement payment",
            receiptUrl = receiptUrl,
            shares = listOf(share),
            yourShare = yourShare,
            yourBalanceImpact = yourImpact,
            createdBy = currentUserMember,
            createdAtTime = getCurrentFormattedTime()
        )

        val list = MockDataProvider.expensesByEpicId.getOrPut(epicId) { mutableListOf() }
        list.add(0, settlementExpense)

        // 2. Log a completed transaction in transaction history
        val txId = "tx_settle_${(1000..9999).random()}"
        val transactionTitle = if (payer.isCurrentUser) "You paid ${receiver.name}"
            else if (receiver.isCurrentUser) "${payer.name} paid You"
            else "${payer.name} paid ${receiver.name}"

        val transaction = Transaction(
            id = txId,
            title = transactionTitle,
            subtitle = note ?: "Settlement for ${epic.name}",
            amount = amount,
            epicName = epic.name,
            date = getCurrentFormattedDate(),
            time = getCurrentFormattedTime(),
            status = TransactionStatus.COMPLETED,
            fromMember = payer,
            toMember = receiver,
            epicId = epicId
        )
        MockDataProvider.allTransactions.add(0, transaction)

        // 3. Recalculate balances, settlements, and persist to LocalDatabase
        val updatedEpic = MockDataProvider.recalculateEpicById(epicId)

        // 4. Audit Trail Activity
        val actEntry = MockDataProvider.logActivity(
            epicId = epicId,
            epicName = epic.name,
            type = ActivityType.SETTLEMENT_RECORDED,
            title = "Debt Settled",
            description = "${currentUserMember.name} recorded settlement: ${payer.name} paid ${receiver.name} (₹${amount.toInt()})",
            amount = amount,
            actor = currentUserMember,
            receiptUrl = receiptUrl,
            expenseId = null
        )

        try {
            syncDataSource?.addTransaction(transaction.toDto())
        } catch (_: Exception) {}

        transaction
    }
}
