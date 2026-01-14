package com.kevinlemein.qash.data.source

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SystemSmsSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getMessagesFromSender(senderId: String): List<String> {
        val messages = mutableListOf<String>()
        val contentResolver: ContentResolver = context.contentResolver

        // Query the phone's SMS Inbox
        val cursor = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS),
            "${Telephony.Sms.Inbox.ADDRESS} LIKE ?", // Filter by sender
            arrayOf("%$senderId%"), // Matches "MPESA", "M-PESA", etc.
            "${Telephony.Sms.Inbox.DATE} DESC" // Newest first
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                messages.add(body)
            }
        }
        return messages
    }
}