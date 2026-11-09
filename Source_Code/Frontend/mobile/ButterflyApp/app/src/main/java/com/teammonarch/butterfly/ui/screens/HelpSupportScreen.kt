package com.teammonarch.butterfly.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet

private const val SUPPORT_EMAIL = "jinhnguyen@my.unt.edu"

private data class FaqItem(val question: String, val answer: String)

private val FAQ = listOf(
    FaqItem(
        "How do I log a dose?",
        "From your Home screen, tap \"Mark Taken\" next to the medication once you've taken it."
    ),
    FaqItem(
        "What counts as on time?",
        "Logging a dose within 1 hour of its scheduled time, either side, counts as on time and earns the adherence halo."
    ),
    FaqItem(
        "What is a Double Monarch Day?",
        "Every second consecutive on-time day pays double Butterfly Bucks ($4 instead of $2)."
    ),
    FaqItem(
        "Who can see my adherence data?",
        "Only your care team can see whether you've taken your medication on time — never your password or login details."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("A note on medical advice", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This app is a support tool to help you track and remember your medication -- it does not replace professional medical advice. Never change your prescribed dosage based on anything shown here; always talk to your doctor or pharmacist about your treatment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            FAQ.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.question, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(item.answer, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Need something else? Reach out to your care team from the Account Info page, or email support directly:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(4.dp))
            Text(
                SUPPORT_EMAIL,
                style = MaterialTheme.typography.bodySmall,
                color = Violet,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$SUPPORT_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, "Lupus Butterfly App Support")
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}
