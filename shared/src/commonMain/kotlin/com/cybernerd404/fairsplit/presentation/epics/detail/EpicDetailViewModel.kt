package com.cybernerd404.fairsplit.presentation.epics.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.cybernerd404.fairsplit.data.remote.realtime.RealtimeDataSource
import com.cybernerd404.fairsplit.data.remote.realtime.RealtimeEventType

enum class EpicDetailTab(val title: String) {
    OVERVIEW("Overview"),
    EXPENSES("Expenses"),
    BALANCES("Balances"),
    ACTIVITY("Activity")
}

class EpicDetailViewModel(
    private val epicId: String,
    private val getEpicDetailUseCase: GetEpicDetailUseCase,
    private val getExpensesForEpicUseCase: GetExpensesForEpicUseCase,
    private val addMemberToEpicUseCase: AddMemberToEpicUseCase,
    private val removeMemberFromEpicUseCase: RemoveMemberFromEpicUseCase,
    private val recordSettlementUseCase: RecordSettlementUseCase,
    private val getEpicActivitiesUseCase: GetEpicActivitiesUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase? = null,
    private val realtimeDataSource: RealtimeDataSource? = null,
    private val deleteEpicUseCase: com.cybernerd404.fairsplit.domain.usecase.DeleteEpicUseCase? = null
) : ScreenModel {

    private val _epicState = MutableStateFlow<DataResource<Epic>>(DataResource.loading())
    val epicState = _epicState.asStateFlow()

    private val _expensesState = MutableStateFlow<DataResource<List<Expense>>>(DataResource.loading())
    val expensesState = _expensesState.asStateFlow()

    private val _activitiesState = MutableStateFlow<DataResource<List<com.cybernerd404.fairsplit.domain.model.ActivityEntry>>>(DataResource.loading())
    val activitiesState = _activitiesState.asStateFlow()

    private val _selectedTab = MutableStateFlow(EpicDetailTab.OVERVIEW)
    val selectedTab = _selectedTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadData()
        startRealtimeSubscription()
    }

    private var pendingRefreshExpenses = false
    private var pendingRefreshEpic = false
    private var pendingRefreshActivities = false
    private var pendingRefreshTransactions = false
    private var debounceJob: kotlinx.coroutines.Job? = null
    private var realtimeJob: kotlinx.coroutines.Job? = null

    fun startRealtimeSubscription() {
        if (realtimeDataSource == null) return
        if (realtimeJob?.isActive == true) return

        realtimeDataSource.connectToEpic(epicId)
        realtimeJob = screenModelScope.launch {
            realtimeDataSource.events.collect { event ->
                if (event.epicId == epicId) {
                    io.github.aakira.napier.Napier.d(tag = "FAIRSPLIT_REALTIME", message = "WebSocket EVENT TYPE: ${event.type} for epic ${event.epicId}")
                    when (event.type) {
                        RealtimeEventType.EXPENSE_CREATED,
                        RealtimeEventType.EXPENSE_UPDATED,
                        RealtimeEventType.EXPENSE_DELETED -> {
                            scheduleCoalescedRefresh(expenses = true, epic = true, activities = true)
                        }
                        RealtimeEventType.MEMBER_JOINED,
                        RealtimeEventType.MEMBER_REMOVED,
                        RealtimeEventType.EPIC_UPDATED -> {
                            scheduleCoalescedRefresh(epic = true, activities = true)
                        }
                        RealtimeEventType.SETTLEMENT_CREATED,
                        RealtimeEventType.BALANCE_UPDATED -> {
                            scheduleCoalescedRefresh(epic = true, transactions = true, activities = true, expenses = true)
                        }
                        RealtimeEventType.ACTIVITY_CREATED -> {
                            scheduleCoalescedRefresh(activities = true)
                        }
                        RealtimeEventType.CONNECTED -> {
                            // Upon WebSocket connect or reconnect, do a full authoritative refresh
                            scheduleCoalescedRefresh(epic = true, expenses = true, activities = true, transactions = true)
                        }
                        else -> {
                            scheduleCoalescedRefresh(epic = true, expenses = true, activities = true)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleCoalescedRefresh(
        epic: Boolean = false,
        expenses: Boolean = false,
        activities: Boolean = false,
        transactions: Boolean = false
    ) {
        if (epic) pendingRefreshEpic = true
        if (expenses) pendingRefreshExpenses = true
        if (activities) pendingRefreshActivities = true
        if (transactions) pendingRefreshTransactions = true

        debounceJob?.cancel()
        debounceJob = screenModelScope.launch {
            kotlinx.coroutines.delay(100) // coalesce bursts of events occurring within 100ms window
            val doEpic = pendingRefreshEpic
            val doExpenses = pendingRefreshExpenses
            val doActivities = pendingRefreshActivities
            val doTransactions = pendingRefreshTransactions

            pendingRefreshEpic = false
            pendingRefreshExpenses = false
            pendingRefreshActivities = false
            pendingRefreshTransactions = false

            io.github.aakira.napier.Napier.d(
                tag = "FAIRSPLIT_REALTIME",
                message = "REST REFRESH START: epicId=$epicId, doEpic=$doEpic, doExpenses=$doExpenses, doActivities=$doActivities, doTransactions=$doTransactions"
            )

            if (doEpic) {
                val epicResult = getEpicDetailUseCase(epicId)
                if (epicResult.isSuccess() && epicResult.data != null) {
                    _epicState.value = epicResult
                    io.github.aakira.napier.Napier.d(
                        tag = "FAIRSPLIT_REALTIME",
                        message = "STATE UPDATED: epic=${epicResult.data?.name}, members=${epicResult.data?.memberCount}"
                    )
                }
            }
            if (doExpenses) {
                val expensesResult = getExpensesForEpicUseCase(epicId)
                if (expensesResult.isSuccess() && expensesResult.data != null) {
                    _expensesState.value = expensesResult
                    io.github.aakira.napier.Napier.d(
                        tag = "FAIRSPLIT_REALTIME",
                        message = "STATE UPDATED: expenses count=${expensesResult.data?.size}"
                    )
                }
            }
            if (doActivities) {
                val activitiesResult = getEpicActivitiesUseCase(epicId)
                if (activitiesResult.isSuccess() && activitiesResult.data != null) {
                    _activitiesState.value = activitiesResult
                    io.github.aakira.napier.Napier.d(
                        tag = "FAIRSPLIT_REALTIME",
                        message = "STATE UPDATED: activities count=${activitiesResult.data?.size}"
                    )
                }
            }
            if (doTransactions) {
                getTransactionsUseCase?.invoke()
            }

            io.github.aakira.napier.Napier.d(
                tag = "FAIRSPLIT_REALTIME",
                message = "REST REFRESH COMPLETE: epicId=$epicId"
            )
        }
    }

    override fun onDispose() {
        debounceJob?.cancel()
        debounceJob = null
        realtimeJob?.cancel()
        realtimeJob = null
        realtimeDataSource?.disconnect()
    }

    fun refresh(id: String = epicId) {
        screenModelScope.launch {
            _isRefreshing.value = true
            val epicResult = getEpicDetailUseCase(id)
            if (epicResult.isSuccess() && epicResult.data != null) {
                _epicState.value = epicResult
            }

            val expensesResult = getExpensesForEpicUseCase(id)
            if (expensesResult.isSuccess() && expensesResult.data != null) {
                _expensesState.value = expensesResult
            }

            val activitiesResult = getEpicActivitiesUseCase(id)
            if (activitiesResult.isSuccess() && activitiesResult.data != null) {
                _activitiesState.value = activitiesResult
            }
            getTransactionsUseCase?.invoke()
            _isRefreshing.value = false
        }
    }

    fun setSelectedTab(tab: EpicDetailTab) {
        _selectedTab.value = tab
    }

    fun loadData(id: String = epicId) {
        screenModelScope.launch {
            _epicState.value = DataResource.loading()
            _expensesState.value = DataResource.loading()
            _activitiesState.value = DataResource.loading()

            val epicResult = getEpicDetailUseCase(id)
            _epicState.value = epicResult

            val expensesResult = getExpensesForEpicUseCase(id)
            _expensesState.value = expensesResult

            val activitiesResult = getEpicActivitiesUseCase(id)
            _activitiesState.value = activitiesResult
            getTransactionsUseCase?.invoke()
        }
    }

    fun refreshSilently(id: String = epicId) {
        screenModelScope.launch {
            val epicResult = getEpicDetailUseCase(id)
            if (epicResult.isSuccess() && epicResult.data != null) {
                _epicState.value = epicResult
            }

            val expensesResult = getExpensesForEpicUseCase(id)
            if (expensesResult.isSuccess() && expensesResult.data != null) {
                _expensesState.value = expensesResult
            }

            val activitiesResult = getEpicActivitiesUseCase(id)
            if (activitiesResult.isSuccess() && activitiesResult.data != null) {
                _activitiesState.value = activitiesResult
            }
            getTransactionsUseCase?.invoke()
        }
    }

    fun addMember(memberName: String) {
        screenModelScope.launch {
            val result = addMemberToEpicUseCase(epicId, memberName)
            if (result.isSuccess() && result.data != null) {
                _epicState.value = result
                loadData()
            }
        }
    }

    fun removeMember(memberId: String) {
        screenModelScope.launch {
            val result = removeMemberFromEpicUseCase(epicId, memberId)
            if (result.isSuccess() && result.data != null) {
                _epicState.value = result
                val expResult = getExpensesForEpicUseCase(epicId)
                _expensesState.value = expResult
                loadData()
            }
        }
    }

    fun settleDebt(payer: Member, receiver: Member, amount: Double, note: String?, receiptUrl: String? = null) {
        screenModelScope.launch {
            val result = recordSettlementUseCase(
                epicId = epicId,
                payerId = payer.id,
                receiverId = receiver.id,
                amount = amount,
                note = note,
                receiptUrl = receiptUrl
            )
            if (result.isSuccess()) {
                getTransactionsUseCase?.invoke()
                loadData()
            }
        }
    }

    fun deleteEpic(onSuccess: () -> Unit) {
        screenModelScope.launch {
            val result = deleteEpicUseCase?.invoke(epicId)
            if (result?.isSuccess() == true && result.data == true) {
                onSuccess()
            }
        }
    }
}
