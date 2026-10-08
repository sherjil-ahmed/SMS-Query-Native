package com.example.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.local.SampleSmsData
import com.example.data.local.SmsEntity
import com.example.data.model.ConversationThread
import com.example.data.model.SmsMessage
import com.example.data.model.SmsType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

import android.provider.ContactsContract

class SmsRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.smsDao()

    val allMessages: Flow<List<SmsMessage>> = dao.getAllMessages().map { entities ->
        entities.map { it.toDomain() }
    }

    val conversationThreads: Flow<List<ConversationThread>> = allMessages.map { messages ->
        messages.groupBy { it.address }
            .map { (address, msgs) ->
                val sorted = msgs.sortedByDescending { it.timestamp }
                val latest = sorted.first()
                val unread = msgs.count { !it.isRead && it.isIncoming }
                ConversationThread(
                    address = address,
                    senderName = latest.senderName.ifBlank { address },
                    lastMessage = latest.body,
                    lastTimestamp = latest.timestamp,
                    unreadCount = unread,
                    totalCount = msgs.size
                )
            }
            .sortedByDescending { it.lastTimestamp }
    }

    fun getMessagesForAddress(address: String): Flow<List<SmsMessage>> {
        return dao.getMessagesForAddress(address).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val count = dao.getMessageCount()
        if (count == 0) {
            dao.insertAll(SampleSmsData.getInitialSamples())
        }
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        dao.clearAll()
        dao.insertAll(SampleSmsData.getInitialSamples())
    }

    suspend fun sendMessage(
        address: String,
        senderName: String,
        body: String,
        simSlot: Int = 1,
        subscriptionId: Int? = null
    ): Result<SmsMessage> =
        withContext(Dispatchers.IO) {
            try {
                // If SEND_SMS permission is granted, dispatch via system SmsManager for selected SIM subscription
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        val resolvedSubId = if (subscriptionId != null && subscriptionId != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID && subscriptionId > 0) {
                            subscriptionId
                        } else {
                            // Try resolving from SubscriptionManager for the chosen simSlot
                            try {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                                    val subManager = context.getSystemService(android.telephony.SubscriptionManager::class.java)
                                    val activeSubs = subManager?.activeSubscriptionInfoList
                                    val match = activeSubs?.find {
                                        val s = if (it.simSlotIndex >= 0) it.simSlotIndex + 1 else 1
                                        s == simSlot
                                    }
                                    match?.subscriptionId
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }

                        val smsManager = try {
                            if (resolvedSubId != null && resolvedSubId != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID && resolvedSubId > 0) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    context.getSystemService(SmsManager::class.java)?.createForSubscriptionId(resolvedSubId)
                                        ?: @Suppress("DEPRECATION") SmsManager.getSmsManagerForSubscriptionId(resolvedSubId)
                                } else {
                                    @Suppress("DEPRECATION")
                                    SmsManager.getSmsManagerForSubscriptionId(resolvedSubId)
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    context.getSystemService(SmsManager::class.java)
                                        ?: @Suppress("DEPRECATION") SmsManager.getDefault()
                                } else {
                                    @Suppress("DEPRECATION")
                                    SmsManager.getDefault()
                                }
                            }
                        } catch (e: Exception) {
                            @Suppress("DEPRECATION")
                            SmsManager.getDefault()
                        }

                        if (body.length > 160) {
                            val parts = smsManager?.divideMessage(body)
                            if (parts != null && parts.size > 1) {
                                smsManager.sendMultipartTextMessage(address, null, parts, null, null)
                            } else {
                                smsManager?.sendTextMessage(address, null, body, null, null)
                            }
                        } else {
                            smsManager?.sendTextMessage(address, null, body, null, null)
                        }
                    } catch (e: Exception) {
                        Log.w("SmsRepository", "Could not send SMS via hardware carrier (expected on emulator): ${e.message}")
                    }
                }

                val entity = SmsEntity(
                    address = address,
                    senderName = senderName.ifBlank { address },
                    body = body,
                    timestamp = System.currentTimeMillis(),
                    type = SmsType.OUTGOING.value,
                    isRead = true,
                    simSlot = simSlot
                )
                val id = dao.insertMessage(entity)
                Result.success(entity.copy(id = id).toDomain())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun simulateIncomingMessage(address: String, senderName: String, body: String): Result<SmsMessage> =
        withContext(Dispatchers.IO) {
            try {
                val entity = SmsEntity(
                    address = address,
                    senderName = senderName.ifBlank { address },
                    body = body,
                    timestamp = System.currentTimeMillis(),
                    type = SmsType.INCOMING.value,
                    isRead = false
                )
                val id = dao.insertMessage(entity)
                Result.success(entity.copy(id = id).toDomain())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun markThreadAsRead(address: String) = withContext(Dispatchers.IO) {
        dao.markAsRead(address)
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMessage(id)
    }

    suspend fun deleteThread(address: String) = withContext(Dispatchers.IO) {
        dao.deleteThread(address)
    }

    private fun resolveContactName(phoneNumber: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex != -1) cursor.getString(nameIndex) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncDeviceSms(): Int = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext 0
        }

        var importedCount = 0
        try {
            val uri = Uri.parse("content://sms")
            val projection = arrayOf("_id", "address", "body", "date", "type", "read")
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC LIMIT 100")

            cursor?.use {
                val newEntities = mutableListOf<SmsEntity>()
                val addressCol = it.getColumnIndex("address")
                val bodyCol = it.getColumnIndex("body")
                val dateCol = it.getColumnIndex("date")
                val typeCol = it.getColumnIndex("type")
                val readCol = it.getColumnIndex("read")

                while (it.moveToNext()) {
                    val address = it.getString(addressCol) ?: "Unknown"
                    val body = it.getString(bodyCol) ?: ""
                    val date = it.getLong(dateCol)
                    val typeVal = it.getInt(typeCol) // 1 = inbox, 2 = sent
                    val readVal = it.getInt(readCol) == 1

                    val resolvedName = resolveContactName(address) ?: address

                    newEntities.add(
                        SmsEntity(
                            address = address,
                            senderName = resolvedName,
                            body = body,
                            timestamp = date,
                            type = if (typeVal == 2) SmsType.OUTGOING.value else SmsType.INCOMING.value,
                            isRead = readVal
                        )
                    )
                }

                if (newEntities.isNotEmpty()) {
                    dao.insertAll(newEntities)
                    importedCount = newEntities.size
                }
            }
        } catch (e: Exception) {
            Log.e("SmsRepository", "Error reading system SMS content provider: ${e.message}")
        }
        importedCount
    }
}
