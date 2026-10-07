package com.cybernerd404.fairsplit.data.remote.realtime

import com.cybernerd404.fairsplit.data.local.TokenStorage
import com.cybernerd404.fairsplit.data.remote.ApiConfig
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
enum class RealtimeEventType {
    EPIC_UPDATED,
    EPIC_DELETED,
    MEMBER_JOINED,
    MEMBER_REMOVED,
    EXPENSE_CREATED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    SETTLEMENT_CREATED,
    BALANCE_UPDATED,
    ACTIVITY_CREATED,
    CONNECTED
}

@Serializable
data class RealtimeEventDto(
    val type: RealtimeEventType,
    val epicId: String,
    val entityId: String? = null,
    val actorId: String? = null,
    val timestamp: String = ""
)

interface RealtimeDataSource {
    val events: SharedFlow<RealtimeEventDto>
    fun connectToEpic(epicId: String)
    fun disconnect()
}

class RealtimeDataSourceImpl(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : RealtimeDataSource {

    private val _events = MutableSharedFlow<RealtimeEventDto>(extraBufferCapacity = 64)
    override val events: SharedFlow<RealtimeEventDto> = _events.asSharedFlow()

    private var connectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }


    override fun connectToEpic(epicId: String) {
        disconnect()

        connectionJob = scope.launch {
            var backoffMillis = 1000L
            val maxBackoffMillis = 30000L

            while (isActive) {
                try {
                    val token = tokenStorage.getAccessToken()
                    val baseWsUrl = ApiConfig.epicWebSocketUrl(epicId)
                    val wsUrl = if (!token.isNullOrBlank()) {
                        if (baseWsUrl.contains("?")) "$baseWsUrl&token=$token" else "$baseWsUrl?token=$token"
                    } else baseWsUrl

                    Napier.d(tag = "FAIRSPLIT_WS", message = "WebSocket CONNECTING: epicId=$epicId, url=$wsUrl, hasToken=${!token.isNullOrBlank()}")

                    httpClient.webSocket(
                        urlString = wsUrl,
                        request = {
                            timeout {
                                requestTimeoutMillis = Long.MAX_VALUE
                                socketTimeoutMillis = Long.MAX_VALUE
                            }
                            if (!token.isNullOrBlank()) {
                                headers.append("Authorization", "Bearer $token")
                            }
                        }
                    ) {
                        Napier.d(tag = "FAIRSPLIT_WS", message = "WebSocket CONNECTED: epicId=$epicId")
                        // Reset backoff upon successful connection
                        backoffMillis = 1000L

                        // Emit CONNECTED event to trigger silent refresh
                        _events.emit(RealtimeEventDto(type = RealtimeEventType.CONNECTED, epicId = epicId))

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                Napier.d(tag = "FAIRSPLIT_WS", message = "WebSocket EVENT RECEIVED: $text")
                                try {
                                    val event = json.decodeFromString<RealtimeEventDto>(text)
                                    _events.emit(event)
                                } catch (e: Exception) {
                                    Napier.w(tag = "FAIRSPLIT_WS", message = "Failed to parse event frame: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    Napier.d(tag = "FAIRSPLIT_WS", message = "WebSocket DISCONNECTED: cancelled for epic $epicId")
                    break
                } catch (e: Exception) {
                    Napier.e(tag = "FAIRSPLIT_WS", message = "WebSocket ERROR: epicId=$epicId, error=${e.message}", throwable = e)
                    Napier.d(tag = "FAIRSPLIT_WS", message = "WebSocket DISCONNECTED: epicId=$epicId, reconnecting in ${backoffMillis}ms")
                }

                if (isActive) {
                    delay(backoffMillis)
                    backoffMillis = (backoffMillis * 2).coerceAtMost(maxBackoffMillis)
                }
            }
        }
    }

    override fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
    }
}
