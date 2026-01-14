package com.kevinlemein.qash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kevinlemein.qash.data.local.dao.TransactionDao
import com.kevinlemein.qash.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}