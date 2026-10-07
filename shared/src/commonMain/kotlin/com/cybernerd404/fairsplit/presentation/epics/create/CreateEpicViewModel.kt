package com.cybernerd404.fairsplit.presentation.epics.create

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.domain.usecase.CreateEpicUseCase
import com.cybernerd404.fairsplit.domain.usecase.GetRecentPeersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateEpicViewModel(
    private val createEpicUseCase: CreateEpicUseCase,
    private val getRecentPeersUseCase: GetRecentPeersUseCase
) : ScreenModel {

    private val userProfile = LocalDatabase.getUserProfile()
    val currentUserName: String = userProfile?.name?.trim()?.ifBlank { "You" } ?: "You"

    private val _epicName = MutableStateFlow("")
    val epicName = _epicName.asStateFlow()

    // Start with current user as the only default member
    private val _members = MutableStateFlow(listOf(currentUserName))
    val members = _members.asStateFlow()

    private val _recentPeers = MutableStateFlow<List<Member>>(emptyList())
    val recentPeers = _recentPeers.asStateFlow()

    private val _newMemberInput = MutableStateFlow("")
    val newMemberInput = _newMemberInput.asStateFlow()

    private val _creationState = MutableStateFlow<DataResource<Epic>>(DataResource.initial())
    val creationState = _creationState.asStateFlow()

    init {
        loadRecentPeers()
    }

    fun loadRecentPeers() {
        screenModelScope.launch {
            val peers = getRecentPeersUseCase()
            // Exclude current user from peers list
            _recentPeers.value = peers.filter { 
                !it.name.equals(currentUserName, ignoreCase = true) &&
                (userProfile?.id.isNullOrBlank() || it.id != userProfile?.id)
            }
        }
    }

    fun onEpicNameChanged(name: String) {
        _epicName.value = name
    }

    fun onNewMemberInputChanged(text: String) {
        _newMemberInput.value = text
    }

    fun togglePeer(peerName: String) {
        val clean = peerName.trim()
        if (clean.isBlank() || clean.equals(currentUserName, ignoreCase = true)) return
        val currentList = _members.value
        if (currentList.any { it.equals(clean, ignoreCase = true) }) {
            _members.value = currentList.filter { !it.equals(clean, ignoreCase = true) }
        } else {
            _members.value = currentList + clean
        }
    }

    fun addMember() {
        val name = _newMemberInput.value.trim()
        if (name.isNotEmpty() && !_members.value.any { it.equals(name, ignoreCase = true) }) {
            _members.value = _members.value + name
            _newMemberInput.value = ""
        }
    }

    fun removeMember(name: String) {
        if (!name.equals(currentUserName, ignoreCase = true)) {
            _members.value = _members.value.filter { !it.equals(name, ignoreCase = true) }
        }
    }

    fun createEpic(onSuccess: (Epic) -> Unit) {
        val name = _epicName.value.trim()
        if (name.isEmpty()) return

        screenModelScope.launch {
            _creationState.value = DataResource.loading()
            val result = createEpicUseCase(name, _members.value)
            _creationState.value = result
            if (result.isSuccess() && result.data != null) {
                onSuccess(result.data!!)
            }
        }
    }
}
