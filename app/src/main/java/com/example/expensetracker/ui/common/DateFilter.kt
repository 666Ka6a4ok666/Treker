package com.example.expensetracker.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val displayDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("uk", "UA"))

/** Форматує мітку часу у людяний вигляд. */
fun formatDateTime(epochMillis: Long): String = displayDateFormat.format(Date(epochMillis))

enum class DatePeriod(val label: String) {
    ALL("Всі"),
    TODAY("Сьогодні"),
    WEEK("7 днів"),
    MONTH("Місяць"),
    QUARTER("Квартал");

    /** Повертає нижню межу періоду (0 — без обмежень) для фільтрації «від дати». */
    fun startMillis(now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        when (this) {
            ALL -> return 0L
            TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            WEEK -> cal.add(Calendar.DAY_OF_YEAR, -6)
            MONTH -> cal.add(Calendar.MONTH, -1)
            QUARTER -> cal.add(Calendar.MONTH, -3)
        }
        return cal.timeInMillis
    }

    /** Перевіряє, чи дата входить у період. */
    fun contains(dateMillis: Long, now: Long = System.currentTimeMillis()): Boolean {
        return dateMillis >= startMillis(now) && dateMillis <= now
    }
}

/** Готовий ряд чіпів вибору періоду. */
@Composable
fun DatePeriodChips(
    selected: DatePeriod,
    onSelect: (DatePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DatePeriod.entries.forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick = { onSelect(period) },
                label = { Text(period.label) }
            )
        }
    }
}
