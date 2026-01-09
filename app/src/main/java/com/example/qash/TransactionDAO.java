package com.example.qash;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao

public interface TransactionDAO {

    // Insert a transaction
    @Insert
    void insert(Transaction transaction);

    // Update a transaction
    @Update
    void update(Transaction transaction);

    // Delete a transaction
    @Delete
    void delete(Transaction transaction);

    // Get all transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<Transaction> getAllTransactions();

    // Get all transactions by type (Expense or Income)
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    List<Transaction> getTransactionsByType(String type);

    // Get transactions by date range
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Transaction> getTransactionsByDateRange(long startDate, long endDate);

    // Get total income
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Income'")
    double getTotalIncome();

    // Get total expenses
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense'")
    double getTotalExpenses();

    // Delete all transactions
    @Query("DELETE FROM transactions")
    void deleteAll();

    // ADD THIS - Check if M-Pesa code already exists
    @Query("SELECT COUNT(*) FROM transactions WHERE mpesa_code = :mpesaCode")
    int countByMpesaCode(String mpesaCode);

    // Or return the transaction directly
    @Query("SELECT * FROM transactions WHERE mpesa_code = :mpesaCode LIMIT 1")
    Transaction findByMpesaCode(String mpesaCode);

    @Query("SELECT new_balance FROM transactions ORDER BY date DESC LIMIT 1")
    Double getLatestMpesaBalance();
}
