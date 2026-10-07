package com.cybernerd404.fairsplit.domain.engine

import com.cybernerd404.fairsplit.domain.model.*
import kotlin.math.abs
import kotlin.math.min

/**
 * Pure domain calculation engine for offline expense splitting, balance calculation,
 * deterministic settlement generation, and category breakdowns.
 * Uses paise integer arithmetic for precision.
 */
object ExpenseCalculationEngine {

    /**
     * Calculates equal shares for a given expense amount among participants.
     */
    fun calculateEqualShares(amount: Double, participants: List<Member>): List<MemberShare> {
        if (participants.isEmpty() || amount <= 0.0) return emptyList()

        val totalPaise = Money.toPaise(amount)
        val sharePaiseList = Money.splitEqually(totalPaise, participants.size)

        return participants.mapIndexed { index, member ->
            MemberShare(
                member = member,
                shareAmount = Money.toRupees(sharePaiseList[index])
            )
        }
    }

    /**
     * Calculates each member's total spending (amount actually paid) and net balance
     * (amount paid MINUS amount owed/consumed) across all expenses in an Epic.
     */
    fun calculateMemberBalances(members: List<Member>, expenses: List<Expense>): List<Member> {
        val paidMapPaise = mutableMapOf<String, Long>()
        val owedMapPaise = mutableMapOf<String, Long>()

        members.forEach {
            paidMapPaise[it.id] = 0L
            owedMapPaise[it.id] = 0L
        }

        expenses.forEach { expense ->
            val payerId = expense.paidBy.id
            val expensePaise = Money.toPaise(expense.amount)
            paidMapPaise[payerId] = (paidMapPaise[payerId] ?: 0L) + expensePaise

            // Sum up participant shares
            val shares = if (expense.shares.isNotEmpty()) {
                expense.shares
            } else {
                calculateEqualShares(expense.amount, expense.participants)
            }

            shares.forEach { share ->
                val participantId = share.member.id
                val sharePaise = Money.toPaise(share.shareAmount)
                owedMapPaise[participantId] = (owedMapPaise[participantId] ?: 0L) + sharePaise
            }
        }

        return members.map { member ->
            val paidPaise = paidMapPaise[member.id] ?: 0L
            val owedPaise = owedMapPaise[member.id] ?: 0L
            val netBalancePaise = paidPaise - owedPaise

            member.copy(
                totalSpent = Money.toRupees(paidPaise),
                balance = Money.toRupees(netBalancePaise)
            )
        }
    }

    /**
     * Calculates total spending grouped by category and percentage share.
     */
    fun calculateCategoryBreakdowns(expenses: List<Expense>): List<CategoryBreakdown> {
        if (expenses.isEmpty()) return emptyList()

        val categoryTotalsPaise = mutableMapOf<ExpenseCategory, Long>()
        var epicTotalPaise = 0L

        expenses.forEach { expense ->
            val amountPaise = Money.toPaise(expense.amount)
            categoryTotalsPaise[expense.category] = (categoryTotalsPaise[expense.category] ?: 0L) + amountPaise
            epicTotalPaise += amountPaise
        }

        if (epicTotalPaise <= 0L) return emptyList()

        return categoryTotalsPaise
            .filter { it.value > 0L }
            .map { (category, totalPaise) ->
                val amount = Money.toRupees(totalPaise)
                val percentage = (totalPaise.toFloat() / epicTotalPaise.toFloat())
                CategoryBreakdown(category, amount, percentage)
            }
            .sortedByDescending { it.amount }
    }

    /**
     * Generates a deterministic minimal list of settlements (who pays whom) from member balances.
     * Uses greedy debt-settlement algorithm on paise units.
     */
    fun calculateSettlements(members: List<Member>): List<Settlement> {
        data class MemberDebt(val member: Member, var balancePaise: Long)

        val debtors = mutableListOf<MemberDebt>()
        val creditors = mutableListOf<MemberDebt>()

        members.forEach { member ->
            val balancePaise = Money.toPaise(member.balance)
            if (balancePaise < 0L) {
                debtors.add(MemberDebt(member, abs(balancePaise)))
            } else if (balancePaise > 0L) {
                creditors.add(MemberDebt(member, balancePaise))
            }
        }

        // Sort to settle largest debts first
        debtors.sortByDescending { it.balancePaise }
        creditors.sortByDescending { it.balancePaise }

        val settlements = mutableListOf<Settlement>()
        var debtorIdx = 0
        var creditorIdx = 0
        var settlementIdCounter = 1

        while (debtorIdx < debtors.size && creditorIdx < creditors.size) {
            val debtor = debtors[debtorIdx]
            val creditor = creditors[creditorIdx]

            val settleAmountPaise = min(debtor.balancePaise, creditor.balancePaise)
            if (settleAmountPaise > 0L) {
                settlements.add(
                    Settlement(
                        id = "st_$settlementIdCounter",
                        fromMember = debtor.member,
                        toMember = creditor.member,
                        amount = Money.toRupees(settleAmountPaise),
                        status = SettlementStatus.PENDING
                    )
                )
                settlementIdCounter++

                debtor.balancePaise -= settleAmountPaise
                creditor.balancePaise -= settleAmountPaise
            }

            if (debtor.balancePaise == 0L) debtorIdx++
            if (creditor.balancePaise == 0L) creditorIdx++
        }

        return settlements
    }

    /**
     * Fully recalculates all metrics of an Epic (members, balances, totals, categories, settlements).
     */
    fun recalculateEpic(epic: Epic, expenses: List<Expense>): Epic {
        val updatedMembers = calculateMemberBalances(epic.members, expenses)
        val totalSpentPaise = expenses.sumOf { Money.toPaise(it.amount) }
        val categoryBreakdowns = calculateCategoryBreakdowns(expenses)
        val settlements = calculateSettlements(updatedMembers)

        val currentUserMember = updatedMembers.firstOrNull { it.isCurrentUser }
        val yourSpent = currentUserMember?.totalSpent ?: 0.0
        val yourBalance = currentUserMember?.balance ?: 0.0

        return epic.copy(
            totalSpent = Money.toRupees(totalSpentPaise),
            memberCount = updatedMembers.size,
            members = updatedMembers,
            yourSpent = yourSpent,
            yourBalance = yourBalance,
            categoryBreakdowns = categoryBreakdowns,
            recentExpenses = expenses,
            settlements = settlements
        )
    }

    /**
     * Aggregates financial overview for the current user across all active epics.
     */
    fun calculateUserSummary(epics: List<Epic>, currentUser: UserProfile): UserProfile {
        var youOwePaise = 0L
        var othersOweYouPaise = 0L

        epics.filter { it.status == EpicStatus.ACTIVE }.forEach { epic ->
            val balancePaise = Money.toPaise(epic.yourBalance)
            if (balancePaise < 0L) {
                youOwePaise += abs(balancePaise)
            } else if (balancePaise > 0L) {
                othersOweYouPaise += balancePaise
            }
        }

        val netBalancePaise = othersOweYouPaise - youOwePaise

        return currentUser.copy(
            youOwe = Money.toRupees(youOwePaise),
            othersOweYou = Money.toRupees(othersOweYouPaise),
            netBalance = Money.toRupees(netBalancePaise)
        )
    }
}
