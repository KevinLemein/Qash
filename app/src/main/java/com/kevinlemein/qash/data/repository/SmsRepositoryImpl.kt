package com.kevinlemein.qash.data.repository

import com.kevinlemein.qash.data.local.dao.TransactionDao
import com.kevinlemein.qash.data.mapper.toDomain
import com.kevinlemein.qash.data.mapper.toEntity
import com.kevinlemein.qash.data.source.SystemSmsSource
import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.repository.SmsRepository
import com.kevinlemein.qash.domain.usecase.ParseSmsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val systemSmsSource: SystemSmsSource,
    private val parseSmsUseCase: ParseSmsUseCase
) : SmsRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        // 1. Get Entities from Room (Flow<List<TransactionEntity>>)
        // 2. Map them to Domain Models (Flow<List<Transaction>>)
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTransaction(transaction: Transaction) {
        // Convert Domain -> Entity before saving
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateCategory(mpesaCode: String, newCategory: String) {
        dao.updateCategory(mpesaCode, newCategory)
    }

    // THE SYNC ENGINE
    override suspend fun syncMessages() {
        // 1. Get raw messages from "MPESA"
        val rawMessages = systemSmsSource.getMessagesFromSender("MPESA")

        // 2. Parse them using your Domain Logic
        val transactions = rawMessages.mapNotNull { body ->
            parseSmsUseCase(body)
        }


        // 3. Save valid ones to Database
        // (Remember: Your DAO ignores duplicates automatically!)
        transactions.forEach { transaction ->
            dao.insertTransaction(transaction.toEntity())
        }
    }
}