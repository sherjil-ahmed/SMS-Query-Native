package com.example.data.model

data class ConversationThread(
    val address: String,
    val senderName: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val totalCount: Int
)
