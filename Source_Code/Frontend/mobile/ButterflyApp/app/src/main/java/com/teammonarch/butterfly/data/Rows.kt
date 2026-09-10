package com.teammonarch.butterfly.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileRow(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("current_bb") val currentBb: Int = 0,
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("last_log_date") val lastLogDate: String? = null,
    @SerialName("reduce_motion") val reduceMotion: Boolean = false,
    @SerialName("butterfly_color") val butterflyColor: String = "gold",
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class MedicationRow(
    val id: String? = null,
    @SerialName("patient_id") val patientId: String,
    val name: String,
    @SerialName("scheduled_time") val scheduledTime: String
)

@Serializable
data class MedicationLogRow(
    val id: String? = null,
    @SerialName("medication_id") val medicationId: String,
    @SerialName("patient_id") val patientId: String,
    val status: String,
    @SerialName("scheduled_for") val scheduledFor: String? = null
)

@Serializable
data class MedicationInsert(
    @SerialName("patient_id") val patientId: String,
    val name: String,
    @SerialName("scheduled_time") val scheduledTime: String
)

@Serializable
data class MedicationLogInsert(
    @SerialName("medication_id") val medicationId: String,
    @SerialName("patient_id") val patientId: String,
    val status: String,
    @SerialName("scheduled_for") val scheduledFor: String
)

@Serializable
data class ScheduledForRow(
    @SerialName("scheduled_for") val scheduledFor: String? = null
)

/** Result of logging a dose: the patient's updated stats, for the UI to reflect immediately. */
data class MarkTakenResult(
    val newBalance: Int,
    val newStreak: Int,
    val isDoubleMonarchDay: Boolean,
    val bbAwarded: Int,
    val wasOnTime: Boolean
)
