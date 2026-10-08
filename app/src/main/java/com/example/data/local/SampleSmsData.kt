package com.example.data.local

import com.example.data.model.SmsType
import java.util.Calendar

object SampleSmsData {

    private fun timeAt(daysAgo: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getInitialSamples(): List<SmsEntity> {
        return listOf(
            // --- Contact 1: Sarah Jenkins (Friend / Colleague) ---
            SmsEntity(
                address = "+1 (555) 234-8901",
                senderName = "Sarah Jenkins",
                body = "Hey! Are you still free for lunch tomorrow around 12:30 PM?",
                timestamp = timeAt(daysAgo = 0, hour = 14, minute = 30), // Today 2:30 PM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = false
            ),
            SmsEntity(
                address = "+1 (555) 234-8901",
                senderName = "Sarah Jenkins",
                body = "Yes absolutely! Let's go to that new bistro downtown.",
                timestamp = timeAt(daysAgo = 0, hour = 14, minute = 35), // Today 2:35 PM
                type = SmsType.OUTGOING.value,
                isRead = true
            ),
            SmsEntity(
                address = "+1 (555) 234-8901",
                senderName = "Sarah Jenkins",
                body = "Awesome, table booked under my name! See you then.",
                timestamp = timeAt(daysAgo = 0, hour = 14, minute = 42), // Today 2:42 PM
                type = SmsType.INCOMING.value,
                isRead = false
            ),
            SmsEntity(
                address = "+1 (555) 234-8901",
                senderName = "Sarah Jenkins",
                body = "Just got home, had a really long day with the client presentation.",
                timestamp = timeAt(daysAgo = 2, hour = 21, minute = 15), // 2 days ago 9:15 PM (outside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 2: Chase Bank Alerts ---
            SmsEntity(
                address = "24273",
                senderName = "Chase Bank",
                body = "Chase Alert: Your one-time verification code is 849204. Valid for 10 minutes. Do not share this security code with anyone.",
                timestamp = timeAt(daysAgo = 0, hour = 10, minute = 15), // Today 10:15 AM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),
            SmsEntity(
                address = "24273",
                senderName = "Chase Bank",
                body = "Chase Alert: A charge of $47.50 at Trader Joe's was approved on card ending in 4108.",
                timestamp = timeAt(daysAgo = 3, hour = 16, minute = 20), // 3 days ago 4:20 PM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),
            SmsEntity(
                address = "24273",
                senderName = "Chase Bank",
                body = "Chase Alert: Security alert! Unrecognized sign-in attempt detected from Chrome on macOS. If this was you, reply YES, otherwise reply NO.",
                timestamp = timeAt(daysAgo = 18, hour = 23, minute = 5), // 18 days ago 11:05 PM (outside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 3: Amazon Delivery ---
            SmsEntity(
                address = "262966",
                senderName = "Amazon Logistics",
                body = "Your package with order #112-984021 is out for delivery with driver Carlos. Track your delivery live at amzn.to/track.",
                timestamp = timeAt(daysAgo = 1, hour = 11, minute = 10), // Yesterday 11:10 AM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),
            SmsEntity(
                address = "262966",
                senderName = "Amazon Logistics",
                body = "Delivered: Your package was handed directly to resident or placed near front door. Thank you for shopping with Amazon.",
                timestamp = timeAt(daysAgo = 1, hour = 13, minute = 45), // Yesterday 1:45 PM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 4: Alex Rivera ---
            SmsEntity(
                address = "+1 (555) 908-1122",
                senderName = "Alex Rivera",
                body = "Did you check out the keynote announcement today? The new processor benchmarks look unbelievable.",
                timestamp = timeAt(daysAgo = 1, hour = 19, minute = 40), // Yesterday 7:40 PM (outside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),
            SmsEntity(
                address = "+1 (555) 908-1122",
                senderName = "Alex Rivera",
                body = "Yeah, especially the battery efficiency improvements. I might upgrade this year.",
                timestamp = timeAt(daysAgo = 1, hour = 19, minute = 48), // Yesterday 7:48 PM
                type = SmsType.OUTGOING.value,
                isRead = true
            ),
            SmsEntity(
                address = "+1 (555) 908-1122",
                senderName = "Alex Rivera",
                body = "Sending you the PDF breakdown from AnandTech when I get to my desk.",
                timestamp = timeAt(daysAgo = 4, hour = 10, minute = 5), // 4 days ago 10:05 AM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 5: Dr. Martinez Dental ---
            SmsEntity(
                address = "+1 (555) 762-3490",
                senderName = "Dr. Martinez Dental",
                body = "Reminder: You have an upcoming dental hygiene appointment scheduled for Thursday, Oct 15 at 10:00 AM. Reply C to confirm or R to reschedule.",
                timestamp = timeAt(daysAgo = 4, hour = 9, minute = 15), // 4 days ago 9:15 AM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),
            SmsEntity(
                address = "+1 (555) 762-3490",
                senderName = "Dr. Martinez Dental",
                body = "C",
                timestamp = timeAt(daysAgo = 4, hour = 9, minute = 18), // 4 days ago 9:18 AM
                type = SmsType.OUTGOING.value,
                isRead = true
            ),
            SmsEntity(
                address = "+1 (555) 762-3490",
                senderName = "Dr. Martinez Dental",
                body = "Thank you! Your appointment is confirmed. Please arrive 10 minutes early.",
                timestamp = timeAt(daysAgo = 4, hour = 9, minute = 20), // 4 days ago 9:20 AM
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 6: Liam Chen (Work Projects) ---
            SmsEntity(
                address = "+1 (555) 432-8765",
                senderName = "Liam Chen",
                body = "Good morning! The client approved the revised scope of work. We can proceed with sprint deployment.",
                timestamp = timeAt(daysAgo = 5, hour = 8, minute = 45), // 5 days ago 8:45 AM (outside 9am-6pm - morning)
                type = SmsType.INCOMING.value,
                isRead = true
            ),
            SmsEntity(
                address = "+1 (555) 432-8765",
                senderName = "Liam Chen",
                body = "Fantastic news Liam. I'll merge the release branch right now.",
                timestamp = timeAt(daysAgo = 5, hour = 9, minute = 2), // 5 days ago 9:02 AM (inside 9am-6pm)
                type = SmsType.OUTGOING.value,
                isRead = true
            ),
            SmsEntity(
                address = "+1 (555) 432-8765",
                senderName = "Liam Chen",
                body = "Meeting at 3:00 PM today with the design team to review the query filtering UI specs.",
                timestamp = timeAt(daysAgo = 6, hour = 11, minute = 30), // 6 days ago 11:30 AM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 7: SkyLine Airlines ---
            SmsEntity(
                address = "87221",
                senderName = "SkyLine Airlines",
                body = "Flight Alert: UA 482 departing SFO to JFK is on time. Gate updated to B22. Boarding starts at 3:15 PM.",
                timestamp = timeAt(daysAgo = 8, hour = 14, minute = 10), // 8 days ago 2:10 PM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 8: Metro Power & Electric ---
            SmsEntity(
                address = "+1 (555) 889-0145",
                senderName = "Metro Utility",
                body = "Your monthly electric utility invoice for September ($76.84) has been generated. Due date is Oct 24. Pay online: utility.example.com",
                timestamp = timeAt(daysAgo = 12, hour = 15, minute = 0), // 12 days ago 3:00 PM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 9: Apex Cloud Support ---
            SmsEntity(
                address = "44910",
                senderName = "Apex Cloud Support",
                body = "Support Ticket #99214: Your SSL certificate renewal has been verified and deployed successfully.",
                timestamp = timeAt(daysAgo = 22, hour = 16, minute = 45), // 22 days ago 4:45 PM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),
            SmsEntity(
                address = "44910",
                senderName = "Apex Cloud Support",
                body = "Critical Maintenance notice: Scheduled database migration will take place this Sunday at 2:00 AM UTC.",
                timestamp = timeAt(daysAgo = 28, hour = 7, minute = 30), // 28 days ago 7:30 AM (outside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            ),

            // --- Contact 10: Pulse Fitness Club ---
            SmsEntity(
                address = "+1 (555) 312-9988",
                senderName = "Pulse Fitness",
                body = "Reminder: Your high-intensity cycling class is booked for tomorrow morning at 6:30 AM with Instructor Maya.",
                timestamp = timeAt(daysAgo = 34, hour = 17, minute = 15), // 34 days ago 5:15 PM (inside 9am-6pm)
                type = SmsType.INCOMING.value,
                isRead = true
            )
        )
    }
}
