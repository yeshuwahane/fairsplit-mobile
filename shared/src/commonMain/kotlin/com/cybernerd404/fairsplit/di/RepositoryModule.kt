package com.cybernerd404.fairsplit.di

import com.cybernerd404.fairsplit.data.repository.*
import com.cybernerd404.fairsplit.domain.auth.DevelopmentOtpProvider
import com.cybernerd404.fairsplit.domain.auth.OtpProvider
import com.cybernerd404.fairsplit.domain.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<OtpProvider> { DevelopmentOtpProvider(devOtpEnabled = true, defaultOtp = "123456") }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get()) }
    single<EpicRepository> { EpicRepositoryImpl(get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<ActivityRepository> { ActivityRepositoryImpl(get()) }
}
