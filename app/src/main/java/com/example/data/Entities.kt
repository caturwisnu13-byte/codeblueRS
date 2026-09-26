package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertStatus(val label: String, val badgeColorHex: Long) {
    DISPATCHED("Terkirim (Menunggu Respon)", 0xFFD32F2F),
    RESPONDING("Tim Dalam Perjalanan", 0xFF0288D1),
    ON_SCENE("Tim Tiba di Lokasi", 0xFF7B1FA2),
    RESUSCITATION("Resusitasi Berlangsung", 0xFFF57C00),
    RESOLVED("Selesai (ROSC / Stabil)", 0xFF2E7D32),
    CANCELLED("Dibatalkan", 0xFF757575)
}

enum class PatientCategory(val label: String) {
    ADULT("Dewasa"),
    PEDIATRIC("Anak-anak (Pediatrik)"),
    NEONATE("Bayi Baru Lahir (Neonatus)")
}

enum class RoleType(val title: String, val shortRole: String) {
    LEADER_DOCTOR("Dokter Penanggung Jawab Resusitasi (Team Leader)", "Dokter Jaga"),
    AIRWAY_NURSE("Perawat Airway, Intubasi & Bagging", "Perawat Airway"),
    CIRCULATION_NURSE("Perawat Kompresi Dada (CPR)", "Perawat Kompresi"),
    DRUG_NURSE("Perawat Obat, IV Line & Sirkulasi", "Perawat Obat"),
    DEFIB_EQUIPMENT("Petugas Alat AED / Defibrilator & Suction", "Petugas Defib"),
    SECURITY_PORTER("Petugas Sterilisasi Koridor & Transport", "Petugas Porter")
}

@Entity(tableName = "code_blue_alerts")
data class CodeBlueAlertEntity(
    @PrimaryKey val id: String,
    val building: String,
    val floor: Int,
    val roomName: String,
    val roomId: String = "",
    val bedNumber: String = "",
    val patientCategory: String = PatientCategory.ADULT.name,
    val conditionSummary: String = "Henti Jantung / Tidak Sadar",
    val needsDefib: Boolean = true,
    val cprStartedLocally: Boolean = true,
    val callerName: String = "Perawat Ruangan",
    val status: String = AlertStatus.DISPATCHED.name,
    val activatedAt: Long = System.currentTimeMillis(),
    val firstArrivalAt: Long? = null,
    val resolvedAt: Long? = null,
    val outcome: String? = null,
    val leadDoctor: String = "Dr. Farhan Sp.An",
    val totalShocks: Int = 0,
    val totalEpiDoses: Int = 0,
    val cprDurationSeconds: Int = 0,
    val isDrill: Boolean = false,
    val roomPosX: Float = 0.5f,
    val roomPosY: Float = 0.5f,
    val deactivatedBy: String? = null
)

@Entity(tableName = "incident_logs")
data class IncidentLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val authorName: String,
    val authorRole: String,
    val logType: String,
    val message: String
)

@Entity(tableName = "responders")
data class ResponderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val roleTitle: String,
    val phone: String,
    val currentBuilding: String,
    val currentFloor: Int,
    val state: String = "IDLE", // IDLE, RESPONDING, ON_SCENE
    val isOnDuty: Boolean = true, // ON-DUTY vs OFF-DUTY
    val isLeader: Boolean = false, // Team Leader / Dokter Penanggung Jawab
    val assignedAlertId: String? = null,
    val etaSeconds: Int = 0,
    val posX: Float = 0.2f,
    val posY: Float = 0.3f,
    val targetPosX: Float = 0.5f,
    val targetPosY: Float = 0.5f
)

@Entity(tableName = "hospital_rooms")
data class HospitalRoomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val building: String,
    val floor: Int,
    val defaultBed: String = "Bed 1",
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val isEmergencyPriority: Boolean = false,
    val isActive: Boolean = true
)
