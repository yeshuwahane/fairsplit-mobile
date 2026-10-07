package com.cybernerd404.fairsplit.presentation.expense.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.domain.usecase.DeleteExpenseUseCase
import com.cybernerd404.fairsplit.domain.usecase.GetExpenseDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseDetailViewModel(
    private val expenseId: String,
    private val getExpenseDetailUseCase: GetExpenseDetailUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ScreenModel {

    private val _expenseState = MutableStateFlow<DataResource<Expense>>(DataResource.loading())
    val expenseState = _expenseState.asStateFlow()

    private val _deleteState = MutableStateFlow<DataResource<Boolean>>(DataResource.initial())
    val deleteState = _deleteState.asStateFlow()

    private val _showReceiptModal = MutableStateFlow(false)
    val showReceiptModal = _showReceiptModal.asStateFlow()

    init {
        loadExpense()
    }

    fun loadExpense(id: String = expenseId) {
        screenModelScope.launch {
            _expenseState.value = DataResource.loading()
            val result = getExpenseDetailUseCase(id)
            _expenseState.value = result
        }
    }

    fun deleteExpense(onSuccess: () -> Unit) {
        screenModelScope.launch {
            _deleteState.value = DataResource.loading()
            val result = deleteExpenseUseCase(expenseId)
            _deleteState.value = result
            if (result.isSuccess()) {
                onSuccess()
            }
        }
    }

    fun openReceipt() {
        _showReceiptModal.value = true
    }

    fun closeReceipt() {
        _showReceiptModal.value = false
    }
}
