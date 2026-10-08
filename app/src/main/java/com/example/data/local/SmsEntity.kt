package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SmsMessage
import com.example.data.model.SmsType

@Entity(tableName = "sms_messages")
data class SmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String,
    val senderName: String,
    val body: String,
    val timestamp: Long,
    val type: Int, // 1 = incoming, 2 = outgoing
    val isRead: Boolean = true,
    val simSlot: Int = 1
) {
    fun toDomain(): SmsMessage {
        return SmsMessage(
            id = id,
            address = address,
            senderName = senderName,
            body = body,
            timestamp = timestamp,
            type = SmsType.fromValue(type),
            isRead = isRead,
            simSlot = simSlot
        )
    }

    companion object {
        fun fromDomain(domain: SmsMessage): SmsEntity {
            return SmsEntity(
                id = domain.id,
                address = domain.address,
                senderName = domain.senderName,
                body = domain.body,
                timestamp = domain.timestamp,
                type = domain.type.value,
                isRead = domain.isRead,
                simSlot = domain.simSlot
            )
        }
    }
}
