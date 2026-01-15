package com.kevinlemein.qash.domain.model

import java.util.Date

data class Transaction(
    val mpesaCode: String,   // Matches your Entity
    val amount: Double,
    val description: String, // You preferred "description" over "recipient"
    val category: String = "Uncategorised",
    val date: Date,          // Domain uses Date
    val type: TransactionType,
    val newBalance: Double = 0.0
)

enum class TransactionType {
    SENT, RECEIVED, PAYBILL, BUY_GOODS, UNKNOWN
}

