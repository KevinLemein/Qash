package com.example.qash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance, tvTodaySpending, tvWeekSpending, tvNoTransactions;
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

            // Update UI on main thread
            runOnUiThread(() -> {
                // Update balance
                tvBalance.setText(String.format(Locale.getDefault(), "KES %.2f", balance));

                // Show transactions or "No transactions" message
                if (transactions.isEmpty()) {
                    tvNoTransactions.setText("No transactions yet");
                } else {
                    // Build a simple list of transactions
                    StringBuilder transactionList = new StringBuilder();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                    for (Transaction transaction : transactions) {
                        String date = dateFormat.format(new Date(transaction.getDate()));
                        String sign = transaction.getType().equals("Expense") ? "-" : "+";

                        transactionList.append(String.format(Locale.getDefault(),
                                "%s KES %.2f - %s\n%s • %s\n\n",
                                sign,
                                transaction.getAmount(),
                                transaction.getDescription(),
                                transaction.getCategory(),
                                date
                        ));
                    }

                    tvNoTransactions.setText(transactionList.toString());
                }

                // TODO: Calculate today and week spending (we'll do this next)
                tvTodaySpending.setText("KES 0.00");
                tvWeekSpending.setText("KES 0.00");
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}