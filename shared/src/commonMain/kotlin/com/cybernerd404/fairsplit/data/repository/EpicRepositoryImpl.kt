package com.cybernerd404.fairsplit.data.repository

import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.data.utils.safeApiCall
import com.cybernerd404.fairsplit.domain.engine.ExpenseCalculationEngine
import com.cybernerd404.fairsplit.domain.model.*
import com.cybernerd404.fairsplit.domain.repository.EpicRepository

import com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSource
import com.cybernerd404.fairsplit.data.remote.dto.toDto

class EpicRepositoryImpl(
    private val syncDataSource: RemoteSyncDataSource? = null
) : EpicRepository {

    override suspend fun getEpics(status: EpicStatus?): DataResource<List<Epic>> = safeApiCall {
        try {
            val remoteEpics = syncDataSource?.getEpics()
            if (remoteEpics != null) {
                val rDomainList = remoteEpics.map { it.toDomain() }
                MockDataProvider.allEpics.clear()
                MockDataProvider.allEpics.addAll(rDomainList)

                val validEpicIds = rDomainList.map { it.id }.toSet()
                MockDataProvider.expensesByEpicId.keys.retainAll(validEpicIds)

                rDomainList.forEach { rEpic ->
                    if (rEpic.recentExpenses.isNotEmpty()) {
                        val list = MockDataProvider.expensesByEpicId.getOrPut(rEpic.id) { mutableListOf() }
                        rEpic.recentExpenses.forEach { exp ->
                            val existingIdx = list.indexOfFirst { it.id == exp.id }
                            if (existingIdx >= 0) list[existingIdx] = exp else list.add(0, exp)
                        }
                    }
                    MockDataProvider.recalculateEpicById(rEpic.id)
                }

                MockDataProvider.recalculateGlobalSummary()
                MockDataProvider.persistToLocalDatabase()
            }
        } catch (_: Exception) {}

        if (status != null) {
            MockDataProvider.allEpics.filter { it.status == status }
        } else {
            MockDataProvider.allEpics
        }
    }

    override suspend fun getEpicById(id: String): DataResource<Epic> = safeApiCall {
        try {
            val remote = syncDataSource?.getEpicById(id)
            if (remote != null) {
                val rEpic = remote.toDomain()
                val idx = MockDataProvider.allEpics.indexOfFirst {
                    it.id == id || (rEpic.inviteCode.isNotBlank() && it.inviteCode.equals(rEpic.inviteCode, ignoreCase = true))
                }
                if (idx >= 0) {
                    MockDataProvider.allEpics[idx] = rEpic
                } else {
                    MockDataProvider.allEpics.add(0, rEpic)
                }
                if (rEpic.recentExpenses.isNotEmpty()) {
                    val list = MockDataProvider.expensesByEpicId.getOrPut(rEpic.id) { mutableListOf() }
                    rEpic.recentExpenses.forEach { exp ->
                        val existingIdx = list.indexOfFirst { it.id == exp.id }
                        if (existingIdx >= 0) list[existingIdx] = exp else list.add(0, exp)
                    }
                }
                MockDataProvider.recalculateEpicById(rEpic.id)
                MockDataProvider.persistToLocalDatabase()
                return@safeApiCall rEpic
            }
        } catch (_: Exception) {}

        MockDataProvider.recalculateEpicById(id)
            ?: MockDataProvider.allEpics.firstOrNull { it.id == id }
            ?: throw Exception("Epic with id $id not found")
    }

    override suspend fun createEpic(name: String, memberNames: List<String>): DataResource<Epic> = safeApiCall {
        val colorPalette = MockDataProvider.avatarColors
        val cleanMemberNames = memberNames.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        val currentUserProfile = com.cybernerd404.fairsplit.data.local.LocalDatabase.getOrCreateUserProfile()
        val currentUserName = currentUserProfile.name.ifBlank { "You" }
        val currentUserId = currentUserProfile.id

        // Ensure Current User is at index 0
        val finalNames = if (cleanMemberNames.none { it.equals(currentUserName, ignoreCase = true) }) {
            listOf(currentUserName) + cleanMemberNames
        } else {
            cleanMemberNames
        }

        val createdMembers = finalNames.mapIndexed { index, mName ->
            val isUser = mName.equals(currentUserName, ignoreCase = true)
            Member(
                id = if (isUser) currentUserId else "m_${mName.lowercase()}_${index}_${(100..999).random()}",
                name = mName,
                avatarInitials = mName.take(2).uppercase(),
                avatarColorHex = colorPalette[index % colorPalette.size],
                isCurrentUser = isUser
            )
        }

        val tempId = "epic_${name.lowercase().replace(" ", "_")}_${(1000..9999).random()}"
        val cleanCode = "FS-${name.filter { it.isLetter() }.take(4).uppercase().ifEmpty { "EPIC" }}-${(1000..9999).random()}"
        val draftEpic = Epic(
            id = tempId,
            name = name,
            description = "Group for $name",
            totalSpent = 0.0,
            memberCount = createdMembers.size,
            members = createdMembers,
            yourSpent = 0.0,
            yourBalance = 0.0,
            status = EpicStatus.ACTIVE,
            categoryBreakdowns = emptyList(),
            recentExpenses = emptyList(),
            settlements = emptyList(),
            createdAt = "Just now",
            inviteCode = cleanCode
        )

        var authoritativeEpic = draftEpic
        if (syncDataSource != null) {
            val remoteDto = syncDataSource.saveEpic(draftEpic.toDto())
            if (remoteDto.id.isNotBlank()) {
                authoritativeEpic = remoteDto.toDomain()
            }
        }

        MockDataProvider.expensesByEpicId[authoritativeEpic.id] = mutableListOf()
        val existingIndex = MockDataProvider.allEpics.indexOfFirst { it.id == authoritativeEpic.id }
        if (existingIndex >= 0) {
            MockDataProvider.allEpics[existingIndex] = authoritativeEpic
        } else {
            MockDataProvider.allEpics.add(0, authoritativeEpic)
        }
        MockDataProvider.recalculateGlobalSummary()
        MockDataProvider.persistToLocalDatabase()
        com.cybernerd404.fairsplit.data.local.LocalDatabase.addRecentPeers(authoritativeEpic.members)

        authoritativeEpic
    }

    override suspend fun addMemberToEpic(epicId: String, memberName: String): DataResource<Epic> = safeApiCall {
        val epic = MockDataProvider.allEpics.firstOrNull { it.id == epicId }
            ?: throw Exception("Epic not found")

        val cleanName = memberName.trim()
        if (cleanName.isEmpty()) return@safeApiCall epic

        if (epic.members.any { it.name.equals(cleanName, ignoreCase = true) }) {
            return@safeApiCall epic
        }

        val color = MockDataProvider.avatarColors[epic.members.size % MockDataProvider.avatarColors.size]
        val newMember = Member(
            id = "m_${cleanName.lowercase()}_${epic.members.size + 1}",
            name = cleanName,
            avatarInitials = cleanName.take(2).uppercase(),
            avatarColorHex = color,
            isCurrentUser = false
        )

        val updatedMembers = epic.members + newMember
        val updatedEpic = epic.copy(
            members = updatedMembers,
            memberCount = updatedMembers.size
        )

        val index = MockDataProvider.allEpics.indexOfFirst { it.id == epicId }
        if (index >= 0) {
            MockDataProvider.allEpics[index] = updatedEpic
        }

        com.cybernerd404.fairsplit.data.local.LocalDatabase.addRecentPeers(listOf(newMember))
        MockDataProvider.persistToLocalDatabase()
        MockDataProvider.recalculateEpicById(epicId) ?: updatedEpic
    }

    override suspend fun removeMemberFromEpic(epicId: String, memberId: String): DataResource<Epic> = safeApiCall {
        val epic = MockDataProvider.allEpics.firstOrNull { it.id == epicId }
            ?: throw Exception("Epic not found")

        // Cannot remove current user
        val memberToRemove = epic.members.firstOrNull { it.id == memberId }
        if (memberToRemove == null || memberToRemove.isCurrentUser) {
            return@safeApiCall epic
        }

        // Remove member from members list
        val updatedMembers = epic.members.filter { it.id != memberId }

        // Remove member from existing expenses participants/shares
        val expenses = MockDataProvider.expensesByEpicId[epicId] ?: mutableListOf()
        val sanitizedExpenses = expenses.map { expense ->
            if (expense.participants.any { it.id == memberId }) {
                val remainingParticipants = expense.participants.filter { it.id != memberId }.ifEmpty { updatedMembers }
                val newShares = ExpenseCalculationEngine.calculateEqualShares(expense.amount, remainingParticipants)
                expense.copy(participants = remainingParticipants, shares = newShares)
            } else {
                expense
            }
        }.toMutableList()

        MockDataProvider.expensesByEpicId[epicId] = sanitizedExpenses
        val baseEpic = epic.copy(members = updatedMembers, memberCount = updatedMembers.size)
        val index = MockDataProvider.allEpics.indexOfFirst { it.id == epicId }
        if (index >= 0) {
            MockDataProvider.allEpics[index] = baseEpic
        }

        MockDataProvider.recalculateEpicById(epicId) ?: baseEpic
    }

    override suspend fun getEpicByInviteCode(inviteCode: String): DataResource<Epic> = safeApiCall {
        val clean = inviteCode.trim().uppercase()
        try {
            val remote = syncDataSource?.getEpicByInviteCode(clean)
            if (remote != null) {
                val rEpic = remote.toDomain()
                val idx = MockDataProvider.allEpics.indexOfFirst { it.id == rEpic.id || it.inviteCode.equals(clean, ignoreCase = true) }
                if (idx >= 0) {
                    MockDataProvider.allEpics[idx] = rEpic
                } else {
                    MockDataProvider.allEpics.add(0, rEpic)
                }
                if (rEpic.recentExpenses.isNotEmpty()) {
                    val list = MockDataProvider.expensesByEpicId.getOrPut(rEpic.id) { mutableListOf() }
                    rEpic.recentExpenses.forEach { exp ->
                        if (list.none { it.id == exp.id }) list.add(0, exp)
                    }
                }
                MockDataProvider.recalculateEpicById(rEpic.id)
                MockDataProvider.persistToLocalDatabase()
                return@safeApiCall rEpic
            }
        } catch (_: Exception) {}

        val local = MockDataProvider.allEpics.firstOrNull { it.inviteCode.equals(clean, ignoreCase = true) }
        if (local != null) return@safeApiCall local

        throw Exception("No Epic found with invite code '$inviteCode'")
    }

    override suspend fun joinEpicByInviteCode(inviteCode: String): DataResource<Epic> = safeApiCall {
        val clean = inviteCode.trim().uppercase()
        val currentUserProfile = com.cybernerd404.fairsplit.data.local.LocalDatabase.getOrCreateUserProfile()
        val currentUserName = currentUserProfile.name.ifBlank { "You" }
        val currentUserId = currentUserProfile.id

        val newMember = Member(
            id = currentUserId,
            name = currentUserName,
            avatarInitials = if (currentUserProfile.avatarInitials.isNotBlank()) currentUserProfile.avatarInitials else currentUserName.take(2).uppercase(),
            avatarColorHex = 0xFF10B981,
            isCurrentUser = true
        )

        try {
            val joinedRemote = syncDataSource?.joinEpic(clean, newMember.toDto())
            if (joinedRemote != null) {
                val rEpic = joinedRemote.toDomain()
                val idx = MockDataProvider.allEpics.indexOfFirst { it.id == rEpic.id || it.inviteCode.equals(clean, ignoreCase = true) }
                if (idx >= 0) {
                    MockDataProvider.allEpics[idx] = rEpic
                } else {
                    MockDataProvider.allEpics.add(0, rEpic)
                }
                if (rEpic.recentExpenses.isNotEmpty()) {
                    val list = MockDataProvider.expensesByEpicId.getOrPut(rEpic.id) { mutableListOf() }
                    rEpic.recentExpenses.forEach { exp ->
                        if (list.none { it.id == exp.id }) list.add(0, exp)
                    }
                }
                MockDataProvider.recalculateEpicById(rEpic.id)
                MockDataProvider.persistToLocalDatabase()
                com.cybernerd404.fairsplit.data.local.LocalDatabase.addRecentPeers(rEpic.members)
                return@safeApiCall rEpic
            }
        } catch (_: Exception) {}

        // Fallback only if local epic exists
        val localEpic = MockDataProvider.allEpics.firstOrNull { it.inviteCode.equals(clean, ignoreCase = true) }
            ?: throw Exception("Could not join epic with code '$inviteCode'")

        val existingIndex = localEpic.members.indexOfFirst { it.id == currentUserId || it.name.equals(currentUserName, ignoreCase = true) }
        val updatedMembers = if (existingIndex >= 0) {
            localEpic.members.map { if (it.id == currentUserId || it.name.equals(currentUserName, ignoreCase = true)) newMember else it }
        } else {
            localEpic.members + newMember
        }
        val updatedEpic = localEpic.copy(
            members = updatedMembers,
            memberCount = updatedMembers.size
        )
        val idx = MockDataProvider.allEpics.indexOfFirst { it.id == localEpic.id }
        if (idx >= 0) MockDataProvider.allEpics[idx] = updatedEpic else MockDataProvider.allEpics.add(0, updatedEpic)
        MockDataProvider.persistToLocalDatabase()
        updatedEpic
    }

    override suspend fun deleteEpic(epicId: String): DataResource<Boolean> = safeApiCall {
        try {
            syncDataSource?.deleteEpic(epicId)
        } catch (_: Exception) {}
        MockDataProvider.allEpics.removeAll { it.id == epicId }
        MockDataProvider.expensesByEpicId.remove(epicId)
        MockDataProvider.allActivities.removeAll { it.epicId == epicId }
        MockDataProvider.recalculateGlobalSummary()
        MockDataProvider.persistToLocalDatabase()
        true
    }

    override suspend fun updateEpic(epicId: String, name: String, description: String?): DataResource<Epic> = safeApiCall {
        val existing = MockDataProvider.allEpics.firstOrNull { it.id == epicId }
            ?: throw Exception("Epic not found")
        val updated = existing.copy(name = name, description = description ?: existing.description)
        try {
            val remote = syncDataSource?.updateEpic(updated.toDto())?.toDomain()
            if (remote != null) {
                val idx = MockDataProvider.allEpics.indexOfFirst { it.id == epicId }
                if (idx >= 0) MockDataProvider.allEpics[idx] = remote
                MockDataProvider.persistToLocalDatabase()
                return@safeApiCall remote
            }
        } catch (_: Exception) {}
        val idx = MockDataProvider.allEpics.indexOfFirst { it.id == epicId }
        if (idx >= 0) MockDataProvider.allEpics[idx] = updated
        MockDataProvider.persistToLocalDatabase()
        updated
    }

    override suspend fun getRecentPeers(): List<Member> {
        val allMembers = MockDataProvider.allEpics.flatMap { it.members }
        if (allMembers.isNotEmpty()) {
            com.cybernerd404.fairsplit.data.local.LocalDatabase.addRecentPeers(allMembers)
        }
        return com.cybernerd404.fairsplit.data.local.LocalDatabase.getRecentPeers()
    }
}
