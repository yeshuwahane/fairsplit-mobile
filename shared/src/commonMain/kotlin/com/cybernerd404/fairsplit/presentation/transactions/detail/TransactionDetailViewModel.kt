package com.cybernerd404.fairsplit.presentation.transactions.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.domain.usecase.DeleteTransactionUseCase
import com.cybernerd404.fairsplit.domain.usecase.GetTransactionByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.cybernerd404.fairsplit.domain.usecase.GetEpicDetailUseCase
import com.cybernerd404.fairsplit.domain.usecase.UpdateTransactionUseCase

class TransactionDetailViewModel(
    private val transactionId: String,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getEpicDetailUseCase: GetEpicDetailUseCase? = null
) : ScreenModel {

    private val _transactionState = MutableStateFlow<DataResource<Transaction?>>(DataResource.loading())
    val transactionState: StateFlow<DataResource<Transaction?>> = _transactionState.asStateFlow()

    private val _effectiveMaxAmount = MutableStateFlow<Double?>(null)
    val effectiveMaxAmount: StateFlow<Double?> = _effectiveMaxAmount.asStateFlow()

    fun loadTransaction(id: String = transactionId) {
        screenModelScope.launch {
            _transactionState.value = DataResource.loading()
            val result = getTransactionByIdUseCase(id)
            _transactionState.value = result

            val tx = result.data
            if (tx != null && tx.epicId.isNotEmpty() && getEpicDetailUseCase != null) {
                val epicResult = getEpicDetailUseCase(tx.epicId)
                if (epicResult.isSuccess() && epicResult.data != null) {
                    val epic = epicResult.data!!
                    val pendingDebt = epic.settlements.firstOrNull {
                        it.fromMember.id == tx.fromMember.id && it.toMember.id == tx.toMember.id
                    }?.amount ?: 0.0
                    // Effective outstanding debt before this settlement was made:
                    _effectiveMaxAmount.value = pendingDebt + tx.amount
                }
            }
        }
    }

    fun updateAmount(newAmount: Double, newNote: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            val currentTx = _transactionState.value.data
            if (currentTx == null) {
                onError("Transaction not found")
                return@launch
            }
            if (newAmount <= 0) {
                onError("Amount must be greater than zero. To remove, use Delete Settlement.")
                return@launch
            }
            val maxAllowed = _effectiveMaxAmount.value
            if (maxAllowed != null && newAmount > maxAllowed) {
                onError("Amount cannot exceed outstanding bilateral debt (₹${maxAllowed.toInt()})")
                return@launch
            }

            val updated = currentTx.copy(
                amount = newAmount,
                subtitle = newNote ?: currentTx.subtitle
            )
            val result = updateTransactionUseCase(updated)
            if (result.isSuccess() && result.data != null) {
                _transactionState.value = DataResource.success(result.data)
                onSuccess()
            } else {
                onError(result.error?.message ?: "Failed to update settlement")
            }
        }
    }

    fun deleteTransaction(onSuccess: () -> Unit) {
        screenModelScope.launch {
            val result = deleteTransactionUseCase(transactionId)
            if (result.isSuccess() && result.data == true) {
                onSuccess()
            }
        }
    }
}
