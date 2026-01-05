package com.example.qash;

import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;


public class AddTransaction extends AppCompatActivity {

    private TextInputEditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private RadioButton rbExpense, rbIncome;
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

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
        // Categories array
        String[] categories = {
                "Transport",
                "Food & Groceries",
                "Bills & Utilities",
                "Entertainment",
                "Airtime & Data",
                "Shopping",
                "Health",
                "Other"
        };

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to spinner
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

        // For now, just show a toast with the data
        String message = String.format(
                "Saved!\nType: %s\nAmount: KES %.2f\nCategory: %s\nDescription: %s",
                type, amount, category, description
        );

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // TODO: Save to database (we'll do this next)

        // Close activity
        finish();
    }
}