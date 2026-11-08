package com.qawmialumni.app.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatter {
    private val bengaliDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    fun toBengaliDigits(input: String): String {
        return input.map { bengaliDigits[it] ?: it }.joinToString("")
    }

    fun formatRelativeBangla(timestamp: Long): String {
        if (timestamp <= 0L) return "কিছুক্ষণ আগে"
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            diff < 0 -> "এখনই"
            seconds < 60 -> "এইমাত্র"
            minutes < 60 -> "${toBengaliDigits(minutes.toString())} মিনিট আগে"
            hours < 24 -> "${toBengaliDigits(hours.toString())} ঘণ্টা আগে"
            days < 7 -> "${toBengaliDigits(days.toString())} দিন আগে"
            else -> {
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                toBengaliDigits(sdf.format(Date(timestamp)))
            }
        }
    }

    fun formatFullBangla(timestamp: Long): String {
        if (timestamp <= 0L) return "সময় অজ্ঞাত"
        val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        return toBengaliDigits(sdf.format(Date(timestamp)))
    }
}
