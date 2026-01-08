package com.example.qash;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTransaction extends AppCompatActivity {

    private EditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private RadioButton rbExpense, rbIncome;
    private Button btnSave, btnCancel;

    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize database
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rbExpense = findViewById(R.id.rbExpense);
        rbIncome = findViewById(R.id.rbIncome);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup category spinner
        setupCategorySpinner();

        // Save button click
        btnSave.setOnClickListener(v -> saveTransaction());

        // Cancel button click
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "Transport",
                "Food & Groceries",
                "Bills & Utilities",
                "Entertainment",
                "Airtime & Data",
                "Shopping",
                "Health",
                "Dining",
                "Government",
                "Insurance",
                "Personal Transfer",
                "Withdrawal",
                "Fuliza",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void saveTransaction() {
        // Get values
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String type = rbExpense.isChecked() ? "Expense" : "Income";

        // Validate
        if (amountStr.isEmpty()) {
            etAmount.setError("Please enter amount");
            etAmount.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Please enter description");
            etDescription.requestFocus();
            return;
        }

        // Convert amount to double
        double amount = Double.parseDouble(amountStr);

        // Get current timestamp
        long currentDate = System.currentTimeMillis();

        // Create transaction object
        Transaction transaction = new Transaction(amount, description, category, type, currentDate, null);

        // Save to database in background thread
        executorService.execute(() -> {
            database.transactionDao().insert(transaction);

            // Show success message on main thread
            runOnUiThread(() -> {
                Toast.makeText(this, "Transaction saved successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and go back to MainActivity
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}