package com.cybernerd404.fairsplit.presentation.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareLauncher(): ShareLauncher {
    val context = LocalContext.current
    return remember(context) {
        object : ShareLauncher {
            override fun share(text: String, title: String) {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, title)
                context.startActivity(shareIntent)
            }
        }
    }
}
