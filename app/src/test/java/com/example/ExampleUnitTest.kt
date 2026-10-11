package com.example

import com.example.data.model.DatePreset
import com.example.data.model.FilterDirection
import com.example.data.model.QueryFilter
import com.example.data.model.QueryMatcher
import com.example.data.model.SmsMessage
import com.example.data.model.SmsType
import com.example.data.model.TimeWindowPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    private fun createTimestamp(hour: Int, minute: Int, daysAgo: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun testTimeOfDayWindowFiltering_9amTo6pm() {
        val messages = listOf(
            SmsMessage(
                id = 1,
                address = "12345",
                senderName = "Chase",
                body = "Security code is 1234",
                timestamp = createTimestamp(hour = 10, minute = 30), // Inside 9am - 6pm
                type = SmsType.INCOMING
            ),
            SmsMessage(
                id = 2,
                address = "54321",
                senderName = "Sarah",
                body = "Good morning early birds",
                timestamp = createTimestamp(hour = 7, minute = 15), // Outside (before 9am)
                type = SmsType.INCOMING
            ),
            SmsMessage(
                id = 3,
                address = "67890",
                senderName = "Alex",
                body = "Late night movie talk",
                timestamp = createTimestamp(hour = 21, minute = 45), // Outside (after 6pm)
                type = SmsType.INCOMING
            ),
            SmsMessage(
                id = 4,
                address = "11111",
                senderName = "Liam",
                body = "Lunch meeting at noon",
                timestamp = createTimestamp(hour = 12, minute = 0), // Inside 9am - 6pm
                type = SmsType.INCOMING
            )
        )

        val filter = QueryFilter(
            applyTimeOfDayWindow = true,
            timePreset = TimeWindowPreset.BUSINESS_HOURS,
            timeStartHour = 9,
            timeStartMinute = 0,
            timeEndHour = 18,
            timeEndMinute = 0
        )

        val results = QueryMatcher.evaluate(messages, filter)

        assertEquals(2, results.size)
        assertTrue(results.any { it.message.id == 1L })
        assertTrue(results.any { it.message.id == 4L })
    }

    @Test
    fun testKeywordAndSenderFiltering() {
        val messages = listOf(
            SmsMessage(
                id = 1,
                address = "12345",
                senderName = "Chase Bank",
                body = "Your verification code is 884920",
                timestamp = createTimestamp(hour = 14, minute = 0),
                type = SmsType.INCOMING
            ),
            SmsMessage(
                id = 2,
                address = "555-1234",
                senderName = "Sarah Jenkins",
                body = "See you tomorrow for coffee",
                timestamp = createTimestamp(hour = 15, minute = 0),
                type = SmsType.INCOMING
            )
        )

        // Query by keyword "code"
        val filter1 = QueryFilter(keywords = "code")
        val results1 = QueryMatcher.evaluate(messages, filter1)
        assertEquals(1, results1.size)
        assertEquals(1L, results1[0].message.id)
        assertTrue(results1[0].matchedKeywordRanges.isNotEmpty())

        // Query by sender "Sarah"
        val filter2 = QueryFilter(senderQuery = "Sarah")
        val results2 = QueryMatcher.evaluate(messages, filter2)
        assertEquals(1, results2.size)
        assertEquals(2L, results2[0].message.id)
    }

    @Test
    fun testDirectionFilter() {
        val messages = listOf(
            SmsMessage(id = 1, address = "111", senderName = "A", body = "Incoming", timestamp = createTimestamp(10, 0), type = SmsType.INCOMING),
            SmsMessage(id = 2, address = "111", senderName = "A", body = "Outgoing", timestamp = createTimestamp(11, 0), type = SmsType.OUTGOING)
        )

        val incomingOnly = QueryMatcher.evaluate(messages, QueryFilter(direction = FilterDirection.INCOMING_ONLY))
        assertEquals(1, incomingOnly.size)
        assertEquals(1L, incomingOnly[0].message.id)

        val outgoingOnly = QueryMatcher.evaluate(messages, QueryFilter(direction = FilterDirection.OUTGOING_ONLY))
        assertEquals(1, outgoingOnly.size)
        assertEquals(2L, outgoingOnly[0].message.id)
    }

    @Test
    fun testDualSimSupport() {
        val defaultSims = com.example.data.model.SimCard.DEFAULT_DUAL_SIMS
        assertEquals(2, defaultSims.size)
        assertEquals(1, defaultSims[0].slotIndex)
        assertEquals(2, defaultSims[1].slotIndex)
        assertTrue(defaultSims[0].isDefaultSms)

        val msgSim1 = SmsMessage(id = 1, address = "111", senderName = "Bob", body = "Hi", timestamp = 1000L, simSlot = 1)
        val msgSim2 = SmsMessage(id = 2, address = "222", senderName = "Alice", body = "Hello", timestamp = 2000L, simSlot = 2)

        assertEquals(1, msgSim1.simSlot)
        assertEquals(2, msgSim2.simSlot)

        val customSim = com.example.data.model.SimCard(
            slotIndex = 2,
            subscriptionId = 15,
            displayName = "Work",
            carrierName = "T-Mobile",
            isDefaultSms = false,
            isSystemDetected = true
        )
        assertEquals("Work", customSim.shortLabel)
        assertEquals("SIM 2 • Work (T-Mobile)", customSim.fullLabel)
    }

    @Test
    fun testDefaultSmsModelAndPermissions() {
        val sim1 = com.example.data.model.SimCard(
            slotIndex = 1,
            subscriptionId = 1,
            displayName = "Personal",
            carrierName = "Verizon",
            isDefaultSms = true,
            isSystemDetected = true
        )
        assertTrue(sim1.isDefaultSms)
        assertEquals(1, sim1.slotIndex)
        assertEquals("Personal", sim1.shortLabel)
    }
}
