package com.kevinlemein.qash.domain.usecase

import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.model.TransactionType
import com.kevinlemein.qash.domain.util.CategoryHelper
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ParseSmsUseCase @Inject constructor() {

    // REGEX UPDATES:
    // 1. [Cc]onfirmed -> Matches "Confirmed" OR "confirmed"
    // 2. \.?\s* -> Matches "dot space", "dot", or "space" (Handles "confirmed.You")

    // 1. SENT: "sent to" OR "paid to"
    private val sentPattern = """^([A-Z0-9]+)\s+[Cc]onfirmed\.?\s*Ksh([0-9,]+\.[0-9]{2})\s+(?:sent|paid)\s+to\s+(.+?)\s+on\s+([\d/]+\s+at\s+[\d:]+\s+[AP]M).*New\s+M-PESA\s+balance\s+is\s+Ksh([0-9,]+\.[0-9]{2}).*""".toRegex()

    // 2. RECEIVED: "received ... from"
    private val receivedPattern = """^([A-Z0-9]+)\s+[Cc]onfirmed\.?\s*You\s+have\s+received\s+Ksh([0-9,]+\.[0-9]{2})\s+from\s+(.+?)\s+on\s+([\d/]+\s+at\s+[\d:]+\s+[AP]M).*New\s+M-PESA\s+balance\s+is\s+Ksh([0-9,]+\.[0-9]{2}).*""".toRegex()

    // 3. PURCHASE (Airtime/Bundles): "You bought Ksh... of ..."
    private val purchasePattern = """^([A-Z0-9]+)\s+[Cc]onfirmed\.?\s*You\s+bought\s+Ksh([0-9,]+\.[0-9]{2})\s+of\s+(.+?)\s+on\s+([\d/]+\s+at\s+[\d:]+\s+[AP]M).*New\s+M-PESA\s+balance\s+is\s+Ksh([0-9,]+\.[0-9]{2}).*""".toRegex()

    private val dateFormat = SimpleDateFormat("d/M/yy 'at' h:mm a", Locale.ENGLISH)

    operator fun invoke(body: String): Transaction? {
        // Normalize: replace newlines with space, trim ends
        val cleanBody = body.replace("\n", " ").trim()

        // Check Sent
        sentPattern.find(cleanBody)?.let { match ->
            val (code, amount, recipient, date, bal) = match.destructured
            return createTransaction(code, amount, recipient.removeSuffix("."), date, bal, TransactionType.SENT)
        }

        // Check Received
        receivedPattern.find(cleanBody)?.let { match ->
            val (code, amount, recipient, date, bal) = match.destructured
            return createTransaction(code, amount, recipient, date, bal, TransactionType.RECEIVED)
        }

        // Check Purchase (Airtime)
        purchasePattern.find(cleanBody)?.let { match ->
            val (code, amount, item, date, bal) = match.destructured
            return createTransaction(code, amount, item.replaceFirstChar { it.uppercase() }, date, bal, TransactionType.SENT)
        }

        return null
    }

    private fun createTransaction(
        code: String,
        amountStr: String,
        description: String,
        dateStr: String,
        balanceStr: String,
        type: TransactionType
    ): Transaction? {
        return try {
            val amount = amountStr.replace(",", "").toDouble()
            val balance = balanceStr.replace(",", "").toDouble()
            val date = dateFormat.parse(dateStr) ?: return null

            val cleanDesc = description.trim()
            val autoCategory = CategoryHelper.categorize(cleanDesc)

            Transaction(
                mpesaCode = code,
                amount = amount,
                description = cleanDesc,
                category = autoCategory,
                date = date,
                type = type,
                newBalance = balance
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}