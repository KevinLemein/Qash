package com.example.qash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MpesaMessagesActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    private RecyclerView rvMpesaMessages;
    private TextView tvNoMessages;
    private Button btnImportAll;
    private MpesaMessageAdapter adapter;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa_messages);

        // Initialize database
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        rvMpesaMessages = findViewById(R.id.rvMpesaMessages);
        tvNoMessages = findViewById(R.id.tvNoMessages);
        btnImportAll = findViewById(R.id.btnImportAll);

        // Setup RecyclerView
        adapter = new MpesaMessageAdapter();
        rvMpesaMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMpesaMessages.setAdapter(adapter);

        // Handle single message click
        adapter.setOnMessageClickListener((message, position) -> {
            importSingleMessage(message, position);
        });

        // Handle import all button
        btnImportAll.setOnClickListener(v -> {
            importAllMessages();
        });

        // Check permission and load messages
        checkSmsPermission();
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            // Permission already granted
            loadMpesaMessages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                loadMpesaMessages();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS permission is required to read M-Pesa messages",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void loadMpesaMessages() {
        executorService.execute(() -> {
            List<MpesaMessage> mpesaMessages = new ArrayList<>();

            // Query SMS inbox
            Uri smsUri = Uri.parse("content://sms/inbox");
            String[] projection = new String[]{"address", "body", "date"};

            Cursor cursor = getContentResolver().query(smsUri, projection, null, null, "date DESC");

            if (cursor != null && cursor.moveToFirst()) {
                int addressIndex = cursor.getColumnIndex("address");
                int bodyIndex = cursor.getColumnIndex("body");
                int dateIndex = cursor.getColumnIndex("date");

                do {
                    String address = cursor.getString(addressIndex);
                    String body = cursor.getString(bodyIndex);
                    long date = cursor.getLong(dateIndex);

                    // Filter only M-Pesa messages
                    if (isMpesaMessage(address, body)) {
                        mpesaMessages.add(new MpesaMessage(body, date));
                    }

                } while (cursor.moveToNext());

                cursor.close();
            }

            // Update UI
            runOnUiThread(() -> {
                if (mpesaMessages.isEmpty()) {
                    rvMpesaMessages.setVisibility(View.GONE);
                    tvNoMessages.setVisibility(View.VISIBLE);
                    btnImportAll.setEnabled(false);
                } else {
                    rvMpesaMessages.setVisibility(View.VISIBLE);
                    tvNoMessages.setVisibility(View.GONE);
                    btnImportAll.setEnabled(true);
                    adapter.setMessages(mpesaMessages);
                }
            });
        });
    }

    private boolean isMpesaMessage(String address, String body) {
        // Check if message is from M-Pesa
        if (address == null || body == null) return false;

        // M-Pesa sender IDs
        return address.toUpperCase().contains("MPESA") ||
                body.toUpperCase().contains("M-PESA") ||
                body.contains("Confirmed.") ||
                body.matches(".*[A-Z0-9]{10}.*Confirmed.*"); // Transaction code pattern
    }

//    private void importSingleMessage(MpesaMessage message, int position) {
//        executorService.execute(() -> {
//
//            // logs
//            android.util.Log.d("MpesaImport", "Starting import for message: " + message.getMessageBody().substring(0, Math.min(50, message.getMessageBody().length())));
//
//            // Parse the SMS
//            MpesaSmsParser.ParsedTransaction parsed = MpesaSmsParser.parse(message.getMessageBody(), database);
//
//            // another log
//            android.util.Log.d("MpesaImport", "Parsed result - Valid: " + parsed.isValid + ", Category: " + parsed.category + ", Amount: " + parsed.amount);
//
//            if (!parsed.isValid) {
//                runOnUiThread(() -> {
//                    Toast.makeText(this, "Failed to parse transaction", Toast.LENGTH_SHORT).show();
//                });
//                return;
//            }
//
//            // Check if this is a Fuliza statement
//            if (parsed.usedFuliza && parsed.description.equals("Fuliza Statement")) {
//                // This is a Fuliza notification - find the original transaction and update it
//                executorService.execute(() -> {
//                    // Find transaction with same M-Pesa code
//                    // Update its balance to show negative
//                    // For now, we'll skip importing Fuliza statements separately
//                    android.util.Log.d("MpesaImport", "Detected Fuliza statement - Outstanding: " +
//                            parsed.fulizaOutstanding);
//                });
//
//                runOnUiThread(() -> {
//                    adapter.markAsImported(position);
//                    Toast.makeText(this, "Fuliza statement detected (Outstanding: KES " +
//                                    String.format(Locale.getDefault(), "%.2f", parsed.fulizaOutstanding) + ")",
//                            Toast.LENGTH_SHORT).show();
//                });
//                return;
//            }
//
//            runOnUiThread(() -> {
//                if (parsed.category == null) {
//                    // Unknown merchant - ask user to categorize
//                    showCategorySelectionDialog(parsed, message, position);
//                } else {
//                    // Known merchant - import directly
//                    saveTransaction(parsed, message, position);
//                }
//            });
//        });
//    }

    private void importSingleMessage(MpesaMessage message, int position) {
        executorService.execute(() -> {

            // Parse the SMS
            MpesaSmsParser.ParsedTransaction parsed = MpesaSmsParser.parse(message.getMessageBody(), database);

            if (!parsed.isValid) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to parse transaction", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Check if this is a Fuliza statement
            if (parsed.usedFuliza && parsed.description.equals("Fuliza Access Fee")) {
                // This is the second SMS - save Fuliza fee separately
                runOnUiThread(() -> {
                    // Check if the actual transaction exists
                    executorService.execute(() -> {
                        Transaction actualTransaction = database.transactionDao().findByMpesaCode(parsed.mpesaCode);

                        if (actualTransaction != null) {
                            // Save Fuliza fee as separate transaction
                            if (parsed.category == null) {
                                parsed.category = "Fuliza";
                            }

                            saveTransaction(parsed, message, position);
                        } else {
                            // Actual transaction not imported yet - ask user to import it first
                            Toast.makeText(this, "Import the actual transaction first", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                return;
            }

            runOnUiThread(() -> {
                if (parsed.category == null) {
                    // Unknown merchant - ask user to categorize
                    showCategorySelectionDialog(parsed, message, position);
                } else {
                    // Known merchant - import directly
                    saveTransaction(parsed, message, position);
                }
            });
        });
    }

    private void showCategorySelectionDialog(MpesaSmsParser.ParsedTransaction parsed,
                                             MpesaMessage message, int position) {
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

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Categorize Transaction");
        builder.setMessage("Merchant: " + parsed.description + "\nAmount: KES " +
                String.format(Locale.getDefault(), "%.2f", parsed.amount));

        final boolean[] rememberMerchant = {true}; // Checkbox state

        builder.setSingleChoiceItems(categories, 0, (dialog, which) -> {
            parsed.category = categories[which];
        });

        builder.setPositiveButton("Import", (dialog, which) -> {
            if (parsed.category == null) {
                parsed.category = categories[0]; // Default to first option
            }

            // Save merchant mapping if checkbox is checked
            if (rememberMerchant[0]) {
                executorService.execute(() -> {
                    String merchantKeyword = parsed.description.toLowerCase();
                    database.merchantCategoryDao().insert(
                            new MerchantCategory(merchantKeyword, parsed.category)
                    );
                });
            }

            // Save the transaction
            saveTransaction(parsed, message, position);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveTransaction(MpesaSmsParser.ParsedTransaction parsed,
                                 MpesaMessage message, int position) {
        executorService.execute(() -> {

            // Check if this transaction already exists
            if (parsed.mpesaCode != null && !parsed.mpesaCode.isEmpty()) {
                int count = database.transactionDao().countByMpesaCode(parsed.mpesaCode);
                if (count > 0) {
                    android.util.Log.d("MpesaImport", "Transaction " + parsed.mpesaCode + " already exists - skipping");

                    runOnUiThread(() -> {
                        adapter.markAsImported(position);
                        Toast.makeText(this, "Transaction already imported", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
            }

            android.util.Log.d("MpesaImport", "Saving transaction: " + parsed.description +
                    ", Amount: " + parsed.amount + ", Code: " + parsed.mpesaCode);

            // Create transaction with M-Pesa code
            Transaction transaction = new Transaction(
                    parsed.amount,
                    parsed.description,
                    parsed.category,
                    parsed.type,
                    message.getDate(),
                    parsed.mpesaCode,
                    parsed.newBalance
            );

            // Save to database
            database.transactionDao().insert(transaction);

            android.util.Log.d("MpesaImport", "Transaction saved successfully!");

            runOnUiThread(() -> {
                adapter.markAsImported(position);
                Toast.makeText(this, "Transaction imported successfully!", Toast.LENGTH_SHORT).show();
            });
        });
    }

//    private void importAllMessages() {
//        List<MpesaMessage> unimported = adapter.getUnimportedMessages();
//
//        if (unimported.isEmpty()) {
//            Toast.makeText(this, "All messages already imported", Toast.LENGTH_SHORT).show();n
//            return;
//        }
//
//        Toast.makeText(this, "Importing " + unimported.size() + " messages...",
//                Toast.LENGTH_SHORT).show();
//
//        // TODO: Import all messages
//        // For now, just mark them as imported
//        for (int i = 0; i < adapter.getItemCount(); i++) {
//            adapter.markAsImported(i);
//        }
//    }

    private void importAllMessages() {
        List<MpesaMessage> unimported = adapter.getUnimportedMessages();

        if (unimported.isEmpty()) {
            Toast.makeText(this, "All messages already imported", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        btnImportAll.setEnabled(false);

        Toast.makeText(this, "Importing " + unimported.size() + " messages...",
                Toast.LENGTH_LONG).show();

        executorService.execute(() -> {
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < unimported.size(); i++) {
                MpesaMessage message = unimported.get(i);

                // Log progress
                android.util.Log.d("MpesaImport", "Importing message " + (i + 1) + "/" + unimported.size());

                // Parse the SMS
                MpesaSmsParser.ParsedTransaction parsed = MpesaSmsParser.parse(message.getMessageBody(), database);

                if (!parsed.isValid) {
                    failCount++;
                    android.util.Log.d("MpesaImport", "Failed to parse message " + (i + 1));
                    continue;
                }

                // ADD THIS - Check for duplicate before category check
                if (parsed.mpesaCode != null && !parsed.mpesaCode.isEmpty()) {
                    int count = database.transactionDao().countByMpesaCode(parsed.mpesaCode);
                    if (count > 0) {
                        android.util.Log.d("MpesaImport", "Message " + (i + 1) + " already imported - skipping");

                        // Mark as imported in UI
                        final int pos = adapter.getMessages().indexOf(message);
                        if (pos >= 0) {
                            runOnUiThread(() -> adapter.markAsImported(pos));
                        }
                        continue;
                    }
                }

                // If category is null, skip (needs manual categorization)
                if (parsed.category == null) {
                    failCount++;
                    android.util.Log.d("MpesaImport", "Message " + (i + 1) + " needs manual categorization - skipped");
                    continue;
                }

                // Save transaction
                try {
                    Transaction transaction = new Transaction(
                            parsed.amount,
                            parsed.description,
                            parsed.category,
                            parsed.type,
                            message.getDate(),
                            parsed.mpesaCode,
                            parsed.newBalance
                    );

                    database.transactionDao().insert(transaction);
                    successCount++;

                    // Mark as imported in UI
                    final int position = adapter.getMessages().indexOf(message);
                    if (position >= 0) {
                        runOnUiThread(() -> adapter.markAsImported(position));
                    }

                    android.util.Log.d("MpesaImport", "Successfully imported message " + (i + 1));

                } catch (Exception e) {
                    failCount++;
                    android.util.Log.e("MpesaImport", "Error saving message " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Show final result
            final int finalSuccess = successCount;
            final int finalFail = failCount;

            runOnUiThread(() -> {
                btnImportAll.setEnabled(true);

                String resultMessage = "Import complete!\n" +
                        "✓ Imported: " + finalSuccess + "\n" +
                        "✗ Failed/Skipped: " + finalFail;

                Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show();

                android.util.Log.d("MpesaImport", resultMessage);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}