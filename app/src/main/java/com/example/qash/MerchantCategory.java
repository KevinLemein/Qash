package com.example.qash;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "merchant_categories")
public class MerchantCategory {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String merchantKeyword; // Lowercase keyword to match
    private String category;

    public MerchantCategory(String merchantKeyword, String category) {
        this.merchantKeyword = merchantKeyword.toLowerCase();
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMerchantKeyword() {
        return merchantKeyword;
    }

    public void setMerchantKeyword(String merchantKeyword) {
        this.merchantKeyword = merchantKeyword.toLowerCase();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}