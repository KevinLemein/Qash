package com.kevinlemein.qash.domain.usecase

import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ParseSmsUseCase @Inject constructor() {

    // Regex for "Sent" (Send Money / Paybill / Buy Goods)
    private val sentPattern = """([A-Z0-9]+)\s+Confirmed\.\s+Ksh([0-9,]+\.[0-9]{2})\s+sent\s+to\s+(.+?)\s+on\s+(.+)""".toRegex()

    // Regex for "Received"
    private val receivedPattern = """([A-Z0-9]+)\s+Confirmed\.\s+You\s+have\s+received\s+Ksh([0-9,]+\.[0-9]{2})\s+from\s+(.+?)\s+on\s+(.+)""".toRegex()

    // M-Pesa Date Format: "14/1/26 at 1:08 PM"
    private val dateFormat = SimpleDateFormat("d/M/yy 'at' h:mm a", Locale.ENGLISH)

    operator fun invoke(body: String): Transaction? {
        // 1. Try parsing as a "Sent" transaction
        sentPattern.find(body)?.let { match ->
            val (code, amountStr, recipient, dateStr) = match.destructured
            // FIX: Added "Uncategorized" to match the function signature
            return createTransaction(code, amountStr, recipient, "Uncategorized", dateStr, TransactionType.SENT)
        }

        // 2. Try parsing as a "Received" transaction
        receivedPattern.find(body)?.let { match ->
            val (code, amountStr, recipient, dateStr) = match.destructured
            // FIX: Added "Uncategorized" here too
            return createTransaction(code, amountStr, recipient, "Uncategorized", dateStr, TransactionType.RECEIVED)
        }

        // If no patterns match (e.g., "Failed transaction"), return null
        return null
    } // <--- Added closing brace for invoke()

    // Moved OUTSIDE invoke()
    private fun createTransaction(
        code: String,
        amountStr: String,
        description: String,
        category: String, // This was missing in the calls above
        dateStr: String,
        type: TransactionType
    ): Transaction? {
        return try {
            val cleanAmount = amountStr.replace(",", "").toDouble()
            // Clean date string (remove trailing dots/spaces)
            val cleanDateStr = dateStr.trim().removeSuffix(".")
            val date = dateFormat.parse(cleanDateStr) ?: return null

            Transaction(
                mpesaCode = code,
                amount = cleanAmount,
                description = description.trim(),
                category = category,
                date = date,
                type = type,
                newBalance = 0.0
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}