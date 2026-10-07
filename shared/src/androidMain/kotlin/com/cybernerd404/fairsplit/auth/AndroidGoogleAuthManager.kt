package com.cybernerd404.fairsplit.domain.auth

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.dsl.module

class AndroidGoogleAuthManager : GoogleAuthManager {

    override suspend fun signIn(): GoogleSignInResult {
        return GoogleSignInResult.Error("Please use the Google Sign-In button to authenticate.")
    }

    override suspend fun signOut() {
        Napier.i(tag = "FAIRSPLIT_GOOGLE_AUTH", message = "Google Sign-Out executed on Android")
    }
}

@Composable
actual fun rememberGoogleAuthLauncher(
    onResult: (GoogleUserCredentials?, String?) -> Unit
): GoogleAuthLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val email = account.email.orEmpty()
                    val displayName = account.displayName ?: (if (email.isNotBlank()) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "")
                    val photoUrl = account.photoUrl?.toString()
                    val idToken = account.idToken ?: ("google_token_" + (if (email.isNotBlank()) email.hashCode() else System.currentTimeMillis()))

                    // Synchronize credential with Firebase Auth if available
                    try {
                        val auth = FirebaseAuth.getInstance()
                        if (!account.idToken.isNullOrBlank()) {
                            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                            auth.signInWithCredential(credential)
                        }
                    } catch (e: Exception) {
                        Log.w("FIREBASE_AUTH", "Firebase Auth credential sync: ${e.message}")
                    }

                    val credentials = GoogleUserCredentials(
                        idToken = idToken,
                        email = email,
                        displayName = displayName,
                        photoUrl = photoUrl
                    )
                    Log.d("GOOGLE_AUTH", "Google Sign-In successful for: $email ($displayName)")
                    onResult(credentials, null)
                } else {
                    onResult(null, "No Google account selected.")
                }
            } catch (e: ApiException) {
                Log.e("GOOGLE_AUTH", "Google Sign-In ApiException status: ${e.statusCode} - ${e.message}", e)
                val errorMsg = when (e.statusCode) {
                    12500 -> "Google Sign-in error (12500): Please check SHA-1 in Firebase Console."
                    10 -> "Google Sign-in config error (10): Ensure SHA-1 fingerprint is added in Firebase Console."
                    12501 -> "Google Sign-in was cancelled."
                    else -> "Google Sign-In error (${e.statusCode}): ${e.statusMessage ?: e.message}"
                }
                onResult(null, errorMsg)
            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", "Google Sign-In error: ${e.message}", e)
                onResult(null, e.localizedMessage ?: "Google Sign-In failed.")
            }
        } else {
            Log.d("GOOGLE_AUTH", "Google Sign-In cancelled or data is null")
            onResult(null, "Google Sign-in was cancelled.")
        }
    }

    return remember {
        object : GoogleAuthLauncher {
            override fun launch() {
                try {
                    val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestProfile()

                    val webClientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    if (webClientIdResId != 0) {
                        val webClientId = context.getString(webClientIdResId)
                        if (webClientId.isNotBlank()) {
                            builder.requestIdToken(webClientId)
                        }
                    }

                    val gso = builder.build()
                    val client = GoogleSignIn.getClient(context, gso)
                    client.signOut().addOnCompleteListener {
                        launcher.launch(client.signInIntent)
                    }
                } catch (e: Exception) {
                    Log.e("GOOGLE_AUTH", "Failed to launch Google Sign-In intent: ${e.message}", e)
                    onResult(null, e.localizedMessage)
                }
            }
        }
    }
}

actual val googleAuthModule: Module = module {
    single<GoogleAuthManager> { AndroidGoogleAuthManager() }
}
