package com.cybernerd404.fairsplit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseCategory(val displayName: String, val colorHex: Long) {
    FOOD("Food", 0xFFF97316),
    STAY("Stay", 0xFF6366F1),
    TRAVEL("Travel", 0xFF06B6D4),
    ACTIVITIES("Activities", 0xFF10B981),
    SHOPPING("Shopping", 0xFFEC4899),
    ENTERTAINMENT("Entertainment", 0xFF8B5CF6),
    OTHER("Other", 0xFFEAB308);

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

@Serializable
data class CategoryBreakdown(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Float
)
