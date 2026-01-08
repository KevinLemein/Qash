package com.example.qash;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class MpesaNotificationHelper {

    private static final String CHANNEL_ID = "mpesa_transactions";
    private static final String CHANNEL_NAME = "M-Pesa Transactions";
    private static final int NOTIFICATION_ID = 1001;

    public static void showMpesaNotification(Context context, String smsBody) {
        // Create notification channel (required for Android 8.0+)
        createNotificationChannel(context);

        // Parse the SMS to get details
        AppDatabase db = AppDatabase.getInstance(context);
        MpesaSmsParser.ParsedTransaction parsed = MpesaSmsParser.parse(smsBody, db);

        if (!parsed.isValid) {
            return; // Don't show notification for invalid transactions
        }

        // Prepare notification content
        String title = parsed.type.equals("Expense") ? "Money Sent" : "Money Received";
        String amount = String.format("KES %.2f", parsed.amount);
        String description = parsed.description;

        // Create intent to open categorization dialog
        Intent intent = new Intent(context, QuickCategorizeActivity.class);
        intent.putExtra("sms_body", smsBody);
        intent.putExtra("amount", parsed.amount);
        intent.putExtra("description", parsed.description);
        intent.putExtra("type", parsed.type);
        intent.putExtra("mpesa_code", parsed.mpesaCode);
        intent.putExtra("category", parsed.category);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(amount + " - " + description)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(amount + " - " + description + "\n\nTap to categorize"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Categorize", pendingIntent);

        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for M-Pesa transactions requiring categorization");

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}