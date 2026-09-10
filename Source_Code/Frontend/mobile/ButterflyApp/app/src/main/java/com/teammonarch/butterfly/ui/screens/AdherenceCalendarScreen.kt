package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Replays the same streak logic as [com.teammonarch.butterfly.data.SupabaseRepository.markMedicationTaken]
 * over the patient's on-time dates to figure out, after the fact, which of those days were
 * Double Monarch Days (every 2nd consecutive on-time day). Nothing needs to be stored per-log
 * for this -- it's fully derivable from the sequence of on-time dates.
 */
private fun computeDoubleMonarchDates(onTimeDates: Set<LocalDate>): Set<LocalDate> {
    val sorted = onTimeDates.sorted()
    var streak = 0
    var previous: LocalDate? = null
    val result = mutableSetOf<LocalDate>()
    for (date in sorted) {
        streak = if (previous != null && date == previous.plusDays(1)) streak + 1 else 1
        if (streak % 2 == 0) result.add(date)
        previous = date
    }
    return result
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdherenceCalendarScreen(onBack: () -> Unit) {
    val profile = AppSession.currentProfile
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var adherentDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(profile?.id) {
        val patientId = profile?.id ?: return@LaunchedEffect
        val result = SupabaseRepository.fetchAdherenceDates(patientId)
        isLoading = false
        result.onSuccess { dateStrings ->
            adherentDates = dateStrings.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adherence History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (profile != null && profile.currentStreak > 0) {
                Text(
                    "🔥 ${profile.currentStreak}-day streak",
                    fontWeight = FontWeight.Bold,
                    color = GoldAmber,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val doubleMonarchDates = remember(adherentDates) { computeDoubleMonarchDates(adherentDates) }
                CalendarGrid(yearMonth = yearMonth, adherentDates = adherentDates, doubleMonarchDates = doubleMonarchDates)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "🌟 Days with a glow mark an on-time dose. ⭐ marks a Double Monarch Day (2x reward).",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun CalendarGrid(yearMonth: YearMonth, adherentDates: Set<LocalDate>, doubleMonarchDates: Set<LocalDate>) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val leadingBlanks = firstDay.dayOfWeek.value % 7 // Sunday = 0

    val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..daysInMonth).map { yearMonth.atDay(it) }
    val weeks = cells.chunked(7)

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                }
            }
        }
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val hasHalo = date in adherentDates
                            val isDoubleMonarch = date in doubleMonarchDates
                            val isToday = date == LocalDate.now()
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .let {
                                        if (hasHalo) it.background(GoldAmber.copy(alpha = if (isDoubleMonarch) 0.55f else 0.35f), CircleShape) else it
                                    }
                                    .let {
                                        if (isToday) it.background(Violet.copy(alpha = 0.15f), CircleShape) else it
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isDoubleMonarch) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Double Monarch Day",
                                        tint = GoldAmber,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .height(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}
