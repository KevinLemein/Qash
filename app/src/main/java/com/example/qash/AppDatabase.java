package com.example.qash;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Transaction.class, MerchantCategory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract TransactionDAO transactionDao();
    public abstract MerchantCategoryDAO merchantCategoryDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "qash_database"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback) // Attach the callback here
                            .build();
                }
            }
        }
        return instance;
    }

    // Callback to populate the database when it is created
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // We use the executor to run this in the background
            databaseWriteExecutor.execute(() -> {
                populateCommonMerchants(db);
            });
        }
    };

    // Helper method to insert data using raw SQL (Safer during onCreate)
    private static void populateCommonMerchants(SupportSQLiteDatabase db) {
        db.beginTransaction();
        try {
            // --- BILLS & UTILITIES ---
            insertMerchant(db, "KENYA POWER", "Bills & Utilities");
            insertMerchant(db, "KPLC", "Bills & Utilities");
            insertMerchant(db, "ZUKU", "Bills & Utilities");
            insertMerchant(db, "DSTV", "Bills & Utilities");
            insertMerchant(db, "GOTV", "Bills & Utilities");
            insertMerchant(db, "STARLINK", "Bills & Utilities");
            insertMerchant(db, "WIFI", "Bills & Utilities");

            // --- AIRTIME & DATA ---
            insertMerchant(db, "SAFARICOM DATA", "Airtime & Data");
            insertMerchant(db, "AIRTEL", "Airtime & Data");
            insertMerchant(db, "TELKOM", "Airtime & Data");
            insertMerchant(db, "FAIBA", "Airtime & Data");

            // --- FOOD & GROCERIES ---
            insertMerchant(db, "NAIVAS", "Food & Groceries");
            insertMerchant(db, "CARREFOUR", "Food & Groceries");
            insertMerchant(db, "QUICKMART", "Food & Groceries");
            insertMerchant(db, "CHANDARANA", "Food & Groceries");
            insertMerchant(db, "CLEANSHELF", "Food & Groceries");
            insertMerchant(db, "MATTRESS", "Food & Groceries");

            // --- DINING & TAKEOUT ---
            insertMerchant(db, "JAVA HOUSE", "Dining");
            insertMerchant(db, "ARTCAFFE", "Dining");
            insertMerchant(db, "KFC", "Dining");
            insertMerchant(db, "PIZZA INN", "Dining");
            insertMerchant(db, "GALITOS", "Dining");
            insertMerchant(db, "UBER EATS", "Dining");
            insertMerchant(db, "GLOVO", "Dining");

            // --- TRANSPORT & FUEL ---
            insertMerchant(db, "UBER", "Transport");
            insertMerchant(db, "BOLT", "Transport");
            insertMerchant(db, "LITTLE", "Transport");
            insertMerchant(db, "SHELL", "Transport");
            insertMerchant(db, "TOTAL", "Transport");
            insertMerchant(db, "RUBIS", "Transport");
            insertMerchant(db, "OLA", "Transport");


            // --- ENTERTAINMENT ---
            insertMerchant(db, "SHOWMAX", "Entertainment");
            insertMerchant(db, "NETFLIX", "Entertainment");
            insertMerchant(db, "SPOTIFY", "Entertainment");

            // --- GOVERNMENT ---
            insertMerchant(db, "ECITIZEN", "Government");
            insertMerchant(db, "NTSA", "Government");
            insertMerchant(db, "NHIF", "Insurance");
            insertMerchant(db, "NSSF", "Insurance");


            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void insertMerchant(SupportSQLiteDatabase db, String keyword, String category) {
        db.execSQL("INSERT INTO merchant_categories (merchantKeyword, category) VALUES ('"
                + keyword.toLowerCase() + "', '" + category + "')");
    }
}