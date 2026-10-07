package com.cybernerd404.fairsplit.data.local

import com.cybernerd404.fairsplit.data.utils.getCurrentFormattedDate
import com.cybernerd404.fairsplit.data.utils.getCurrentFormattedTime
import com.cybernerd404.fairsplit.data.utils.getCurrentTimestamp
import com.cybernerd404.fairsplit.domain.engine.ExpenseCalculationEngine
import com.cybernerd404.fairsplit.domain.model.*

object MockDataProvider {

    // Color palette for member avatars
    val avatarColors = listOf(0xFF10B981, 0xFF6366F1, 0xFFF59E0B, 0xFFEC4899, 0xFF8B5CF6, 0xFF06B6D4, 0xFFF97316)

    // Current User Profile (Loaded from session/database or blank guest)
    var currentUserProfile: UserProfile = LocalDatabase.getOrCreateUserProfile()

    var currentUserPreferences = UserPreferences(
        currencySymbol = "₹",
        defaultSplitPreference = "Equally",
        notificationsEnabled = true,
        isDarkMode = true
    )

    // Live in-memory expenses map keyed by Epic ID
    val expensesByEpicId = mutableMapOf<String, MutableList<Expense>>()

    // Live in-memory Epics list
    val allEpics = mutableListOf<Epic>()

    // Live transactions list
    val allTransactions = mutableListOf<Transaction>()

    // Live Audit Activities History list
    val allActivities = mutableListOf<ActivityEntry>()

    init {
        initializeData()
    }

    fun persistToLocalDatabase() {
        try {
            LocalDatabase.saveEpics(allEpics)
            LocalDatabase.saveAllExpenses(expensesByEpicId)
            LocalDatabase.saveTransactions(allTransactions)
            LocalDatabase.saveActivities(allActivities)
            LocalDatabase.saveUserProfile(currentUserProfile)
            LocalDatabase.saveUserPreferences(currentUserPreferences)
            LocalDatabase.markDatabaseInitialized()
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("Persist state error: ${e.message}")
        }
    }

    fun updateCurrentUser(profile: UserProfile) {
        currentUserProfile = profile
        val name = profile.name.ifBlank { "You" }
        val initials = if (profile.avatarInitials.isNotBlank()) profile.avatarInitials else name.take(2).uppercase().ifEmpty { "ME" }
        val avatarUrl = profile.avatarUrl

        // Update all occurrences of current user member across epics, expenses, transactions
        allEpics.forEachIndexed { epicIndex, epic ->
            val updatedMembers = epic.members.map { m ->
                if (m.isCurrentUser || m.id == profile.id || m.id == "m1") {
                    m.copy(id = profile.id.ifBlank { m.id }, name = name, avatarInitials = initials, avatarUrl = avatarUrl)
                } else m
            }
            allEpics[epicIndex] = epic.copy(members = updatedMembers)
        }

        expensesByEpicId.forEach { (epicId, expenses) ->
            expenses.forEachIndexed { expIndex, exp ->
                val updatedPaidBy = if (exp.paidBy.isCurrentUser || exp.paidBy.id == profile.id || exp.paidBy.id == "m1") {
                    exp.paidBy.copy(id = profile.id.ifBlank { exp.paidBy.id }, name = name, avatarInitials = initials, avatarUrl = avatarUrl)
                } else exp.paidBy
                val updatedParticipants = exp.participants.map { p ->
                    if (p.isCurrentUser || p.id == profile.id || p.id == "m1") {
                        p.copy(id = profile.id.ifBlank { p.id }, name = name, avatarInitials = initials, avatarUrl = avatarUrl)
                    } else p
                }
                expenses[expIndex] = exp.copy(paidBy = updatedPaidBy, participants = updatedParticipants)
            }
        }

        allTransactions.forEachIndexed { txIndex, tx ->
            val updatedFrom = if (tx.fromMember.isCurrentUser || tx.fromMember.id == profile.id || tx.fromMember.id == "m1") {
                tx.fromMember.copy(id = profile.id.ifBlank { tx.fromMember.id }, name = name, avatarInitials = initials, avatarUrl = avatarUrl)
            } else tx.fromMember
            val updatedTo = if (tx.toMember.isCurrentUser || tx.toMember.id == profile.id || tx.toMember.id == "m1") {
                tx.toMember.copy(id = profile.id.ifBlank { tx.toMember.id }, name = name, avatarInitials = initials, avatarUrl = avatarUrl)
            } else tx.toMember
            allTransactions[txIndex] = tx.copy(fromMember = updatedFrom, toMember = updatedTo)
        }

        allActivities.forEachIndexed { actIndex, act ->
            val updatedActor = if (act.actor.isCurrentUser || act.actor.id == profile.id || act.actor.id == "m1") {
                act.actor.copy(id = profile.id.ifBlank { act.actor.id }, name = name, avatarInitials = initials, avatarUrl = avatarUrl)
            } else act.actor
            allActivities[actIndex] = act.copy(actor = updatedActor)
        }

        persistToLocalDatabase()
    }

    fun initializeData() {
        allEpics.clear()
        expensesByEpicId.clear()
        allTransactions.clear()
        allActivities.clear()

        val savedProfile = LocalDatabase.getUserProfile()
        if (savedProfile != null) {
            currentUserProfile = savedProfile
        } else {
            currentUserProfile = UserProfile(
                id = "",
                name = "",
                email = "",
                phone = "",
                upiId = "",
                avatarInitials = "ME",
                profileCompleted = false
            )
        }

        val savedPrefs = LocalDatabase.getUserPreferences()
        if (savedPrefs != null) {
            currentUserPreferences = savedPrefs
        }

        val savedEpics = LocalDatabase.getEpics()
        val savedExpenses = LocalDatabase.getAllExpenses()
        val savedTransactions = LocalDatabase.getTransactions()
        val savedActivities = LocalDatabase.getActivities()

        // Clean out any legacy mock epics and deduplicate historical epics
        if (!savedEpics.isNullOrEmpty()) {
            val userEpics = savedEpics.filter { !it.id.startsWith("epic_goa") && !it.id.startsWith("epic_mumbai") && !it.id.startsWith("epic_turf") }
            val uniqueEpics = userEpics.distinctBy {
                if (it.inviteCode.isNotBlank()) it.inviteCode.lowercase() else it.id
            }
            allEpics.clear()
            allEpics.addAll(uniqueEpics)
        }
        if (savedExpenses.isNotEmpty()) {
            val userExpenses = savedExpenses.filterKeys { !it.startsWith("epic_goa") && !it.startsWith("epic_mumbai") && !it.startsWith("epic_turf") }
            expensesByEpicId.clear()
            expensesByEpicId.putAll(userExpenses.mapValues { it.value.toMutableList() })
        }
        if (savedTransactions != null) {
            val userTxs = savedTransactions.filter { it.epicId != "epic_goa_trip" && it.epicId != "epic_turf" }
            allTransactions.clear()
            allTransactions.addAll(userTxs)
        }
        if (savedActivities != null) {
            val userActs = savedActivities.filter { it.epicId != "epic_goa_trip" && it.epicId != "epic_mumbai" && it.epicId != "epic_turf" }
            allActivities.clear()
            allActivities.addAll(userActs)
        }

        recalculateGlobalSummary()
    }

    fun logActivity(
        epicId: String,
        epicName: String,
        type: ActivityType,
        title: String,
        description: String,
        amount: Double? = null,
        actor: Member,
        receiptUrl: String? = null,
        expenseId: String? = null
    ): ActivityEntry {
        val entry = ActivityEntry(
            id = "act_${(1000..9999).random()}",
            epicId = epicId,
            epicName = epicName,
            type = type,
            title = title,
            description = description,
            amount = amount,
            actor = actor,
            timestamp = getCurrentTimestamp(),
            date = getCurrentFormattedDate(),
            receiptUrl = receiptUrl,
            expenseId = expenseId
        )
        allActivities.add(0, entry)
        persistToLocalDatabase()
        return entry
    }

    /**
     * Recomputes user profile summary across all active epics.
     */
    fun recalculateGlobalSummary() {
        currentUserProfile = ExpenseCalculationEngine.calculateUserSummary(allEpics, currentUserProfile)
    }

    /**
     * Recalculates an Epic by its ID and refreshes global user balance.
     */
    fun recalculateEpicById(epicId: String): Epic? {
        val epicIndex = allEpics.indexOfFirst { it.id == epicId }
        if (epicIndex < 0) return null

        val currentEpic = allEpics[epicIndex]
        val expenses = expensesByEpicId[epicId] ?: mutableListOf()
        val updatedEpic = ExpenseCalculationEngine.recalculateEpic(currentEpic, expenses)

        allEpics[epicIndex] = updatedEpic
        recalculateGlobalSummary()
        persistToLocalDatabase()
        return updatedEpic
    }

    /**
     * Wipes all in-memory epics, expenses, transactions, and activities and clears local storage.
     */
    fun resetAllData() {
        allEpics.clear()
        expensesByEpicId.clear()
        allTransactions.clear()
        allActivities.clear()
        currentUserProfile = UserProfile(
            id = "",
            name = "",
            email = "",
            phone = "",
            upiId = "",
            avatarInitials = "ME",
            profileCompleted = false
        )
        LocalDatabase.clearAllData()
    }
}
