package com.teammonarch.butterfly.ui.theme

import androidx.compose.ui.graphics.Color

data class ButterflyColorOption(val id: String, val label: String, val color: Color)

val ButterflyColorOptions = listOf(
    ButterflyColorOption("gold", "Gold", GoldAmber),
    ButterflyColorOption("violet", "Violet", Violet),
    ButterflyColorOption("rose", "Rose", Color(0xFFE86A92)),
    ButterflyColorOption("teal", "Teal", Color(0xFF3DAE8E)),
    ButterflyColorOption("sky", "Sky", SkyBlueDark)
)

fun butterflyColorFor(id: String): Color =
    ButterflyColorOptions.firstOrNull { it.id == id }?.color ?: GoldAmber
