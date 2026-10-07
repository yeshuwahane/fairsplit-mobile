package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.runtime.Composable

interface ShareLauncher {
    fun share(text: String, title: String = "Share")
}

@Composable
expect fun rememberShareLauncher(): ShareLauncher
