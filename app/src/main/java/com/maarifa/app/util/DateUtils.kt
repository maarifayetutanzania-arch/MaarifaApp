package com.maarifa.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    fun formatDisplay(date: Date?): String = date?.let { displayFormat.format(it) } ?: "-"

    fun addDays(from: Date, days: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = from
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.time
    }

    fun daysUntil(target: Date?): Long {
        if (target == null) return 0
        val diff = target.time - System.currentTimeMillis()
        return diff / (1000 * 60 * 60 * 24)
    }

    fun isExpired(endDate: Date?): Boolean = endDate == null || endDate.before(Date())

    /** "2026-08" style period key used for grouping engagement/earnings by month. */
    fun currentPeriodKey(): String = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date())
}
