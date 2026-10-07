package com.cybernerd404.fairsplit.data.local

import com.cybernerd404.fairsplit.domain.model.*
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object LocalDatabase {
    private val settings: Settings by lazy { Settings() }
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private const val KEY_EPICS = "fairsplit_db_epics_v1"
    private const val KEY_EXPENSES_PREFIX = "fairsplit_db_expenses_v1_"
    private const val KEY_EXPENSE_KEYS = "fairsplit_db_expense_keys_v1"
    private const val KEY_TRANSACTIONS = "fairsplit_db_transactions_v1"
    private const val KEY_ACTIVITIES = "fairsplit_db_activities_v1"
    private const val KEY_USER_PROFILE = "fairsplit_db_user_profile_v1"
    private const val KEY_USER_PREFERENCES = "fairsplit_db_user_preferences_v1"
    private const val KEY_IS_INITIALIZED = "fairsplit_db_is_initialized_v1"
    private const val KEY_ONBOARDING_COMPLETED = "fairsplit_db_onboarding_completed_v1"
    private const val KEY_IS_LOGGED_IN = "fairsplit_db_is_logged_in_v1"
    private const val KEY_AUTH_PHONE = "fairsplit_db_auth_phone_v1"
    private const val KEY_AUTH_PROVIDER = "fairsplit_db_auth_provider_v1"
    private const val KEY_RECENT_PEERS = "fairsplit_db_recent_peers_v1"

    fun isDatabaseInitialized(): Boolean {
        return settings.getBoolean(KEY_IS_INITIALIZED, false)
    }

    fun markDatabaseInitialized() {
        settings[KEY_IS_INITIALIZED] = true
    }

    // Onboarding & Auth State
    fun isOnboardingCompleted(): Boolean {
        return settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        settings[KEY_ONBOARDING_COMPLETED] = completed
    }

    fun isLoggedIn(): Boolean {
        return settings.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        settings[KEY_IS_LOGGED_IN] = loggedIn
    }

    fun getAuthPhone(): String? {
        return settings.getStringOrNull(KEY_AUTH_PHONE)
    }

    fun setAuthPhone(phone: String?) {
        if (phone == null) settings.remove(KEY_AUTH_PHONE) else settings[KEY_AUTH_PHONE] = phone
    }

    fun getAuthProvider(): String? {
        return settings.getStringOrNull(KEY_AUTH_PROVIDER)
    }

    fun setAuthProvider(provider: String?) {
        if (provider == null) settings.remove(KEY_AUTH_PROVIDER) else settings[KEY_AUTH_PROVIDER] = provider
    }

    fun logout() {
        clearAllData()
    }

    // Epics
    fun saveEpics(epics: List<Epic>) {
        try {
            settings[KEY_EPICS] = json.encodeToString(epics)
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("DB saveEpics error: ${e.message}")
        }
    }

    fun getEpics(): List<Epic>? {
        val raw = settings.getStringOrNull(KEY_EPICS) ?: return null
        return try {
            json.decodeFromString<List<Epic>>(raw)
        } catch (e: Exception) {
            null
        }
    }

    // Expenses by Epic ID
    fun saveExpensesForEpic(epicId: String, expenses: List<Expense>) {
        try {
            settings[KEY_EXPENSES_PREFIX + epicId] = json.encodeToString(expenses)
            
            // Track epic IDs that have expenses
            val existingKeys = getExpenseEpicIds().toMutableSet()
            existingKeys.add(epicId)
            settings[KEY_EXPENSE_KEYS] = json.encodeToString(existingKeys.toList())
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("DB saveExpensesForEpic error: ${e.message}")
        }
    }

    fun getExpensesForEpic(epicId: String): List<Expense>? {
        val raw = settings.getStringOrNull(KEY_EXPENSES_PREFIX + epicId) ?: return null
        return try {
            json.decodeFromString<List<Expense>>(raw)
        } catch (e: Exception) {
            null
        }
    }

    fun getExpenseEpicIds(): List<String> {
        val raw = settings.getStringOrNull(KEY_EXPENSE_KEYS) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAllExpenses(map: Map<String, List<Expense>>) {
        map.forEach { (epicId, expenses) ->
            saveExpensesForEpic(epicId, expenses)
        }
    }

    fun getAllExpenses(): Map<String, List<Expense>> {
        val epicIds = getExpenseEpicIds()
        val result = mutableMapOf<String, List<Expense>>()
        epicIds.forEach { epicId ->
            val list = getExpensesForEpic(epicId)
            if (list != null) {
                result[epicId] = list
            }
        }
        return result
    }

    // Transactions
    fun saveTransactions(transactions: List<Transaction>) {
        try {
            settings[KEY_TRANSACTIONS] = json.encodeToString(transactions)
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("DB saveTransactions error: ${e.message}")
        }
    }

    fun getTransactions(): List<Transaction>? {
        val raw = settings.getStringOrNull(KEY_TRANSACTIONS) ?: return null
        return try {
            json.decodeFromString<List<Transaction>>(raw)
        } catch (e: Exception) {
            null
        }
    }

    // Activities Audit Log
    fun saveActivities(activities: List<ActivityEntry>) {
        try {
            settings[KEY_ACTIVITIES] = json.encodeToString(activities)
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("DB saveActivities error: ${e.message}")
        }
    }

    fun getActivities(): List<ActivityEntry>? {
        val raw = settings.getStringOrNull(KEY_ACTIVITIES) ?: return null
        return try {
            json.decodeFromString<List<ActivityEntry>>(raw)
        } catch (e: Exception) {
            null
        }
    }

    // User Profile
    fun saveUserProfile(profile: UserProfile) {
        try {
            settings[KEY_USER_PROFILE] = json.encodeToString(profile)
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("DB saveUserProfile error: ${e.message}")
        }
    }

    fun getUserProfile(): UserProfile? {
        val raw = settings.getStringOrNull(KEY_USER_PROFILE) ?: return null
        return try {
            json.decodeFromString<UserProfile>(raw)
        } catch (e: Exception) {
            null
        }
    }

    fun getOrCreateUserProfile(): UserProfile {
        val existing = getUserProfile()
        if (existing != null && existing.id.isNotBlank()) {
            return existing
        }
        return UserProfile(
            id = "",
            name = "",
            email = "",
            phone = "",
            upiId = "",
            avatarInitials = "ME",
            youOwe = 0.0,
            othersOweYou = 0.0,
            netBalance = 0.0,
            profileCompleted = false
        )
    }

    // User Preferences
    fun saveUserPreferences(preferences: UserPreferences) {
        try {
            settings[KEY_USER_PREFERENCES] = json.encodeToString(preferences)
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("DB saveUserPreferences error: ${e.message}")
        }
    }

    fun getUserPreferences(): UserPreferences? {
        val raw = settings.getStringOrNull(KEY_USER_PREFERENCES) ?: return null
        return try {
            json.decodeFromString<UserPreferences>(raw)
        } catch (e: Exception) {
            null
        }
    }

    // Recent Peers / Collaborators / Friends
    fun saveRecentPeers(peers: List<Member>) {
        try {
            settings[KEY_RECENT_PEERS] = json.encodeToString(peers)
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("DB saveRecentPeers error: ${e.message}")
        }
    }

    fun getRecentPeers(): List<Member> {
        val raw = settings.getStringOrNull(KEY_RECENT_PEERS) ?: return emptyList()
        return try {
            json.decodeFromString<List<Member>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addRecentPeers(newPeers: List<Member>) {
        val current = getRecentPeers().toMutableList()
        val currentProfile = getUserProfile()
        val currentUserName = currentProfile?.name.orEmpty()
        val currentUserId = currentProfile?.id.orEmpty()

        newPeers.forEach { peer ->
            val cleanName = peer.name.trim()
            if (!peer.isCurrentUser &&
                cleanName.isNotBlank() &&
                !cleanName.equals(currentUserName, ignoreCase = true) &&
                (currentUserId.isBlank() || peer.id != currentUserId)
            ) {
                // Remove existing match so updated/most recent peer goes to front
                current.removeAll { it.name.trim().equals(cleanName, ignoreCase = true) || (it.id.isNotBlank() && it.id == peer.id) }
                current.add(0, peer.copy(isCurrentUser = false, name = cleanName))
            }
        }
        saveRecentPeers(current)
    }

    /**
     * Clears all local storage, epics, expenses, transactions, and session caches to start from zero.
     */
    fun clearAllData() {
        settings.clear()
    }
}
