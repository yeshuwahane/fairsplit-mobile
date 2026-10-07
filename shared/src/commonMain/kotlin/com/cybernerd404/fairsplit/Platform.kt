package com.cybernerd404.fairsplit

interface Platform {
    val name: String
    val isEmulator: Boolean get() = false
}

expect fun getPlatform(): Platform