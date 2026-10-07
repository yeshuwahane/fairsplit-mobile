package com.cybernerd404.fairsplit.di

import com.cybernerd404.fairsplit.presentation.epics.EpicsViewModel
import com.cybernerd404.fairsplit.presentation.epics.create.CreateEpicViewModel
import com.cybernerd404.fairsplit.presentation.epics.detail.EpicDetailViewModel
import com.cybernerd404.fairsplit.presentation.expense.add.AddExpenseViewModel
import com.cybernerd404.fairsplit.presentation.expense.detail.ExpenseDetailViewModel
import com.cybernerd404.fairsplit.presentation.home.HomeViewModel
import com.cybernerd404.fairsplit.presentation.settings.SettingsViewModel
import com.cybernerd404.fairsplit.presentation.transactions.TransactionsViewModel
import com.cybernerd404.fairsplit.presentation.transactions.detail.TransactionDetailViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { HomeViewModel(get()) }
    single { EpicsViewModel(get()) }
    factory { (epicId: String) -> EpicDetailViewModel(epicId, get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { CreateEpicViewModel(get(), get()) }
    factory { (expenseId: String) -> ExpenseDetailViewModel(expenseId, get(), get()) }
    factory { (initialEpicId: String?, existingExpenseId: String?) ->
        AddExpenseViewModel(initialEpicId, existingExpenseId, get(), get(), get(), get(), get())
    }
    single { TransactionsViewModel(get(), get()) }
    factory { (transactionId: String) -> TransactionDetailViewModel(transactionId, get(), get(), get(), get()) }
    single { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { com.cybernerd404.fairsplit.presentation.auth.AuthViewModel(get(), get(), get(), get(), get()) }
    factory { com.cybernerd404.fairsplit.presentation.auth.ProfileSetupViewModel(get(), get()) }
}
