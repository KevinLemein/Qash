package com.kevinlemein.qash.domain.repository

import com.kevinlemein.qash.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
interface SmsRepository {

    // The UI will observe this Flow. Updates happen automatically.
    fun getAllTransactions(): Flow<List<Transaction>>

    // We will use this when the Sync Worker runs
    suspend fun saveTransaction(transaction: Transaction)

    suspend fun updateCategory(mpesaCode: String, newCategory: String)

    suspend fun syncMessages()
}