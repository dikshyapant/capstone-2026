package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.MedicationLogRow
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.model.GameStage
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import com.teammonarch.butterfly.ui.theme.butterflyColorFor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButterflyBankScreen(onBack: () -> Unit) {
    val profile = AppSession.currentProfile
    val bb = profile?.currentBb ?: 0
    val streak = profile?.currentStreak ?: 0
    val accent = butterflyColorFor(profile?.butterflyColor ?: "gold")
    val stage = GameStage.forBB(bb)
    var recentActivity by remember { mutableStateOf<List<MedicationLogRow>>(emptyList()) }

    LaunchedEffect(profile?.id) {
        val patientId = profile?.id ?: return@LaunchedEffect
        SupabaseRepository.fetchRecentActivity(patientId).onSuccess { recentActivity = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Butterfly Bank") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🫙", style = MaterialTheme.typography.displayLarge, color = accent)
                    Spacer(Modifier.height(8.dp))
                    Text("$bb Butterfly Bucks", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (streak > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text("🔥 $streak-day streak", color = GoldAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Your Progress", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("${stage.emoji} ${stage.label} Stage", style = MaterialTheme.typography.bodyLarge)
                    val nextStage = when (stage) {
                        GameStage.CHRYSALIS -> GameStage.CATERPILLAR
                        GameStage.CATERPILLAR -> GameStage.BUTTERFLY
                        GameStage.BUTTERFLY -> null
                    }
                    Spacer(Modifier.height(8.dp))
                    if (nextStage != null) {
                        val progress = (bb.toFloat() / nextStage.minBB.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = progress,
                            color = accent,
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${nextStage.minBB - bb} BB to reach ${nextStage.label} ${nextStage.emoji}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    } else {
                        Text("You've reached the top stage! 🎉", color = TextMuted)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (recentActivity.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Recent activity", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        recentActivity.forEach { log -> RecentActivityRow(log) }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                "Earn Butterfly Bucks by marking your medications taken on time from the Home screen.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private val RECENT_ACTIVITY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

@Composable
private fun RecentActivityRow(log: MedicationLogRow) {
    val (title, subtitle) = when {
        log.status == "taken_on_time" && log.bbAwarded == 4 -> "On-time dose · 2x bonus" to "2 BB × 2.0"
        log.status == "taken_on_time" -> "On-time dose" to "base award"
        log.status == "taken_late" -> "Late dose" to "outside the on-time window"
        else -> "Dose logged" to log.status
    }
    val amountText = log.bbAwarded?.let { "+$it BB" }
    val dateText = log.scheduledFor?.let { raw ->
        runCatching { LocalDate.parse(raw).format(RECENT_ACTIVITY_DATE_FORMAT) }.getOrNull()
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4A9D6E), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (amountText != null) {
                Text(amountText, fontWeight = FontWeight.Bold, color = GoldAmber)
            }
            if (dateText != null) {
                Text(dateText, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}
