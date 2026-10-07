package com.cybernerd404.fairsplit.di

import com.cybernerd404.fairsplit.domain.usecase.*
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetHomeOverviewUseCase(get(), get(), get()) }
    factory { GetEpicsUseCase(get()) }
    factory { GetEpicDetailUseCase(get()) }
    factory { CreateEpicUseCase(get()) }
    factory { AddMemberToEpicUseCase(get()) }
    factory { RemoveMemberFromEpicUseCase(get()) }
    factory { GetEpicByInviteCodeUseCase(get()) }
    factory { JoinEpicByInviteCodeUseCase(get()) }
    factory { DeleteEpicUseCase(get()) }
    factory { UpdateEpicUseCase(get()) }
    factory { GetRecentPeersUseCase(get()) }
    factory { GetExpensesForEpicUseCase(get()) }
    factory { GetExpenseDetailUseCase(get()) }
    factory { AddExpenseUseCase(get()) }
    factory { EditExpenseUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
    factory { RecordSettlementUseCase(get()) }
    factory { GetTransactionsUseCase(get()) }
    factory { GetTransactionByIdUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { UpdateUserProfileUseCase(get()) }
    factory { GetUserPreferencesUseCase(get()) }
    factory { UpdateUserPreferencesUseCase(get()) }
    factory { GetEpicActivitiesUseCase(get()) }
    factory { GetAllActivitiesUseCase(get()) }

    // Auth Use Cases
    factory { RequestPhoneOtpUseCase(get()) }
    factory { VerifyPhoneOtpUseCase(get()) }
    factory { GoogleSignInUseCase(get()) }
    factory { LinkGoogleAccountUseCase(get()) }
    factory { LinkPhoneAccountUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { GetAuthSessionUseCase(get()) }
    factory { LogoutUseCase(get()) }
}
