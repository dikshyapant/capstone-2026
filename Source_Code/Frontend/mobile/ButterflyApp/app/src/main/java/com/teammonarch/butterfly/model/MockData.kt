package com.teammonarch.butterfly.model

object MockData {
    const val patientName = "Taylor"
    const val patientEmail = "lupus.patient@email.com"
    const val clinicianName = "Dr. Emma Davis"
    const val clinicianEmail = "dr.e.davis@email.com"

    val patientMedications = listOf(
        Medication("m1", "Hydroxychloroquine", "8:00 AM", MedicationStatus.TAKEN_ON_TIME),
        Medication("m2", "Prednisone", "1:00 PM", MedicationStatus.TAKEN_LATE),
        Medication("m3", "Methotrexate", "8:00 PM", MedicationStatus.PENDING)
    )

    const val currentBB = 62

    val patients = listOf(
        PatientSummary("p1", "Maya Chen", 92, hasAlert = false),
        PatientSummary("p2", "Taylor Brooks", 54, hasAlert = true, lastMissedDate = "Aug 29"),
        PatientSummary("p3", "Jordan Lee", 78, hasAlert = false),
        PatientSummary("p4", "Sam Rivera", 40, hasAlert = true, lastMissedDate = "Aug 30")
    )
}
