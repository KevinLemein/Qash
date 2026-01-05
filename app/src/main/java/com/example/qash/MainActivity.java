package com.example.qash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance, tvTodaySpending, tvWeekSpending, tvNoTransactions;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        tvBalance = findViewById(R.id.tvBalance);
        tvTodaySpending = findViewById(R.id.tvTodaySpending);
        tvWeekSpending = findViewById(R.id.tvWeekSpending);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        rvTransactions = findViewById(R.id.rvTransactions);

        // Setup RecyclerView
        adapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        // Set long-click listener for delete
        adapter.setOnTransactionLongClickListener(transaction -> {
            showDeleteConfirmationDialog(transaction);
        });

        FloatingActionButton fabAddTransaction = findViewById(R.id.fabAddTransaction);

        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransaction.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load transactions every time we come back to this screen
        loadTransactions();
    }

    private void loadTransactions() {
        executorService.execute(() -> {
            // Get all transactions from database
            List<Transaction> transactions = database.transactionDao().getAllTransactions();

            // Calculate totals
            double totalIncome = database.transactionDao().getTotalIncome();
            double totalExpenses = database.transactionDao().getTotalExpenses();
            double balance = totalIncome - totalExpenses;

            // Calculate today's spending
            long todayStart = getTodayStartTimestamp();
            long todayEnd = System.currentTimeMillis();
            List<Transaction> todayTransactions = database.transactionDao()
                    .getTransactionsByDateRange(todayStart, todayEnd);

            double todaySpending = 0;
            for (Transaction t : todayTransactions) {
                if (t.getType().equals("Expense")) {
                    todaySpending += t.getAmount();
                }
            }
            final double finalTodaySpending = todaySpending;

            // Calculate this week's spending
            long weekStart = getWeekStartTimestamp();
            List<Transaction> weekTransactions = database.transactionDao()
                    .getTransactionsByDateRange(weekStart, todayEnd);

            double weekSpending = 0;
            for (Transaction t : weekTransactions) {
                if (t.getType().equals("Expense")) {
                    weekSpending += t.getAmount();
                }
            }
            final double finalWeekSpending = weekSpending;

            // Update UI on main thread
            runOnUiThread(() -> {
                // Update balance
                tvBalance.setText(String.format(Locale.getDefault(), "KES %.2f", balance));

                // Update today's spending
                tvTodaySpending.setText(String.format(Locale.getDefault(), "KES %.2f", finalTodaySpending));

                // Update week's spending
                tvWeekSpending.setText(String.format(Locale.getDefault(), "KES %.2f", finalWeekSpending));

                // Update transactions list
                if (transactions.isEmpty()) {
                    rvTransactions.setVisibility(View.GONE);
                    tvNoTransactions.setVisibility(View.VISIBLE);
                } else {
                    rvTransactions.setVisibility(View.VISIBLE);
                    tvNoTransactions.setVisibility(View.GONE);
                    adapter.setTransactions(transactions);
                }
            });
        });
    }

    // Helper method to get today's start timestamp (midnight)
    private long getTodayStartTimestamp() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // Helper method to get this week's start timestamp (Monday)
    private long getWeekStartTimestamp() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();

        // Set to Monday of current week
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private void showDeleteConfirmationDialog(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage(String.format("Delete %s - KES %.2f?",
                        transaction.getDescription(),
                        transaction.getAmount()))
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTransaction(transaction);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> {
            database.transactionDao().delete(transaction);

            runOnUiThread(() -> {
                // Reload transactions to update the list
                loadTransactions();

                // Show confirmation
                android.widget.Toast.makeText(this, "Transaction deleted", android.widget.Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}