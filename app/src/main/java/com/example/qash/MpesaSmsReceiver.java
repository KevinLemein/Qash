package com.example.qash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class MpesaSmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "MpesaSmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals(SMS_RECEIVED)) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

            String sender = smsMessage.getDisplayOriginatingAddress();
            String messageBody = smsMessage.getMessageBody();

            Log.d(TAG, "SMS received from: " + sender);
            Log.d(TAG, "Message body: " + messageBody);

            // Check if it's an M-Pesa message
            if (isMpesaMessage(sender, messageBody)) {
                Log.d(TAG, "M-Pesa message detected!");

                // Show notification
                MpesaNotificationHelper.showMpesaNotification(context, messageBody);
            }
        }
    }

    private boolean isMpesaMessage(String address, String body) {
        if (address == null || body == null) return false;

        String addressUpper = address.toUpperCase();
        String bodyLower = body.toLowerCase();

        boolean isFromMpesa = addressUpper.contains("MPESA") ||
                addressUpper.equals("SAFARICOM") ||
                addressUpper.contains("M-PESA");

        boolean hasConfirmedKeyword = bodyLower.contains("confirmed");
        boolean hasTransactionCode = body.matches(".*[A-Z0-9]{10}.*");
        boolean hasKshAmount = body.matches(".*Ksh\\s?[0-9,]+\\.?[0-9]*.*");
        boolean hasFuliza = bodyLower.contains("fuliza");

        boolean isTransaction = hasConfirmedKeyword && hasKshAmount && hasTransactionCode;
        boolean isFulizaStatement = hasFuliza && hasKshAmount;

        return isFromMpesa || isTransaction || isFulizaStatement;
    }
}