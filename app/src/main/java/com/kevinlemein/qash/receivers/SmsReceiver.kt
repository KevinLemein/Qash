package com.kevinlemein.qash.receivers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kevinlemein.qash.MainActivity
import com.kevinlemein.qash.R // Ensure this R matches your package
import com.kevinlemein.qash.domain.repository.SmsRepository
import com.kevinlemein.qash.domain.usecase.ParseSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: SmsRepository
    @Inject lateinit var parseSmsUseCase: ParseSmsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            messages?.forEach { sms ->
                val sender = sms.originatingAddress ?: ""
                val body = sms.messageBody ?: ""

                // 1. Filter for M-PESA
                if (sender.contains("MPESA", ignoreCase = true)) {

                    CoroutineScope(Dispatchers.IO).launch {
                        // 2. Parse Logic (Now includes Airtime!)
                        val transaction = parseSmsUseCase(body)

                        if (transaction != null) {
                            repository.saveTransaction(transaction)

                            // 3. Trigger Notification
                            showNotification(context, transaction.description, transaction.amount)
                        }
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, description: String, amount: Double) {
        val channelId = "qash_transactions"
        val notificationId = Random.nextInt() // Unique ID for every notification

        // 1. Create Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Qash Transactions"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = "Notifications for new M-Pesa transactions"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create Intent to open App when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build Notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Uses your app icon
            .setContentTitle("New Transaction: Ksh $amount")
            .setContentText("$description - Tap to categorize")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 4. Show it (Check permission first)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}