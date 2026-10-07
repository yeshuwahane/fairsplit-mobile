package com.cybernerd404.fairsplit.domain.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.dsl.module

class IosGoogleAuthManager : GoogleAuthManager {

    override suspend fun signIn(): GoogleSignInResult {
        return GoogleSignInResult.Error("Google Sign-In on iOS requires native integration.")
    }

    override suspend fun signOut() {
        Napier.i(tag = "FAIRSPLIT_GOOGLE_AUTH_IOS", message = "Google Sign-Out executed on iOS")
    }
}

@Composable
actual fun rememberGoogleAuthLauncher(
    onResult: (GoogleUserCredentials?, String?) -> Unit
): GoogleAuthLauncher {
    return remember {
        object : GoogleAuthLauncher {
            override fun launch() {
                Napier.i(tag = "FAIRSPLIT_GOOGLE_AUTH_IOS", message = "Launching Google Sign-In on iOS...")
                onResult(
                    null,
                    "Google Sign-In on iOS is available via native account picker."
                )
            }
        }
    }
}

actual val googleAuthModule: Module = module {
    single<GoogleAuthManager> { IosGoogleAuthManager() }
}
