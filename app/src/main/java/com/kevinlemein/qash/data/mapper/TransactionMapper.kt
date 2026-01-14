package com.kevinlemein.qash.data.mapper

import com.kevinlemein.qash.data.local.entity.TransactionEntity
import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.model.TransactionType
import java.util.Date

// 1. Convert DOMAIN (Business Logic) -> ENTITY (Database)
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        mpesaCode = this.mpesaCode,
        amount = this.amount,
        description = this.description,
        category = this.category,
        type = this.type.name, // ENUM -> STRING (e.g., "SENT")
        date = this.date.time, // DATE -> LONG (Unix Timestamp)
        newBalance = this.newBalance
    )
}

// 2. Convert ENTITY (Database) -> DOMAIN (Business Logic)
fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        mpesaCode = this.mpesaCode,
        amount = this.amount,
        description = this.description,
        category = this.category,
        type = try {
            TransactionType.valueOf(this.type) // STRING -> ENUM
        } catch (e: Exception) {
            TransactionType.UNKNOWN // Safety fallback
        },
        date = Date(this.date), // LONG -> DATE
        newBalance = this.newBalance
    )
}