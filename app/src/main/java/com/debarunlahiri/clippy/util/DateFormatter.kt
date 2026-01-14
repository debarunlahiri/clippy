package com.debarunlahiri.clippy.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Helper object for formatting dates and times */
object DateFormatter {

    /** Format timestamp as relative time (e.g., "2 minutes ago", "Yesterday") */
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                when (days) {
                    1L -> "Yesterday"
                    else -> "$days days ago"
                }
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "$months ${if (months == 1L) "month" else "months"} ago"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "$years ${if (years == 1L) "year" else "years"} ago"
            }
        }
    }

    /** Format timestamp as absolute date and time */
    fun formatAbsoluteDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    /** Format timestamp as date only */
    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return format.format(date)
    }

    /** Format timestamp as time only */
    fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(date)
    }
}
