package com.example.qash;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuickCategorizeActivity extends AppCompatActivity {

    private TextView tvAmount, tvOriginalDescription;
    private EditText etCustomDescription;
    private Spinner spinnerCategory;
    private RadioButton rbExpense, rbIncome;
    private Button btnSave, btnSkip;

    private AppDatabase database;
    private ExecutorService executorService;

    private double amount;
    private String originalDescription;
    private String type;
    private String mpesaCode;
    private String suggestedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_categorize);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        tvAmount = findViewById(R.id.tvAmount);
        tvOriginalDescription = findViewById(R.id.tvOriginalDescription);
        etCustomDescription = findViewById(R.id.etCustomDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rbExpense = findViewById(R.id.rbExpense);
        rbIncome = findViewById(R.id.rbIncome);
        btnSave = findViewById(R.id.btnSave);
        btnSkip = findViewById(R.id.btnSkip);

        // Get data from intent
        amount = getIntent().getDoubleExtra("amount", 0);
        originalDescription = getIntent().getStringExtra("description");
        type = getIntent().getStringExtra("type");
        mpesaCode = getIntent().getStringExtra("mpesa_code");
        suggestedCategory = getIntent().getStringExtra("category");

        // Setup UI
        setupCategorySpinner();
        populateFields();

        // Button listeners
        btnSave.setOnClickListener(v -> saveTransaction());
        btnSkip.setOnClickListener(v -> finish());
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

    private void populateFields() {
        // Set amount
        tvAmount.setText(String.format(Locale.getDefault(), "KES %.2f", amount));

        // Set original description
        tvOriginalDescription.setText("From: " + originalDescription);

        // Set custom description hint
        etCustomDescription.setHint("Custom description (optional)");

        // Set transaction type radio button
        if ("Expense".equals(type)) {
            rbExpense.setChecked(true);
        } else {
            rbIncome.setChecked(true);
        }

        // Set suggested category if available
        if (suggestedCategory != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
            int position = adapter.getPosition(suggestedCategory);
            if (position >= 0) {
                spinnerCategory.setSelection(position);
            }
        }
    }

    private void saveTransaction() {
        String customDesc = etCustomDescription.getText().toString().trim();
        String finalDescription = customDesc.isEmpty() ? originalDescription : customDesc;
        String category = spinnerCategory.getSelectedItem().toString();
        String finalType = rbExpense.isChecked() ? "Expense" : "Income";

        // Check for duplicate
        executorService.execute(() -> {
            if (mpesaCode != null && !mpesaCode.isEmpty()) {
                int count = database.transactionDao().countByMpesaCode(mpesaCode);
                if (count > 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Transaction already saved", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
            }

            double mpesaBalance = getIntent().getDoubleExtra("new_balance", 0);

            // Save transaction
            Transaction transaction = new Transaction(
                    amount,
                    finalDescription,
                    category,
                    finalType,
                    System.currentTimeMillis(),
                    mpesaCode,
                    mpesaBalance
            );

            database.transactionDao().insert(transaction);

            // Save merchant mapping
            if (!customDesc.isEmpty()) {
                database.merchantCategoryDao().insert(
                        new MerchantCategory(originalDescription.toLowerCase(), category)
                );
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}