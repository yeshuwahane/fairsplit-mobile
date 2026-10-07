package com.cybernerd404.fairsplit.domain

import com.cybernerd404.fairsplit.data.remote.dto.EpicDto
import com.cybernerd404.fairsplit.data.remote.dto.MemberDto
import com.cybernerd404.fairsplit.data.remote.dto.SettlementDto
import com.cybernerd404.fairsplit.domain.model.SettlementStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettlementDomainTest {

    private val aliceId = "user_alice_123"
    private val bobId = "user_bob_456"

    private val aliceMemberDto = MemberDto(
        id = aliceId,
        name = "Alice",
        avatarInitials = "AL",
        totalSpent = 0.0,
        balance = -980.0
    )

    private val bobMemberDto = MemberDto(
        id = bobId,
        name = "Bob",
        avatarInitials = "BO",
        totalSpent = 980.0,
        balance = 980.0
    )

    @Test
    fun balanceShowsDebt_pendingSettlementAlsoShowsDebt() {
        val settlementDto = SettlementDto(
            id = "epic_1_alice_bob",
            fromMember = aliceMemberDto,
            toMember = bobMemberDto,
            amount = 980.0,
            status = "PENDING"
        )

        val epicDto = EpicDto(
            id = "epic_1",
            name = "Goa Trip",
            members = listOf(aliceMemberDto, bobMemberDto),
            yourSpent = 0.0,
            yourBalance = -980.0,
            settlements = listOf(settlementDto)
        )

        val aliceEpic = epicDto.toDomain(currentUserId = aliceId, currentUserName = "Alice")

        // Alice owes ₹980
        assertEquals(-980.0, aliceEpic.yourBalance)
        assertEquals(1, aliceEpic.settlements.size)

        val pendingDebt = aliceEpic.settlements.first()
        assertEquals(980.0, pendingDebt.amount)
        assertEquals(SettlementStatus.PENDING, pendingDebt.status)
        assertTrue(pendingDebt.fromMember.isCurrentUser, "From member should be current user (Alice)")
        assertFalse(pendingDebt.toMember.isCurrentUser, "To member should be Bob")
    }

    @Test
    fun partialSettlement_updatesPendingAmount() {
        // After Alice pays ₹500, outstanding debt is ₹480
        val settlementDto = SettlementDto(
            id = "epic_1_alice_bob",
            fromMember = aliceMemberDto.copy(balance = -480.0),
            toMember = bobMemberDto.copy(balance = 480.0),
            amount = 480.0,
            status = "PENDING"
        )

        val epicDto = EpicDto(
            id = "epic_1",
            name = "Goa Trip",
            members = listOf(aliceMemberDto.copy(balance = -480.0), bobMemberDto.copy(balance = 480.0)),
            yourSpent = 500.0,
            yourBalance = -480.0,
            settlements = listOf(settlementDto)
        )

        val aliceEpic = epicDto.toDomain(currentUserId = aliceId, currentUserName = "Alice")

        assertEquals(-480.0, aliceEpic.yourBalance)
        assertEquals(1, aliceEpic.settlements.size)
        assertEquals(480.0, aliceEpic.settlements.first().amount)
        assertEquals(SettlementStatus.PENDING, aliceEpic.settlements.first().status)
    }

    @Test
    fun fullSettlement_showsAllSettled() {
        // All debts cleared -> settlements list from backend is empty
        val epicDto = EpicDto(
            id = "epic_1",
            name = "Goa Trip",
            members = listOf(aliceMemberDto.copy(balance = 0.0), bobMemberDto.copy(balance = 0.0)),
            yourSpent = 980.0,
            yourBalance = 0.0,
            settlements = emptyList()
        )

        val aliceEpic = epicDto.toDomain(currentUserId = aliceId, currentUserName = "Alice")

        assertEquals(0.0, aliceEpic.yourBalance)
        assertTrue(aliceEpic.settlements.isEmpty(), "Settlements list must be empty when all debts are settled")
    }

    @Test
    fun debtorSeesYouOwe() {
        val settlementDto = SettlementDto(
            id = "epic_1_alice_bob",
            fromMember = aliceMemberDto,
            toMember = bobMemberDto,
            amount = 980.0,
            status = "PENDING"
        )

        val domainSettlement = settlementDto.toDomain(currentUserId = aliceId, currentUserName = "Alice")

        // Alice is the debtor (fromMember)
        assertTrue(domainSettlement.fromMember.isCurrentUser)
        assertFalse(domainSettlement.toMember.isCurrentUser)
        assertEquals("Alice", domainSettlement.fromMember.name)
        assertEquals("Bob", domainSettlement.toMember.name)
    }

    @Test
    fun creditorSeesTheyAreOwed() {
        val settlementDto = SettlementDto(
            id = "epic_1_alice_bob",
            fromMember = aliceMemberDto,
            toMember = bobMemberDto,
            amount = 980.0,
            status = "PENDING"
        )

        val domainSettlement = settlementDto.toDomain(currentUserId = bobId, currentUserName = "Bob")

        // Bob is the creditor (toMember)
        assertFalse(domainSettlement.fromMember.isCurrentUser)
        assertTrue(domainSettlement.toMember.isCurrentUser)
        assertEquals("Alice", domainSettlement.fromMember.name)
        assertEquals("Bob", domainSettlement.toMember.name)
    }
}
