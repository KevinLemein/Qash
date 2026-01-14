package com.kevinlemein.qash.domain.usecase

import com.kevinlemein.qash.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ParseSmsUseCaseTest {

    private val parseSms = ParseSmsUseCase()

    @Test
    fun `should parse standard Sent Money SMS correctly`() {
        // 1. GIVEN - A sample SMS string
        val sms = "RCK898989 Confirmed. Ksh500.00 sent to KPLC PREPAID on 14/1/26 at 1:08 PM. New M-PESA balance is Ksh1,200.00."

        // 2. WHEN - We run the logic
        val result = parseSms(sms)

        // 3. THEN - We assert the fields match YOUR custom names
        assertNotNull("Result should not be null", result)

        // Matches 'mpesaCode'
        assertEquals("RCK898989", result?.mpesaCode)

        // Matches 'description' (not 'recipient')
        assertEquals("KPLC PREPAID", result?.description)

        assertEquals(500.00, result?.amount!!, 0.0)
        assertEquals("Uncategorized", result.category) // Default value we set
        assertEquals(TransactionType.SENT, result.type)
    }

    @Test
    fun `should parse standard Received Money SMS correctly`() {
        val sms = "RCK112233 Confirmed. You have received Ksh1,500.00 from JOHN DOE on 14/1/26 at 2:30 PM. New M-PESA balance is Ksh3,200.00."

        val result = parseSms(sms)

        assertNotNull(result)
        assertEquals("RCK112233", result?.mpesaCode)
        assertEquals("JOHN DOE", result?.description)
        assertEquals(1500.00, result?.amount!!, 0.0)
        assertEquals(TransactionType.RECEIVED, result.type)
    }

    @Test
    fun `should return null for non-financial messages`() {
        val sms = "Your bundle balance is below 5MB."
        val result = parseSms(sms)

        assertEquals(null, result)
    }
}