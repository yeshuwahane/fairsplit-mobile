package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.EpicStatus
import com.cybernerd404.fairsplit.domain.repository.EpicRepository

class GetEpicsUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(status: EpicStatus? = null): DataResource<List<Epic>> {
        return epicRepository.getEpics(status)
    }
}

class GetEpicDetailUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(epicId: String): DataResource<Epic> {
        return epicRepository.getEpicById(epicId)
    }
}

class CreateEpicUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(name: String, memberNames: List<String>): DataResource<Epic> {
        return epicRepository.createEpic(name, memberNames)
    }
}

class AddMemberToEpicUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(epicId: String, memberName: String): DataResource<Epic> {
        return epicRepository.addMemberToEpic(epicId, memberName)
    }
}

class RemoveMemberFromEpicUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(epicId: String, memberId: String): DataResource<Epic> {
        return epicRepository.removeMemberFromEpic(epicId, memberId)
    }
}

class GetEpicByInviteCodeUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(inviteCode: String): DataResource<Epic> {
        return epicRepository.getEpicByInviteCode(inviteCode)
    }
}

class JoinEpicByInviteCodeUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(inviteCode: String): DataResource<Epic> {
        return epicRepository.joinEpicByInviteCode(inviteCode)
    }
}

class GetRecentPeersUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(): List<com.cybernerd404.fairsplit.domain.model.Member> {
        return epicRepository.getRecentPeers()
    }
}

class DeleteEpicUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(epicId: String): DataResource<Boolean> {
        return epicRepository.deleteEpic(epicId)
    }
}

class UpdateEpicUseCase(private val epicRepository: EpicRepository) {
    suspend operator fun invoke(epicId: String, name: String, description: String? = null): DataResource<Epic> {
        return epicRepository.updateEpic(epicId, name, description)
    }
}
