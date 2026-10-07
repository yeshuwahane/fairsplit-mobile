package com.cybernerd404.fairsplit.data.remote.datasource

import com.cybernerd404.fairsplit.data.remote.ApiConfig
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class HealthCheckResponse(
    val status: String,
    val timestamp: String
)

interface RemoteHealthDataSource {
    suspend fun checkHealth(): Result<HealthCheckResponse>
}

class RemoteHealthDataSourceImpl(private val client: HttpClient) : RemoteHealthDataSource {
    override suspend fun checkHealth(): Result<HealthCheckResponse> {
        return try {
            val response = client.get("/health")
            val result = response.body<HealthCheckResponse>()
            Napier.i(tag = "FAIRSPLIT_HEALTH", message = "Backend connection verified: $result (Host: ${ApiConfig.baseUrl})")
            Result.success(result)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_HEALTH", message = "Backend connection failed at ${ApiConfig.baseUrl}: ${e.message}", throwable = e)
            Result.failure(e)
        }
    }
}
