package com.example.qash;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MerchantCategoryDAO {

    @Insert
    void insert(MerchantCategory merchantCategory);

    @Query("SELECT * FROM merchant_categories WHERE merchantKeyword = :keyword LIMIT 1")
    MerchantCategory findByKeyword(String keyword);

    @Query("SELECT * FROM merchant_categories WHERE :merchantName LIKE '%' || merchantKeyword || '%' LIMIT 1")
    MerchantCategory findByMerchantName(String merchantName);
}
