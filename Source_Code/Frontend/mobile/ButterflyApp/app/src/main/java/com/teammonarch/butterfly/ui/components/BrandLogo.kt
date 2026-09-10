package com.teammonarch.butterfly.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BrandLogo(
    tint: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 26.sp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🦋", fontSize = fontSize)
        Spacer(Modifier.width(8.dp))
        Text(
            "Lupus Butterfly",
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = tint
        )
    }
}
