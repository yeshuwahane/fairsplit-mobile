package com.cybernerd404.fairsplit.di

import com.cybernerd404.fairsplit.data.local.SettingsTokenStorage
import com.cybernerd404.fairsplit.data.local.TokenStorage
import com.cybernerd404.fairsplit.data.remote.ApiConfig
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteHealthDataSource
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteHealthDataSourceImpl
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single<TokenStorage> { SettingsTokenStorage() }

    single {
        HttpClient {
            defaultRequest {
                url(ApiConfig.baseUrl)
            }
            install(ContentNegotiation) {
                json(Json {
                    explicitNulls = false
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(tag = "FAIRSPLIT_HTTP", message = message)
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
            }
            install(io.ktor.client.plugins.websocket.WebSockets) {
                pingIntervalMillis = 15_000L
            }
        }
    }

    single<RemoteHealthDataSource> { RemoteHealthDataSourceImpl(get()) }
    single<com.cybernerd404.fairsplit.data.remote.datasource.RemoteAuthDataSource> {
        com.cybernerd404.fairsplit.data.remote.datasource.RemoteAuthDataSourceImpl(get())
    }
    single<com.cybernerd404.fairsplit.data.remote.datasource.RemoteUserDataSource> {
        com.cybernerd404.fairsplit.data.remote.datasource.RemoteUserDataSourceImpl(get(), get())
    }
    single<com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSource> {
        com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSourceImpl(get(), get())
    }
    single<com.cybernerd404.fairsplit.data.remote.realtime.RealtimeDataSource> {
        com.cybernerd404.fairsplit.data.remote.realtime.RealtimeDataSourceImpl(get(), get())
    }
}
