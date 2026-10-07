package com.cybernerd404.fairsplit.data.remote.datasource

import com.cybernerd404.fairsplit.data.remote.dto.ApiResponse
import com.cybernerd404.fairsplit.data.remote.dto.BackendAuthResponseDto
import com.cybernerd404.fairsplit.data.remote.dto.GoogleAuthRequestDto
import com.cybernerd404.fairsplit.data.remote.dto.LogoutRequestDto
import com.cybernerd404.fairsplit.data.remote.dto.PhoneOtpRequestDto
import com.cybernerd404.fairsplit.data.remote.dto.PhoneOtpVerifyDto
import com.cybernerd404.fairsplit.data.remote.dto.RequestOtpResponseDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface RemoteAuthDataSource {
    suspend fun requestPhoneOtp(phone: String): Result<RequestOtpResponseDto>
    suspend fun verifyPhoneOtp(phone: String, otp: String): Result<BackendAuthResponseDto>
    suspend fun signInWithGoogle(idToken: String, email: String, displayName: String?, photoUrl: String?): Result<BackendAuthResponseDto>
    suspend fun logout(refreshToken: String?): Result<Boolean>
}

class RemoteAuthDataSourceImpl(private val client: HttpClient) : RemoteAuthDataSource {

    override suspend fun signInWithGoogle(
        idToken: String,
        email: String,
        displayName: String?,
        photoUrl: String?
    ): Result<BackendAuthResponseDto> {
        return try {
            Napier.d(tag = "FAIRSPLIT_AUTH", message = "Calling backend POST /api/v1/auth/google for $email")
            val response = client.post("/api/v1/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(GoogleAuthRequestDto(idToken = idToken, email = email, displayName = displayName, photoUrl = photoUrl))
            }
            val body = response.body<ApiResponse<BackendAuthResponseDto>>()
            if (body.success && body.data != null) {
                Napier.i(tag = "FAIRSPLIT_AUTH", message = "Backend Google sign-in successful for user ${body.data.user.id}")
                Result.success(body.data)
            } else {
                val errorMsg = body.error?.message ?: "Google sign-in failed"
                Napier.w(tag = "FAIRSPLIT_AUTH", message = "Backend error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Network error calling backend google sign-in: ${e.message}", throwable = e)
            Result.failure(e)
        }
    }

    override suspend fun requestPhoneOtp(phone: String): Result<RequestOtpResponseDto> {
        return try {
            Napier.d(tag = "FAIRSPLIT_AUTH", message = "Calling backend POST /api/v1/auth/phone/request-otp for $phone")
            val response = client.post("/api/v1/auth/phone/request-otp") {
                contentType(ContentType.Application.Json)
                setBody(PhoneOtpRequestDto(phone = phone))
            }
            val body = response.body<ApiResponse<RequestOtpResponseDto>>()
            if (body.success && body.data != null) {
                Napier.i(tag = "FAIRSPLIT_AUTH", message = "Backend OTP response: ${body.data.message}, devOtp: ${body.data.devOtp}")
                Result.success(body.data)
            } else {
                val errorMsg = body.error?.message ?: "Failed to request OTP from backend"
                Napier.w(tag = "FAIRSPLIT_AUTH", message = "Backend error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Network error calling backend request-otp: ${e.message}", throwable = e)
            Result.failure(e)
        }
    }

    override suspend fun verifyPhoneOtp(phone: String, otp: String): Result<BackendAuthResponseDto> {
        return try {
            Napier.d(tag = "FAIRSPLIT_AUTH", message = "Calling backend POST /api/v1/auth/phone/verify-otp for $phone")
            val response = client.post("/api/v1/auth/phone/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(PhoneOtpVerifyDto(phone = phone, otp = otp))
            }
            val body = response.body<ApiResponse<BackendAuthResponseDto>>()
            if (body.success && body.data != null) {
                Napier.i(tag = "FAIRSPLIT_AUTH", message = "Backend OTP verification successful for user ${body.data.user.id}")
                Result.success(body.data)
            } else {
                val errorMsg = body.error?.message ?: "Invalid verification code"
                Napier.w(tag = "FAIRSPLIT_AUTH", message = "Backend error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Napier.e(tag = "FAIRSPLIT_AUTH", message = "Network error calling backend verify-otp: ${e.message}", throwable = e)
            Result.failure(e)
        }
    }

    override suspend fun logout(refreshToken: String?): Result<Boolean> {
        return try {
            Napier.d(tag = "FAIRSPLIT_AUTH", message = "Calling backend POST /api/v1/auth/logout")
            client.post("/api/v1/auth/logout") {
                contentType(ContentType.Application.Json)
                setBody(LogoutRequestDto(refreshToken = refreshToken))
            }
            Result.success(true)
        } catch (e: Exception) {
            Napier.w(tag = "FAIRSPLIT_AUTH", message = "Backend logout error: ${e.message}")
            Result.success(true)
        }
    }
}
