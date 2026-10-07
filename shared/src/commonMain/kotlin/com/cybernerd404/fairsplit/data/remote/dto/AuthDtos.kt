package com.cybernerd404.fairsplit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorDto? = null,
    val timestamp: Long = 0L
)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
    val details: String? = null,
    val correlationId: String? = null
)

@Serializable
data class PhoneOtpRequestDto(
    val phoneNumber: String? = null,
    val phone: String? = null
) {
    constructor(phone: String) : this(phoneNumber = phone, phone = phone)
}

@Serializable
data class RequestOtpResponseDto(
    val message: String,
    val devOtp: String? = null
)

@Serializable
data class PhoneOtpVerifyDto(
    val phoneNumber: String? = null,
    val phone: String? = null,
    val otp: String
) {
    constructor(phone: String, otp: String) : this(phoneNumber = phone, phone = phone, otp = otp)
}

@Serializable
data class BackendUserDto(
    val id: String,
    val name: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val upiId: String? = null,
    val avatarUrl: String? = null,
    val profileCompleted: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class BackendAuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: BackendUserDto
)

@Serializable
data class LogoutRequestDto(
    val refreshToken: String? = null
)

@Serializable
data class GoogleAuthRequestDto(
    val idToken: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null
)

@Serializable
data class LinkGoogleRequestDto(
    val idToken: String,
    val email: String? = null,
    val displayName: String? = null
)

@Serializable
data class LinkPhoneRequestDto(
    val phone: String
)

@Serializable
data class VerifyLinkPhoneRequestDto(
    val phone: String,
    val otp: String
)

@Serializable
data class UpdateProfileRequestDto(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val upiId: String? = null,
    val avatarUrl: String? = null,
    val profileCompleted: Boolean = true
)

@Serializable
data class AuthIdentityDto(
    val id: String,
    val userId: String,
    val provider: String,
    val providerUserId: String,
    val email: String? = null,
    val phone: String? = null,
    val createdAt: Long = 0L
)

@Serializable
data class AuthResponseDto(
    val user: UserProfileDto,
    val token: String,
    val isNewUser: Boolean = false,
    val profileCompleted: Boolean = false
)
