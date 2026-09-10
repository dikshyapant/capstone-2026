package com.teammonarch.butterfly.model

enum class MedicationStatus { TAKEN_ON_TIME, TAKEN_LATE, MISSED, PENDING }

data class Medication(
    val id: String,
    val name: String,
    val scheduledTime: String,
    val status: MedicationStatus
)

enum class GameStage(val label: String, val emoji: String, val minBB: Int) {
    CHRYSALIS("Chrysalis", "🥚", 0),
    CATERPILLAR("Caterpillar", "🐛", 50),
    BUTTERFLY("Butterfly", "🦋", 150);

    companion object {
        fun forBB(bb: Int): GameStage = when {
            bb >= BUTTERFLY.minBB -> BUTTERFLY
            bb >= CATERPILLAR.minBB -> CATERPILLAR
            else -> CHRYSALIS
        }
    }
}

data class PatientSummary(
    val id: String,
    val name: String,
    val adherencePercent: Int,
    val hasAlert: Boolean,
    val lastMissedDate: String? = null
)
