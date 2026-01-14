package com.kevinlemein.qash.domain.usecase

import com.kevinlemein.qash.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ParseSmsUseCaseTest {

    private val parseSms = ParseSmsUseCase()

    @Test
    fun `should parse Received money with missing space after Confirmed`() {
        // The message that was failing
        val sms = "UAE6Y3P5WN Confirmed.You have received Ksh3,000.00 from BRIAN C  KABUI 0724209295 on 14/1/26 at 11:45 AM  New M-PESA balance is Ksh3,000.00."

        val result = parseSms(sms)

        assertNotNull("Should match received pattern", result)
        assertEquals("UAE6Y3P5WN", result?.mpesaCode)
        assertEquals("BRIAN C  KABUI 0724209295", result?.description)
        assertEquals(3000.00, result?.amount!!, 0.0)
        assertEquals(TransactionType.RECEIVED, result.type)
    }

    @Test
    fun `should parse Buy Goods (Paid to) correctly`() {
        // The message that was failing
        val sms = "UAEK83U2J3 Confirmed. Ksh210.00 paid to GERALD MASYUKA NDALU on 14/1/26 at 4:10 PM.New M-PESA balance is Ksh1,422.38."

        val result = parseSms(sms)

        assertNotNull("Should match sent pattern", result)
        assertEquals("UAEK83U2J3", result?.mpesaCode)
        // Notice we strip the trailing dot in the code logic now
        assertEquals("GERALD MASYUKA NDALU", result?.description)
        assertEquals(210.00, result?.amount!!, 0.0)
        assertEquals(TransactionType.SENT, result.type)
    }
}