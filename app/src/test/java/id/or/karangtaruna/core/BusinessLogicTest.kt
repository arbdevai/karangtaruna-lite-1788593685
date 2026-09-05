package id.or.karangtaruna.core

import id.or.karangtaruna.core.auth.RoleCapabilities
import id.or.karangtaruna.core.auth.Permission
import id.or.karangtaruna.core.auth.Validation
import id.or.karangtaruna.core.model.*
import id.or.karangtaruna.core.util.Formatters
import org.junit.Assert.*
import org.junit.Test

class BusinessLogicTest {
    @Test fun testRupiahFormatting() {
        val result = Formatters.rupiah(4850000)
        assertTrue(result.contains("4.850.000"))
        assertTrue(result.startsWith("Rp"))
    }

    @Test fun testAmountValidation() {
        assertNotNull(Validation.amount(0))
        assertNotNull(Validation.amount(-1000))
        assertNull(Validation.amount(50000))
    }

    @Test fun testMemberNameValidation() {
        assertNotNull(Validation.memberName(""))
        assertNotNull(Validation.memberName("A"))
        assertNull(Validation.memberName("Ahmad Fauzi"))
    }

    @Test fun testRolePermissions() {
        assertTrue(RoleCapabilities.has(Role.TREASURER, Permission.MANAGE_TRANSACTIONS))
        assertTrue(RoleCapabilities.has(Role.ADMIN, Permission.MANAGE_SETTINGS))
        assertFalse(RoleCapabilities.has(Role.VIEWER, Permission.MANAGE_TRANSACTIONS))
        assertFalse(RoleCapabilities.has(Role.TREASURER, Permission.MANAGE_SETTINGS))
    }

    @Test fun testFinancialSummaryBalance() {
        val summary = FinanceSummary(balance = 100000, income = 150000, expense = 50000)
        assertEquals(summary.balance, summary.income - summary.expense)
    }

    @Test fun testRelativeTime() {
        val now = 1757062800000L
        val text = Formatters.relativeTime(now, now)
        assertTrue(text.startsWith("Hari ini"))
    }
}
