package com.cybernerd404.fairsplit.domain.model

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Utility for accurate monetary arithmetic using integer minor units (paise: 1 INR = 100 paise).
 * Avoids IEEE-754 floating point rounding issues during splits and balances.
 */
object Money {

    fun toPaise(amountInRupees: Double): Long {
        return (amountInRupees * 100.0).roundToLong()
    }

    fun toRupees(paise: Long): Double {
        return paise / 100.0
    }

    /**
     * Splits [totalPaise] equally across [participantCount] shares, distributing remainder paise
     * one-by-one so that sum(shares) == totalPaise exactly.
     */
    fun splitEqually(totalPaise: Long, participantCount: Int): List<Long> {
        if (participantCount <= 0) return emptyList()
        val baseShare = totalPaise / participantCount
        val remainder = (totalPaise % participantCount).toInt()

        return List(participantCount) { index ->
            if (index < remainder) baseShare + 1 else baseShare
        }
    }

    fun formatInr(amountInRupees: Double, showDecimalsIfAny: Boolean = false): String {
        val paise = toPaise(amountInRupees)
        val rupees = paise / 100
        val remainingPaise = paise % 100

        return if (showDecimalsIfAny && remainingPaise != 0L) {
            "₹$rupees.${abs(remainingPaise).toString().padStart(2, '0')}"
        } else {
            "₹$rupees"
        }
    }
}
