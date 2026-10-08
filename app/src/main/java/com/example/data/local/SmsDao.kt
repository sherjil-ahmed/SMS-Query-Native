package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SmsEntity>>

    @Query("SELECT * FROM sms_messages WHERE address = :address ORDER BY timestamp ASC")
    fun getMessagesForAddress(address: String): Flow<List<SmsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<SmsEntity>)

    @Query("DELETE FROM sms_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM sms_messages WHERE address = :address")
    suspend fun deleteThread(address: String)

    @Query("UPDATE sms_messages SET isRead = 1 WHERE address = :address")
    suspend fun markAsRead(address: String)

    @Query("SELECT COUNT(*) FROM sms_messages")
    suspend fun getMessageCount(): Int

    @Query("DELETE FROM sms_messages")
    suspend fun clearAll()
}
