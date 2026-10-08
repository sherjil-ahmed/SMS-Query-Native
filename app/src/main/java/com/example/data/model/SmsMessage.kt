package com.example.data.model

enum class SmsType(val value: Int) {
    INCOMING(1),
    OUTGOING(2);

    companion object {
        fun fromValue(value: Int): SmsType = if (value == 2) OUTGOING else INCOMING
    }
}

data class SmsMessage(
    val id: Long = 0,
    val address: String,
    val senderName: String,
    val body: String,
    val timestamp: Long,
    val type: SmsType = SmsType.INCOMING,
    val isRead: Boolean = true,
    val simSlot: Int = 1
) {
    val isIncoming: Boolean get() = type == SmsType.INCOMING
    val isOutgoing: Boolean get() = type == SmsType.OUTGOING
}
