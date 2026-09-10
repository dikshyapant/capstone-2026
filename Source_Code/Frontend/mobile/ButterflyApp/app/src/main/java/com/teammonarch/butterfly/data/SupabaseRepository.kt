package com.teammonarch.butterfly.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A dose is "on-time" within this many minutes of its scheduled time, either side (team-approved 1-hour window). */
private const val ON_TIME_GRACE_MINUTES = 30L

private val TIME_FORMATS = listOf(
    DateTimeFormatter.ofPattern("h:mm a", Locale.US),
    DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
    DateTimeFormatter.ofPattern("H:mm", Locale.US)
)

private fun parseScheduledTime(text: String): LocalTime? {
    val cleaned = text.trim().uppercase(Locale.US)
    for (formatter in TIME_FORMATS) {
        try {
            return LocalTime.parse(cleaned, formatter)
        } catch (e: Exception) {
            // try the next format
        }
    }
    return null
}

object SupabaseRepository {

    private val client get() = SupabaseClient.client

    suspend fun signUp(email: String, password: String, fullName: String, role: String): Result<ProfileRow> {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Sign up succeeded but no session was returned."))

            val profile = ProfileRow(id = userId, fullName = fullName, email = email, role = role)
            client.postgrest["profiles"].insert(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<ProfileRow> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Login succeeded but no session was returned."))

            val profile = client.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<ProfileRow>()

            if (!profile.isActive) {
                client.auth.signOut()
                return Result.failure(IllegalStateException("This account has been deactivated."))
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            client.auth.signOut()
        } finally {
            AppSession.clear()
        }
    }

    suspend fun updateProfile(userId: String, fullName: String, email: String, phone: String, dateOfBirth: String): Result<Unit> {
        return try {
            client.postgrest["profiles"].update(
                {
                    set("full_name", fullName)
                    set("email", email)
                    set("phone", phone)
                    set("date_of_birth", dateOfBirth)
                }
            ) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Changes the account's login password (Supabase Auth), independent of the profiles table. */
    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            client.auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean): Result<Unit> {
        return try {
            client.postgrest["profiles"].update(
                { set("notifications_enabled", enabled) }
            ) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Soft-deletes the account: flags the profile inactive (so future logins are refused,
     * see [signIn]) and signs the current session out. A real hard-delete of the auth user
     * would need an admin/service-role key, which must never live in a mobile client.
     */
    suspend fun deactivateAccount(userId: String): Result<Unit> {
        return try {
            client.postgrest["profiles"].update(
                { set("is_active", false) }
            ) {
                filter { eq("id", userId) }
            }
            client.auth.signOut()
            AppSession.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReduceMotion(userId: String, reduceMotion: Boolean): Result<Unit> {
        return try {
            client.postgrest["profiles"].update(
                { set("reduce_motion", reduceMotion) }
            ) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateButterflyColor(userId: String, color: String): Result<Unit> {
        return try {
            client.postgrest["profiles"].update(
                { set("butterfly_color", color) }
            ) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMedications(patientId: String): Result<List<MedicationRow>> {
        return try {
            val meds = client.postgrest["medications"]
                .select { filter { eq("patient_id", patientId) } }
                .decodeList<MedicationRow>()
            Result.success(meds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMedication(patientId: String, name: String, scheduledTime: String): Result<Unit> {
        return try {
            client.postgrest["medications"].insert(
                MedicationInsert(patientId = patientId, name = name, scheduledTime = scheduledTime)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs a dose as taken now. A dose counts as on-time when it's logged within
     * [ON_TIME_GRACE_MINUTES] of the medication's scheduled time, either side (team's
     * approved 1-hour window). Only on-time doses extend the consecutive-day streak
     * and earn the halo; Double Monarch Day -- $4 instead of $2 -- triggers on every
     * second consecutive on-time day (CR-02). A late dose still earns $1 but doesn't
     * touch the streak, so it correctly breaks the chain if the next day is on-time.
     * If the scheduled time can't be parsed, the dose is treated as on-time by default
     * (per the team's "resolve ambiguity in the patient's favor" guidance).
     */
    suspend fun markMedicationTaken(patientId: String, medicationId: String, scheduledTimeText: String): Result<MarkTakenResult> {
        return try {
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            val scheduledTime = parseScheduledTime(scheduledTimeText)
            val isOnTime = scheduledTime?.let {
                Duration.between(it.atDate(today), now).abs().toMinutes() <= ON_TIME_GRACE_MINUTES
            } ?: true

            val current = client.postgrest["profiles"]
                .select { filter { eq("id", patientId) } }
                .decodeSingle<ProfileRow>()

            if (isOnTime) {
                val lastLogDate = current.lastLogDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                val newStreak = when (lastLogDate) {
                    today -> current.currentStreak
                    today.minusDays(1) -> current.currentStreak + 1
                    else -> 1
                }
                val isDoubleMonarchDay = newStreak > 0 && newStreak % 2 == 0
                val bbAwarded = if (isDoubleMonarchDay) 4 else 2
                val newBalance = current.currentBb + bbAwarded

                client.postgrest["medication_logs"].insert(
                    MedicationLogInsert(
                        medicationId = medicationId,
                        patientId = patientId,
                        status = "taken_on_time",
                        scheduledFor = today.toString()
                    )
                )
                client.postgrest["profiles"].update(
                    {
                        set("current_bb", newBalance)
                        set("current_streak", newStreak)
                        set("last_log_date", today.toString())
                    }
                ) {
                    filter { eq("id", patientId) }
                }

                Result.success(MarkTakenResult(newBalance, newStreak, isDoubleMonarchDay, bbAwarded, wasOnTime = true))
            } else {
                val bbAwarded = 1
                val newBalance = current.currentBb + bbAwarded

                client.postgrest["medication_logs"].insert(
                    MedicationLogInsert(
                        medicationId = medicationId,
                        patientId = patientId,
                        status = "taken_late",
                        scheduledFor = today.toString()
                    )
                )
                client.postgrest["profiles"].update(
                    { set("current_bb", newBalance) }
                ) {
                    filter { eq("id", patientId) }
                }

                Result.success(MarkTakenResult(newBalance, current.currentStreak, isDoubleMonarchDay = false, bbAwarded = bbAwarded, wasOnTime = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Dates (yyyy-MM-dd strings) this patient has an on-time dose logged, for the adherence calendar. */
    suspend fun fetchAdherenceDates(patientId: String): Result<List<String>> {
        return try {
            val logs = client.postgrest["medication_logs"]
                .select(columns = Columns.list("scheduled_for")) {
                    filter {
                        eq("patient_id", patientId)
                        eq("status", "taken_on_time")
                    }
                }
                .decodeList<ScheduledForRow>()
            Result.success(logs.mapNotNull { it.scheduledFor }.distinct())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllPatients(): Result<List<ProfileRow>> {
        return try {
            val patients = client.postgrest["profiles"]
                .select { filter { eq("role", "patient") } }
                .decodeList<ProfileRow>()
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
