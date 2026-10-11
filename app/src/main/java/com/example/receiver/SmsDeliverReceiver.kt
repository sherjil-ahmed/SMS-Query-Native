package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SmsEntity
import com.example.data.model.SmsType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsDeliverReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                processIncomingSms(context, intent)
            } catch (e: Exception) {
                Log.e("SmsDeliverReceiver", "Error processing incoming SMS deliver: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processIncomingSms(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Group multipart SMS segments by originating address
        val groupedBySender = messages.groupBy { it.displayOriginatingAddress ?: it.originatingAddress ?: "Unknown" }

        val subId = intent.getIntExtra("subscription", intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1))
        val simSlot = determineSimSlot(context, subId)

        val dao = AppDatabase.getInstance(context).smsDao()

        for ((sender, parts) in groupedBySender) {
            val fullBody = parts.joinToString("") { it.displayMessageBody ?: it.messageBody ?: "" }
            val timestamp = parts.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

            val entity = SmsEntity(
                address = sender,
                senderName = sender,
                body = fullBody,
                timestamp = timestamp,
                type = SmsType.INCOMING.value,
                isRead = false,
                simSlot = simSlot
            )
            dao.insertMessage(entity)
            Log.d("SmsDeliverReceiver", "Received and stored SMS from $sender on SIM $simSlot: $fullBody")
        }
    }

    private fun determineSimSlot(context: Context, subId: Int): Int {
        if (subId <= 0) return 1
        return try {
            val subManager = context.getSystemService(SubscriptionManager::class.java)
            val subInfo = subManager?.activeSubscriptionInfoList?.find { it.subscriptionId == subId }
            if (subInfo != null && subInfo.simSlotIndex >= 0) subInfo.simSlotIndex + 1 else 1
        } catch (e: Exception) {
            1
        }
    }
}
