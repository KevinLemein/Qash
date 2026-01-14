package com.kevinlemein.qash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = false)
    //val id: Int = 0,
    val mpesaCode: String,

    val amount: Double,

    val description: String,

    val category: String,

    val type: String,

    val date: Long,

    val newBalance: Double = 0.0

//    @ColumnInfo(name = "mpesa_code")
//    val mpesaCode: String? = null,

)
