package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.model.GameStage
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import com.teammonarch.butterfly.ui.theme.butterflyColorFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButterflyBankScreen(onBack: () -> Unit) {
    val profile = AppSession.currentProfile
    val bb = profile?.currentBb ?: 0
    val streak = profile?.currentStreak ?: 0
    val accent = butterflyColorFor(profile?.butterflyColor ?: "gold")
    val stage = GameStage.forBB(bb)

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

            Text(
                "Earn Butterfly Bucks by marking your medications taken on time from the Home screen.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
