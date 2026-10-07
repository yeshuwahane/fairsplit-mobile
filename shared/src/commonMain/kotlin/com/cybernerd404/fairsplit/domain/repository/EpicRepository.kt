package com.cybernerd404.fairsplit.domain.repository

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.EpicStatus
import com.cybernerd404.fairsplit.domain.model.Member

interface EpicRepository {
    suspend fun getEpics(status: EpicStatus? = null): DataResource<List<Epic>>
    suspend fun getEpicById(id: String): DataResource<Epic>
    suspend fun createEpic(name: String, memberNames: List<String>): DataResource<Epic>
    suspend fun addMemberToEpic(epicId: String, memberName: String): DataResource<Epic>
    suspend fun removeMemberFromEpic(epicId: String, memberId: String): DataResource<Epic>
    suspend fun getEpicByInviteCode(inviteCode: String): DataResource<Epic>
    suspend fun joinEpicByInviteCode(inviteCode: String): DataResource<Epic>
    suspend fun deleteEpic(epicId: String): DataResource<Boolean>
    suspend fun updateEpic(epicId: String, name: String, description: String? = null): DataResource<Epic>
    suspend fun getRecentPeers(): List<Member>
}
