package com.kevinlemein.qash.data.repository

import com.kevinlemein.qash.data.local.dao.TransactionDao
import com.kevinlemein.qash.data.mapper.toDomain
import com.kevinlemein.qash.data.mapper.toEntity
import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
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
}