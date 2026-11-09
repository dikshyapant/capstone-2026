package com.teammonarch.butterfly.ui.screens

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.PatientAdherenceSummary
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextMuted
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Builds a CSV export of this month's per-patient adherence summary (REQ-15/16: monthly reports, exportable as CSV). */
private fun buildCsv(yearMonth: YearMonth, summaries: List<PatientAdherenceSummary>): String {
    val header = "Month,Patient,Total Doses Logged,On Time,Late,On-Time %"
    val monthLabel = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}"
    val rows = summaries.joinToString("\n") { s ->
        "$monthLabel,${s.patientName},${s.totalLogs},${s.onTimeCount},${s.lateCount},${s.onTimePercent}%"
    }
    return if (rows.isEmpty()) header else "$header\n$rows"
}

/** Saves the CSV into the device's public Downloads folder (visible in the Files app) via MediaStore -- no storage permission needed on Android 10+. */
private fun saveCsvToDownloads(context: Context, fileName: String, csv: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    return try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return false
        resolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) } ?: return false
        true
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var summaries by remember { mutableStateOf<List<PatientAdherenceSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(yearMonth) {
        isLoading = true
        SupabaseRepository.fetchMonthlyAdherenceSummary(yearMonth)
            .onSuccess { summaries = it }
            .onFailure { errorMessage = it.message }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Adherence Report") },
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

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (summaries.isEmpty()) {
                Text("No patients found.", color = TextMuted, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(summaries) { s ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.patientName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${s.totalLogs} dose${if (s.totalLogs == 1) "" else "s"} logged · ${s.onTimeCount} on time · ${s.lateCount} late",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    "${s.onTimePercent}%",
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        s.totalLogs == 0 -> TextMuted
                                        s.onTimePercent >= 80 -> Color(0xFF4A9D6E)
                                        s.onTimePercent >= 50 -> GoldAmber
                                        else -> MonarchRed
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val fileName = "adherence_report_$yearMonth.csv"
                        val csv = buildCsv(yearMonth, summaries)
                        val saved = saveCsvToDownloads(context, fileName, csv)
                        if (saved) {
                            Toast.makeText(context, "Saved to Downloads as $fileName", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Couldn't save the file", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download CSV")
                }
            }

            if (errorMessage != null) {
                Text(errorMessage.orEmpty(), color = MonarchRed, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
