package com.cybernerd404.fairsplit.presentation.expense.add

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.*
import com.cybernerd404.fairsplit.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val initialEpicId: String?,
    private val existingExpenseId: String?,
    private val getEpicsUseCase: GetEpicsUseCase,
    private val getEpicDetailUseCase: GetEpicDetailUseCase,
    private val getExpenseDetailUseCase: GetExpenseDetailUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val editExpenseUseCase: EditExpenseUseCase
) : ScreenModel {

    val isEditMode = existingExpenseId != null

    private val _epics = MutableStateFlow<List<Epic>>(emptyList())
    val epics = _epics.asStateFlow()

    private val _selectedEpic = MutableStateFlow<Epic?>(null)
    val selectedEpic = _selectedEpic.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText = _amountText.asStateFlow()

    private val _expenseName = MutableStateFlow("")
    val expenseName = _expenseName.asStateFlow()

    private val _selectedPayer = MutableStateFlow<Member?>(null)
    val selectedPayer = _selectedPayer.asStateFlow()

    private val _selectedParticipantIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedParticipantIds = _selectedParticipantIds.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExpenseCategory.FOOD)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _receiptUrl = MutableStateFlow<String?>(null)
    val receiptUrl = _receiptUrl.asStateFlow()

    private val _hasReceipt = MutableStateFlow(false)
    val hasReceipt = _hasReceipt.asStateFlow()

    private val _note = MutableStateFlow("")
    val note = _note.asStateFlow()

    private val _saveState = MutableStateFlow<DataResource<Expense>>(DataResource.initial())
    val saveState = _saveState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        screenModelScope.launch {
            val epicsResult = getEpicsUseCase()
            val list = epicsResult.data ?: emptyList()
            _epics.value = list

            if (isEditMode && existingExpenseId != null) {
                // Load existing expense to populate form
                val expResult = getExpenseDetailUseCase(existingExpenseId)
                val expense = expResult.data
                if (expense != null) {
                    val epic = list.firstOrNull { it.id == expense.epicId } ?: list.firstOrNull()
                    _selectedEpic.value = epic
                    _amountText.value = if (expense.amount % 1.0 == 0.0) expense.amount.toInt().toString() else expense.amount.toString()
                    _expenseName.value = expense.title
                    _selectedPayer.value = expense.paidBy
                    _selectedParticipantIds.value = expense.participants.map { it.id }.toSet()
                    _selectedCategory.value = expense.category
                    _receiptUrl.value = expense.receiptUrl
                    _hasReceipt.value = expense.receiptUrl != null
                    _note.value = expense.note ?: ""
                    return@launch
                }
            }

            // Normal Add Mode
            var defaultEpic = if (initialEpicId != null) {
                list.firstOrNull { it.id == initialEpicId } ?: list.firstOrNull()
            } else {
                list.firstOrNull()
            }

            val targetId = defaultEpic?.id ?: initialEpicId
            if (targetId != null) {
                val freshEpicRes = getEpicDetailUseCase(targetId)
                if (freshEpicRes.isSuccess() && freshEpicRes.data != null) {
                    defaultEpic = freshEpicRes.data
                }
            }

            _selectedEpic.value = defaultEpic

            if (defaultEpic != null) {
                _selectedPayer.value = defaultEpic.members.firstOrNull { it.isCurrentUser } ?: defaultEpic.members.firstOrNull()
                _selectedParticipantIds.value = defaultEpic.members.map { it.id }.toSet()
            }
        }
    }

    fun onEpicSelected(epic: Epic) {
        _selectedEpic.value = epic
        _selectedPayer.value = epic.members.firstOrNull { it.isCurrentUser } ?: epic.members.firstOrNull()
        _selectedParticipantIds.value = epic.members.map { it.id }.toSet()
        screenModelScope.launch {
            val freshRes = getEpicDetailUseCase(epic.id)
            if (freshRes.isSuccess() && freshRes.data != null) {
                val fresh = freshRes.data!!
                _selectedEpic.value = fresh
                if (_selectedPayer.value == null || fresh.members.none { it.id == _selectedPayer.value?.id }) {
                    _selectedPayer.value = fresh.members.firstOrNull { it.isCurrentUser } ?: fresh.members.firstOrNull()
                }
                _selectedParticipantIds.value = fresh.members.map { it.id }.toSet()
            }
        }
    }

    fun onAmountChanged(amount: String) {
        _amountText.value = amount.filter { it.isDigit() || it == '.' }
    }

    fun onExpenseNameChanged(name: String) {
        _expenseName.value = name
    }

    fun onPayerSelected(member: Member) {
        _selectedPayer.value = member
    }

    fun toggleParticipant(memberId: String) {
        val current = _selectedParticipantIds.value.toMutableSet()
        if (current.contains(memberId)) {
            if (current.size > 1) { // must keep at least 1 participant
                current.remove(memberId)
            }
        } else {
            current.add(memberId)
        }
        _selectedParticipantIds.value = current
    }

    fun selectAllParticipants() {
        val epic = _selectedEpic.value ?: return
        _selectedParticipantIds.value = epic.members.map { it.id }.toSet()
    }

    fun onCategorySelected(category: ExpenseCategory) {
        _selectedCategory.value = category
    }

    fun onReceiptSelected(url: String?) {
        _receiptUrl.value = url
        _hasReceipt.value = url != null
    }

    fun toggleReceipt() {
        if (_receiptUrl.value != null) {
            _receiptUrl.value = null
            _hasReceipt.value = false
        } else {
            _receiptUrl.value = "receipt_photo_${(1000..9999).random()}.jpg"
            _hasReceipt.value = true
        }
    }

    fun onNoteChanged(newNote: String) {
        _note.value = newNote
    }

    fun getPerPersonAmount(): Double {
        val amount = _amountText.value.toDoubleOrNull() ?: 0.0
        val count = _selectedParticipantIds.value.size
        return if (count > 0) amount / count else 0.0
    }

    fun submitExpense(onSuccess: (Expense) -> Unit) {
        val epic = _selectedEpic.value ?: return
        val payer = _selectedPayer.value ?: return
        val amount = _amountText.value.toDoubleOrNull() ?: return
        val title = _expenseName.value.trim().ifEmpty { "Expense" }

        screenModelScope.launch {
            _saveState.value = DataResource.loading()
            val result = if (isEditMode && existingExpenseId != null) {
                editExpenseUseCase(
                    expenseId = existingExpenseId,
                    title = title,
                    amount = amount,
                    payerId = payer.id,
                    participantIds = _selectedParticipantIds.value.toList(),
                    category = _selectedCategory.value,
                    note = _note.value.ifBlank { null },
                    receiptUrl = _receiptUrl.value
                )
            } else {
                addExpenseUseCase(
                    epicId = epic.id,
                    title = title,
                    amount = amount,
                    payerId = payer.id,
                    participantIds = _selectedParticipantIds.value.toList(),
                    category = _selectedCategory.value,
                    note = _note.value.ifBlank { null },
                    receiptUrl = _receiptUrl.value
                )
            }

            _saveState.value = result
            if (result.isSuccess() && result.data != null) {
                onSuccess(result.data)
            }
        }
    }
}
