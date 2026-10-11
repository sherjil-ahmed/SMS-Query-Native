package com.example.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HeadlessSmsSendService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && TelephonyManager.ACTION_RESPOND_VIA_MESSAGE == intent.action) {
            val uri: Uri? = intent.data
            val messageText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (uri != null && !messageText.isNullOrBlank()) {
                val recipient = uri.schemeSpecificPart
                Log.d("HeadlessSmsSendService", "Responding via message to $recipient: $messageText")

                val repository = SmsRepository(applicationContext)
                scope.launch {
                    try {
                        repository.sendMessage(
                            address = recipient,
                            senderName = recipient,
                            body = messageText
                        )
                    } catch (e: Exception) {
                        Log.e("HeadlessSmsSendService", "Error sending message: ${e.message}", e)
                    } finally {
                        stopSelf(startId)
                    }
                }
                return START_NOT_STICKY
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
