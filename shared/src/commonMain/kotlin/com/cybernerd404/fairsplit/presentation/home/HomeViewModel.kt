package com.cybernerd404.fairsplit.presentation.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.usecase.GetHomeOverviewUseCase
import com.cybernerd404.fairsplit.domain.usecase.HomeOverviewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeOverviewUseCase: GetHomeOverviewUseCase
) : ScreenModel {

    private val _overviewState = MutableStateFlow<DataResource<HomeOverviewData>>(DataResource.loading())
    val overviewState = _overviewState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        screenModelScope.launch {
            if (_overviewState.value.data == null) {
                _overviewState.value = DataResource.loading()
            }
            val result = getHomeOverviewUseCase()
            _overviewState.value = result
        }
    }

    fun refresh() {
        screenModelScope.launch {
            _isRefreshing.value = true
            val result = getHomeOverviewUseCase()
            _overviewState.value = result
            _isRefreshing.value = false
        }
    }
}
