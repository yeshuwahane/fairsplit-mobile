package com.cybernerd404.fairsplit.domain.auth

/**
 * Replaceable OTP provider abstraction for development and production SMS backends.
 */
interface OtpProvider {
    val isDevelopmentMode: Boolean
    suspend fun generateOrSendOtp(phone: String): String
    suspend fun verifyOtp(phone: String, inputOtp: String): Boolean
}

/**
 * Development OTP provider utilizing static development OTP 123456.
 * Controlled by [devOtpEnabled]. In production, this can be switched to a real SMS gateway provider.
 */
class DevelopmentOtpProvider(
    private val devOtpEnabled: Boolean = true,
    private val defaultOtp: String = "123456"
) : OtpProvider {

    override val isDevelopmentMode: Boolean
        get() = devOtpEnabled

    override suspend fun generateOrSendOtp(phone: String): String {
        return if (devOtpEnabled) defaultOtp else ""
    }

    override suspend fun verifyOtp(phone: String, inputOtp: String): Boolean {
        return if (devOtpEnabled) {
            inputOtp.trim() == defaultOtp
        } else {
            false
        }
    }
}
