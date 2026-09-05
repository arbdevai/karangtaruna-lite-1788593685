package id.or.karangtaruna.core.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val locale = Locale.forLanguageTag("id-ID")
    private val zone = ZoneId.systemDefault()
    private val numberFormat = NumberFormat.getCurrencyInstance(locale).apply { maximumFractionDigits = 0 }
    private val fullDate = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    private val compactDate = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    private val timeFormat = DateTimeFormatter.ofPattern("HH.mm", locale)

    fun rupiah(amount: Long): String = numberFormat.format(amount).replace("Rp", "Rp ")
    fun date(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).atZone(zone).format(compactDate)
    fun fullDate(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).atZone(zone).format(fullDate)
    fun monthYear(year: Int, month: Int): String = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
    fun relativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val timeStr = Instant.ofEpochMilli(epochMillis).atZone(zone).format(timeFormat)
        return when (date) {
            today -> "Hari ini, $timeStr"
            today.minusDays(1) -> "Kemarin, $timeStr"
            else -> date(epochMillis)
        }
    }
}
