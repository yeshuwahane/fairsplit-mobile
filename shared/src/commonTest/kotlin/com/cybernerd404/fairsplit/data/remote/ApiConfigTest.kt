package com.cybernerd404.fairsplit.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiConfigTest {

    @Test
    fun testProductionBackendUrlIsConfigured() {
        assertFalse(ApiConfig.useLocalServer, "useLocalServer should default to false for production")
        assertEquals("https://fairplit-backend-production.up.railway.app", ApiConfig.baseUrl)
        assertEquals("wss://fairplit-backend-production.up.railway.app", ApiConfig.webSocketBaseUrl)
    }

    @Test
    fun testEndpointUrlsAreConstructedCorrectlyWithoutDuplicatePrefixes() {
        assertEquals("https://fairplit-backend-production.up.railway.app/health", ApiConfig.healthUrl)
        assertEquals("https://fairplit-backend-production.up.railway.app/api/v1/auth/phone/request-otp", ApiConfig.requestOtpUrl)
        assertEquals("https://fairplit-backend-production.up.railway.app/api/v1/auth/phone/verify-otp", ApiConfig.verifyOtpUrl)
        assertEquals("https://fairplit-backend-production.up.railway.app/api/v1/auth/refresh", ApiConfig.refreshTokenUrl)
        assertEquals("https://fairplit-backend-production.up.railway.app/api/v1/auth/logout", ApiConfig.logoutUrl)
    }

    @Test
    fun testProductionWebSocketUrlUsesWssAndCorrectPath() {
        val epicId = "test-epic-1234"
        val wsUrl = ApiConfig.epicWebSocketUrl(epicId)
        assertTrue(wsUrl.startsWith("wss://"), "Production WebSocket must use wss:// scheme")
        assertFalse(wsUrl.contains("ws://"), "Production WebSocket must not contain plain ws://")
        assertEquals(
            "wss://fairplit-backend-production.up.railway.app/api/v1/ws/epics/$epicId",
            wsUrl
        )
    }
}
