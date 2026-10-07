package com.cybernerd404.fairsplit.presentation.transactions

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getAllActivitiesUseCase: com.cybernerd404.fairsplit.domain.usecase.GetAllActivitiesUseCase
) : ScreenModel {

    private val _transactionsState = MutableStateFlow<DataResource<List<Transaction>>>(DataResource.loading())
    val transactionsState = _transactionsState.asStateFlow()

    private val _activitiesState = MutableStateFlow<DataResource<List<com.cybernerd404.fairsplit.domain.model.ActivityEntry>>>(DataResource.loading())
    val activitiesState = _activitiesState.asStateFlow()

    private val _selectedTab = MutableStateFlow("All Activity") // "All Activity", "Settlements"
    val selectedTab = _selectedTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        screenModelScope.launch {
            _isRefreshing.value = true
            val txResult = getTransactionsUseCase()
            _transactionsState.value = txResult

            val actResult = getAllActivitiesUseCase()
            _activitiesState.value = actResult
            _isRefreshing.value = false
        }
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun loadData() {
        screenModelScope.launch {
            if (_transactionsState.value.data == null) {
                _transactionsState.value = DataResource.loading()
            }
            if (_activitiesState.value.data == null) {
                _activitiesState.value = DataResource.loading()
            }

            val txResult = getTransactionsUseCase()
            _transactionsState.value = txResult

            val actResult = getAllActivitiesUseCase()
            _activitiesState.value = actResult
        }
    }
}
