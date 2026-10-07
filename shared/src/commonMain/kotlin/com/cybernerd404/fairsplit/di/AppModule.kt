package com.cybernerd404.fairsplit.di

import com.cybernerd404.fairsplit.domain.auth.googleAuthModule
import com.cybernerd404.fairsplit.domain.notification.notificationModule

fun appModule() = listOf(
    networkModule,
    notificationModule,
    googleAuthModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
