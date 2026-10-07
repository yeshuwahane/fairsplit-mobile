package com.cybernerd404.fairsplit.presentation.epics

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.EpicStatus
import com.cybernerd404.fairsplit.domain.usecase.GetEpicsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EpicsViewModel(
    private val getEpicsUseCase: GetEpicsUseCase
) : ScreenModel {

    private val _epicsState = MutableStateFlow<DataResource<List<Epic>>>(DataResource.loading())
    val epicsState = _epicsState.asStateFlow()

    private val _selectedStatus = MutableStateFlow(EpicStatus.ACTIVE)
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadEpics()
    }

    fun refresh() {
        screenModelScope.launch {
            _isRefreshing.value = true
            val result = getEpicsUseCase(_selectedStatus.value)
            _epicsState.value = result
            _isRefreshing.value = false
        }
    }

    fun setStatus(status: EpicStatus) {
        _selectedStatus.value = status
        loadEpics()
    }

    fun loadEpics() {
        screenModelScope.launch {
            if (_epicsState.value.data == null) {
                _epicsState.value = DataResource.loading()
            }
            val result = getEpicsUseCase(_selectedStatus.value)
            _epicsState.value = result
        }
    }
}
