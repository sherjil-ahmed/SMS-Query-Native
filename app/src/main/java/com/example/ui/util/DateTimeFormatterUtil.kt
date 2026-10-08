package com.example.ui.util

import com.example.data.model.DatePreset
import com.example.data.model.QueryFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeFormatterUtil {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    fun formatFullDateTime(timestamp: Long): String {
        return fullDateTimeFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatThreadTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calMsg = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameDay = calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return timeFormat.format(Date(timestamp))
        }

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "Yesterday"
        }

        val isSameYear = calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR)
        return if (isSameYear) {
            shortDateFormat.format(Date(timestamp))
        } else {
            dateFormat.format(Date(timestamp))
        }
    }

    fun isMessageInTimeWindow(timestamp: Long, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val msgHour = cal.get(Calendar.HOUR_OF_DAY)
        val msgMinute = cal.get(Calendar.MINUTE)
        val msgMin = msgHour * 60 + msgMinute
        val startMin = startHour * 60 + startMinute
        val endMin = endHour * 60 + endMinute

        return if (startMin <= endMin) {
            msgMin in startMin..endMin
        } else {
            msgMin >= startMin || msgMin <= endMin
        }
    }

    fun getFilterSummary(filter: QueryFilter): String {
        val parts = mutableListOf<String>()

        if (filter.keywords.isNotBlank()) {
            parts.add("\"${filter.keywords}\"")
        }
        if (filter.senderQuery.isNotBlank()) {
            parts.add("From: ${filter.senderQuery}")
        }

        val dateDesc = when (filter.datePreset) {
            DatePreset.ALL_TIME -> if (filter.applyTimeOfDayWindow) "All Dates" else null
            DatePreset.TODAY -> "Today"
            DatePreset.LAST_7_DAYS -> "Last 7 Days"
            DatePreset.LAST_30_DAYS -> "Last 30 Days"
            DatePreset.CUSTOM_RANGE -> {
                val start = filter.customStartDateMillis?.let { formatDate(it) } ?: "Beginning"
                val end = filter.customEndDateMillis?.let { formatDate(it) } ?: "Now"
                "$start to $end"
            }
        }
        if (dateDesc != null) {
            parts.add(dateDesc)
        }

        if (filter.applyTimeOfDayWindow) {
            parts.add(filter.formattedTimeWindow())
        }

        return if (parts.isEmpty()) "All Messages" else parts.joinToString(" • ")
    }
}
