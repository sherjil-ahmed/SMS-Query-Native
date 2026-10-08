package com.example.data.model

import java.util.Calendar

enum class FilterDirection(val label: String) {
    ALL("All Messages"),
    INCOMING_ONLY("Received"),
    OUTGOING_ONLY("Sent")
}

enum class DatePreset(val label: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    CUSTOM_RANGE("Custom Dates")
}

enum class TimeWindowPreset(val label: String, val startHour: Int, val startMinute: Int, val endHour: Int, val endMinute: Int) {
    BUSINESS_HOURS("9am – 6pm", 9, 0, 18, 0),
    MORNING("6am – 12pm", 6, 0, 12, 0),
    AFTERNOON("12pm – 6pm", 12, 0, 18, 0),
    EVENING("6pm – 10pm", 18, 0, 22, 0),
    CUSTOM("Custom Time", 9, 0, 18, 0)
}

enum class SortOption(val label: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    SENDER_NAME("By Sender")
}

data class QueryFilter(
    val keywords: String = "",
    val senderQuery: String = "",
    val direction: FilterDirection = FilterDirection.ALL,
    val datePreset: DatePreset = DatePreset.ALL_TIME,
    val customStartDateMillis: Long? = null,
    val customEndDateMillis: Long? = null,
    val applyTimeOfDayWindow: Boolean = false,
    val timePreset: TimeWindowPreset = TimeWindowPreset.BUSINESS_HOURS,
    val timeStartHour: Int = 9,
    val timeStartMinute: Int = 0,
    val timeEndHour: Int = 18,
    val timeEndMinute: Int = 0,
    val sortBy: SortOption = SortOption.NEWEST_FIRST
) {
    val isActive: Boolean
        get() = keywords.isNotBlank() ||
                senderQuery.isNotBlank() ||
                direction != FilterDirection.ALL ||
                datePreset != DatePreset.ALL_TIME ||
                applyTimeOfDayWindow ||
                sortBy != SortOption.NEWEST_FIRST

    val activeFiltersCount: Int
        get() {
            var count = 0
            if (keywords.isNotBlank()) count++
            if (senderQuery.isNotBlank()) count++
            if (direction != FilterDirection.ALL) count++
            if (datePreset != DatePreset.ALL_TIME) count++
            if (applyTimeOfDayWindow) count++
            if (sortBy != SortOption.NEWEST_FIRST) count++
            return count
        }

    fun formattedTimeWindow(): String {
        val startFormatted = formatHourMinute(timeStartHour, timeStartMinute)
        val endFormatted = formatHourMinute(timeEndHour, timeEndMinute)
        return "$startFormatted – $endFormatted"
    }

    companion object {
        fun formatHourMinute(hour: Int, minute: Int): String {
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return if (minute == 0) {
                "$displayHour $period"
            } else {
                String.format("%d:%02d %s", displayHour, minute, period)
            }
        }
    }
}
