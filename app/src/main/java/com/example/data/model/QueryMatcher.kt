package com.example.data.model

import java.util.Calendar

object QueryMatcher {

    data class FilterResult(
        val message: SmsMessage,
        val isWithinTimeWindow: Boolean,
        val matchedKeywordRanges: List<IntRange> = emptyList()
    )

    fun evaluate(messages: List<SmsMessage>, filter: QueryFilter): List<FilterResult> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // 1. Calculate Date Range Bounds
        val (minTimestamp, maxTimestamp) = calculateDateBounds(filter, now)

        val startMinOfDay = filter.timeStartHour * 60 + filter.timeStartMinute
        val endMinOfDay = filter.timeEndHour * 60 + filter.timeEndMinute

        val results = mutableListOf<FilterResult>()

        for (msg in messages) {
            // Direction filter
            when (filter.direction) {
                FilterDirection.INCOMING_ONLY -> if (!msg.isIncoming) continue
                FilterDirection.OUTGOING_ONLY -> if (!msg.isOutgoing) continue
                FilterDirection.ALL -> Unit
            }

            // Date Range filter
            if (minTimestamp != null && msg.timestamp < minTimestamp) continue
            if (maxTimestamp != null && msg.timestamp > maxTimestamp) continue

            // Time of Day Window check
            calendar.timeInMillis = msg.timestamp
            val msgHour = calendar.get(Calendar.HOUR_OF_DAY)
            val msgMinute = calendar.get(Calendar.MINUTE)
            val msgMinOfDay = msgHour * 60 + msgMinute

            val inTimeWindow = if (startMinOfDay <= endMinOfDay) {
                msgMinOfDay in startMinOfDay..endMinOfDay
            } else {
                // Overnight window e.g. 10 PM to 6 AM
                msgMinOfDay >= startMinOfDay || msgMinOfDay <= endMinOfDay
            }

            if (filter.applyTimeOfDayWindow && !inTimeWindow) {
                continue
            }

            // Sender Name / Phone Query
            if (filter.senderQuery.isNotBlank()) {
                val queryLower = filter.senderQuery.trim().lowercase()
                val nameMatches = msg.senderName.lowercase().contains(queryLower)
                val addressMatches = msg.address.lowercase().contains(queryLower)
                if (!nameMatches && !addressMatches) {
                    continue
                }
            }

            // Keyword Query (in body)
            val matchedRanges = mutableListOf<IntRange>()
            if (filter.keywords.isNotBlank()) {
                val keywordsList = filter.keywords.trim()
                    .split(",", " ")
                    .filter { it.isNotBlank() }
                    .map { it.lowercase() }

                var hasMatch = false
                val bodyLower = msg.body.lowercase()

                for (kw in keywordsList) {
                    var startIndex = 0
                    while (startIndex < bodyLower.length) {
                        val index = bodyLower.indexOf(kw, startIndex)
                        if (index == -1) break
                        matchedRanges.add(index until (index + kw.length))
                        startIndex = index + kw.length
                        hasMatch = true
                    }
                }

                if (!hasMatch) {
                    continue
                }
            }

            results.add(
                FilterResult(
                    message = msg,
                    isWithinTimeWindow = inTimeWindow,
                    matchedKeywordRanges = matchedRanges
                )
            )
        }

        // Apply Sorting
        return when (filter.sortBy) {
            SortOption.NEWEST_FIRST -> results.sortedByDescending { it.message.timestamp }
            SortOption.OLDEST_FIRST -> results.sortedBy { it.message.timestamp }
            SortOption.SENDER_NAME -> results.sortedWith(
                compareBy<FilterResult> { it.message.senderName.lowercase() }
                    .thenByDescending { it.message.timestamp }
            )
        }
    }

    private fun calculateDateBounds(filter: QueryFilter, now: Long): Pair<Long?, Long?> {
        val cal = Calendar.getInstance()
        return when (filter.datePreset) {
            DatePreset.ALL_TIME -> null to null
            DatePreset.TODAY -> {
                cal.timeInMillis = now
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to null
            }
            DatePreset.LAST_7_DAYS -> {
                cal.timeInMillis = now
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to null
            }
            DatePreset.LAST_30_DAYS -> {
                cal.timeInMillis = now
                cal.add(Calendar.DAY_OF_YEAR, -30)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to null
            }
            DatePreset.CUSTOM_RANGE -> {
                val start = filter.customStartDateMillis?.let {
                    cal.timeInMillis = it
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                val end = filter.customEndDateMillis?.let {
                    cal.timeInMillis = it
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    cal.timeInMillis
                }
                start to end
            }
        }
    }
}
