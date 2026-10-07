package com.cybernerd404.fairsplit.data.remote.datasource

import com.cybernerd404.fairsplit.data.local.TokenStorage
import com.cybernerd404.fairsplit.data.remote.dto.ApiResponse
import com.cybernerd404.fairsplit.data.remote.dto.BackendUserDto
import com.cybernerd404.fairsplit.data.remote.dto.UpdateProfileRequestDto
import com.cybernerd404.fairsplit.data.remote.dto.UserPreferencesDto
import com.cybernerd404.fairsplit.data.remote.dto.UserProfileDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class RemoteUserDataSourceImpl(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage
) : RemoteUserDataSource {

    override suspend fun getUserProfile(): UserProfileDto {
        val token = tokenStorage.getAccessToken()
        Napier.d(tag = "FAIRSPLIT_HTTP", message = "Calling backend GET /api/v1/users/me")
        val response = client.get("/api/v1/users/me") {
            contentType(ContentType.Application.Json)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        val body = response.body<ApiResponse<BackendUserDto>>()
        if (body.success && body.data != null) {
            val data = body.data
            return UserProfileDto(
                id = data.id,
                name = data.name,
                email = data.email.orEmpty(),
                phone = data.phoneNumber.orEmpty(),
                upiId = data.upiId.orEmpty(),
                avatarInitials = if (data.name.isNotBlank()) data.name.take(2).uppercase() else "ME",
                avatarUrl = data.avatarUrl,
                profileCompleted = data.profileCompleted
            )
        } else {
            val errorMsg = body.error?.message ?: "Failed to fetch user profile"
            Napier.w(tag = "FAIRSPLIT_HTTP", message = "Backend error: $errorMsg")
            throw Exception(errorMsg)
        }
    }

    override suspend fun updateProfile(profile: UserProfileDto): UserProfileDto {
        val token = tokenStorage.getAccessToken()
        Napier.d(tag = "FAIRSPLIT_HTTP", message = "Calling backend PUT /api/v1/users/me to update profile for ${profile.name}")
        val response = client.put("/api/v1/users/me") {
            contentType(ContentType.Application.Json)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(
                UpdateProfileRequestDto(
                    name = profile.name,
                    email = profile.email.ifBlank { null },
                    phone = profile.phone.ifBlank { null },
                    upiId = profile.upiId.ifBlank { null },
                    avatarUrl = profile.avatarUrl,
                    profileCompleted = true
                )
            )
        }
        val body = response.body<ApiResponse<BackendUserDto>>()
        if (body.success && body.data != null) {
            val data = body.data
            Napier.i(tag = "FAIRSPLIT_HTTP", message = "Backend profile updated successfully for user ${data.id}: ${data.name}")
            return UserProfileDto(
                id = data.id,
                name = data.name,
                email = data.email.orEmpty(),
                phone = data.phoneNumber ?: profile.phone,
                upiId = data.upiId.orEmpty(),
                avatarInitials = if (data.name.isNotBlank()) data.name.take(2).uppercase() else profile.avatarInitials,
                avatarUrl = data.avatarUrl,
                profileCompleted = data.profileCompleted,
                youOwe = profile.youOwe,
                othersOweYou = profile.othersOweYou,
                netBalance = profile.netBalance
            )
        } else {
            val errorMsg = body.error?.message ?: "Failed to update profile"
            Napier.w(tag = "FAIRSPLIT_HTTP", message = "Backend profile update error: $errorMsg")
            throw Exception(errorMsg)
        }
    }

    override suspend fun getUserPreferences(): UserPreferencesDto {
        return UserPreferencesDto()
    }

    override suspend fun updatePreferences(preferences: UserPreferencesDto): UserPreferencesDto {
        return preferences
    }
}
