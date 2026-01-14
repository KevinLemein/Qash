package com.kevinlemein.qash.domain.usecase

import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ParseSmsUseCase @Inject constructor() {

    // Regex Explanation:
    // 1. We capture the core transaction details (Code, Amount, Recipient, Date)
    // 2. We look for "New M-PESA balance is Ksh..." at the end to capture the Real Balance.

    // Pattern: Code ... Amount ... sent/paid to ... Recipient ... on Date ... Balance
    private val sentPattern = """^([A-Z0-9]+)\s+Confirmed\.\s+Ksh([0-9,]+\.[0-9]{2})\s+(?:sent|paid)\s+to\s+(.+?)\s+on\s+([\d/]+\s+at\s+[\d:]+\s+[AP]M).*New\s+M-PESA\s+balance\s+is\s+Ksh([0-9,]+\.[0-9]{2}).*""".toRegex()

    // Pattern: Code ... Received ... Amount ... from ... Recipient ... on Date ... Balance
    private val receivedPattern = """^([A-Z0-9]+)\s+Confirmed\.?\s*You\s+have\s+received\s+Ksh([0-9,]+\.[0-9]{2})\s+from\s+(.+?)\s+on\s+([\d/]+\s+at\s+[\d:]+\s+[AP]M).*New\s+M-PESA\s+balance\s+is\s+Ksh([0-9,]+\.[0-9]{2}).*""".toRegex()

    private val dateFormat = SimpleDateFormat("d/M/yy 'at' h:mm a", Locale.ENGLISH)

    operator fun invoke(body: String): Transaction? {
        val cleanBody = body.replace("\n", " ").trim()

        sentPattern.find(cleanBody)?.let { match ->
            val (code, amount, recipient, date, bal) = match.destructured
            return createTransaction(code, amount, recipient.removeSuffix("."), date, bal, TransactionType.SENT)
        }

        receivedPattern.find(cleanBody)?.let { match ->
            val (code, amount, recipient, date, bal) = match.destructured
            return createTransaction(code, amount, recipient, date, bal, TransactionType.RECEIVED)
        }

        return null
    }

    private fun createTransaction(
        code: String,
        amountStr: String,
        description: String,
        dateStr: String,
        balanceStr: String, // New Argument
        type: TransactionType
    ): Transaction? {
        return try {
            val amount = amountStr.replace(",", "").toDouble()
            val balance = balanceStr.replace(",", "").toDouble() // Parse the real balance
            val date = dateFormat.parse(dateStr) ?: return null

            Transaction(
                mpesaCode = code,
                amount = amount,
                description = description.trim(),
                category = "Uncategorized",
                date = date,
                type = type,
                newBalance = balance // Save it!
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}