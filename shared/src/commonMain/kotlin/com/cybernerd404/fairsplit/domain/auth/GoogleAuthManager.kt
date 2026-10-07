package com.cybernerd404.fairsplit.domain.auth

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

data class GoogleUserCredentials(
    val idToken: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)

sealed class GoogleSignInResult {
    data class Success(val credentials: GoogleUserCredentials) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
}

/**
 * Platform-independent Google Sign-In abstraction.
 */
interface GoogleAuthManager {
    suspend fun signIn(): GoogleSignInResult
    suspend fun signOut()
}

interface GoogleAuthLauncher {
    fun launch()
}

/**
 * Composable multiplatform launcher for Google Sign-In intent / native sheet.
 */
@Composable
expect fun rememberGoogleAuthLauncher(
    onResult: (GoogleUserCredentials?, String?) -> Unit
): GoogleAuthLauncher

/**
 * Platform Koin module providing the platform GoogleAuthManager implementation.
 */
expect val googleAuthModule: Module
