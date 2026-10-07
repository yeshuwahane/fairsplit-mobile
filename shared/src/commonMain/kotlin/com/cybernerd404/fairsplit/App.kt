package com.cybernerd404.fairsplit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.cybernerd404.fairsplit.di.appModule
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.FairSplitSlideTransition
import com.cybernerd404.fairsplit.presentation.splash.SplashScreen
import com.cybernerd404.fairsplit.theme.FairSplitTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.compose.KoinApplication
import org.koin.compose.KoinContext
import org.koin.mp.KoinPlatformTools

@Composable
fun App() {
    remember {
        try {
            Napier.base(DebugAntilog())
        } catch (_: Exception) {}
    }

    val isKoinStarted = remember {
        try {
            KoinPlatformTools.defaultContext().getOrNull() != null
        } catch (_: Exception) {
            false
        }
    }

    if (isKoinStarted) {
        KoinContext {
            AppContent()
        }
    } else {
        KoinApplication(
            application = {
                modules(appModule())
            }
        ) {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    FairSplitTheme(darkTheme = true) {
        AppBackground(
            modifier = Modifier.fillMaxSize(),
            applyStatusBarPadding = false
        ) {
            Navigator(SplashScreen()) { navigator ->
                FairSplitSlideTransition(navigator)

                val pendingInviteCode by com.cybernerd404.fairsplit.domain.deeplink.DeepLinkManager.pendingInviteCode.collectAsState()
                if (pendingInviteCode != null) {
                    com.cybernerd404.fairsplit.presentation.epics.JoinEpicModal(
                        initialCode = pendingInviteCode,
                        onDismiss = {
                            com.cybernerd404.fairsplit.domain.deeplink.DeepLinkManager.clearPendingInviteCode()
                        },
                        onJoined = { epic ->
                            com.cybernerd404.fairsplit.domain.deeplink.DeepLinkManager.clearPendingInviteCode()
                            navigator.push(com.cybernerd404.fairsplit.presentation.epics.detail.EpicDetailScreen(epicId = epic.id))
                        }
                    )
                }
            }
        }
    }
}
