package com.example.qash;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private double amount;
    private String description;
    private String category;
    private String type;
    private long date;

    @ColumnInfo(name = "mpesa_code")
    private String mpesaCode;

    @ColumnInfo(name = "new_balance")
    private double newBalance;

    // Constructor
    public Transaction(double amount, String description, String category, String type, long date, String mpesaCode, double newBalance) {

        this.amount = amount;
        this.description = description;
        this.category = category;
        this.type = type;
        this.date = date;
        this.mpesaCode = mpesaCode;
        this.newBalance = newBalance;
    }

    // Getters and Setters
    public String getMpesaCode() {
        return mpesaCode;
    }

    public double getNewBalance() { return newBalance;}

    public void setNewBalance(double newBalance) { this.newBalance = newBalance;}

    public void setMpesaCode(String mpesaCode) {
        this.mpesaCode = mpesaCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}