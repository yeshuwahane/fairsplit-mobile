package com.cybernerd404.fairsplit.domain.deeplink

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeepLinkManager {
    private val _pendingInviteCode = MutableStateFlow<String?>(null)
    val pendingInviteCode = _pendingInviteCode.asStateFlow()

    fun setPendingInviteCode(rawUriOrCode: String?) {
        if (rawUriOrCode.isNullOrBlank()) {
            _pendingInviteCode.value = null
            return
        }
        val trimmed = rawUriOrCode.trim()
        val extracted = when {
            trimmed.contains("code=") -> trimmed.substringAfter("code=").substringBefore("&").trim()
            trimmed.contains("/join/") -> trimmed.substringAfter("/join/").substringBefore("?").trim()
            else -> trimmed
        }
        Napier.i(tag = "FAIRSPLIT_DEEPLINK", message = "Deep link received: $rawUriOrCode -> extracted code: $extracted")
        _pendingInviteCode.value = extracted.ifBlank { null }
    }

    fun clearPendingInviteCode() {
        Napier.d(tag = "FAIRSPLIT_DEEPLINK", message = "Clearing pending invite code")
        _pendingInviteCode.value = null
    }
}
