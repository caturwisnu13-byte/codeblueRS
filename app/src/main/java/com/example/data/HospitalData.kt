package com.example.data

data class HospitalRoom(
    val id: String,
    val name: String,
    val building: String,
    val floor: Int,
    val bedCount: Int,
    val mapX: Float,
    val mapY: Float,
    val defaultBed: String = "Bed 1"
) {
    val accessLink: String
        get() = "https://codeblue.rs/unit/$id"
}

data class HospitalBuilding(
    val id: String,
    val name: String,
    val totalFloors: Int,
    val defaultRooms: List<String>
)

object HospitalData {
    val buildings = listOf(
        "Gedung Bedah & Emergensi (GBE)",
        "Gedung Paviliun Teratai",
        "Gedung Rawat Inap Anggrek",
        "Gedung Flamboyan"
    )

    val rooms = listOf(
        HospitalRoom("RM-IGD-01", "IGD Resusitasi Bed 1", "Gedung Bedah & Emergensi (GBE)", 1, 1, 0.28f, 0.35f, "Bed 1"),
        HospitalRoom("RM-IGD-02", "IGD Resusitasi Bed 2", "Gedung Bedah & Emergensi (GBE)", 1, 1, 0.32f, 0.35f, "Bed 2"),
        HospitalRoom("RM-ICU-03", "ICU Dewasa Bed 3", "Gedung Bedah & Emergensi (GBE)", 2, 8, 0.72f, 0.28f, "Bed 3"),
        HospitalRoom("RM-ICU-05", "ICU Dewasa Bed 5", "Gedung Bedah & Emergensi (GBE)", 2, 8, 0.78f, 0.28f, "Bed 5"),
        HospitalRoom("RM-ICCU-02", "ICCU / Jantung Bed 2", "Gedung Bedah & Emergensi (GBE)", 2, 6, 0.65f, 0.38f, "Bed 2"),
        HospitalRoom("RM-OK-02", "Kamar Operasi OK 2", "Gedung Bedah & Emergensi (GBE)", 3, 1, 0.45f, 0.25f, "Meja Operasi"),
        HospitalRoom("RM-VK-01", "Kamar Bersalin / VK 1", "Gedung Paviliun Teratai", 2, 4, 0.25f, 0.65f, "Bed Partus 1"),
        HospitalRoom("RM-HD-08", "Ruang Hemodialisa Bed 8", "Gedung Paviliun Teratai", 1, 12, 0.75f, 0.70f, "Bed HD 8"),
        HospitalRoom("RM-TERATAI-104", "Bangsal Teratai Kamar 104", "Gedung Paviliun Teratai", 1, 4, 0.35f, 0.72f, "Bed 2"),
        HospitalRoom("RM-ANGGREK-201", "Bangsal Anggrek VIP 201", "Gedung Rawat Inap Anggrek", 2, 1, 0.50f, 0.50f, "Bed VIP"),
        HospitalRoom("RM-ANGGREK-208", "Bangsal Anggrek Kelas 1 Kamar 208", "Gedung Rawat Inap Anggrek", 2, 2, 0.60f, 0.52f, "Bed 1"),
        HospitalRoom("RM-FLAMBOYAN-305", "Bangsal Flamboyan Kamar 305", "Gedung Flamboyan", 3, 4, 0.30f, 0.80f, "Bed 3"),
        HospitalRoom("RM-POLI-01", "Poliklinik Jantung Terpadu", "Gedung Paviliun Teratai", 1, 2, 0.82f, 0.60f, "Meja Periksa 1"),
        HospitalRoom("RM-RAD-01", "Ruang Radiologi / CT-Scan", "Gedung Bedah & Emergensi (GBE)", 1, 2, 0.20f, 0.45f, "Gantry CT")
    )

    fun findRoomById(id: String): HospitalRoom {
        return rooms.find { it.id.equals(id.trim(), ignoreCase = true) }
            ?: rooms.find { it.accessLink.equals(id.trim(), ignoreCase = true) }
            ?: rooms.first()
    }

    fun getInitialResponders(): List<ResponderEntity> = listOf(
        ResponderEntity(
            id = "resp-001",
            name = "Dr. Farhan Syahputra, Sp.An",
            role = RoleType.LEADER_DOCTOR.name,
            roleTitle = RoleType.LEADER_DOCTOR.title,
            phone = "0812-3456-7891",
            currentBuilding = "Gedung Bedah & Emergensi (GBE)",
            currentFloor = 1,
            state = "IDLE",
            isOnDuty = true,
            isLeader = true,
            assignedAlertId = null,
            etaSeconds = 0,
            posX = 0.15f,
            posY = 0.20f,
            targetPosX = 0.15f,
            targetPosY = 0.20f
        ),
        ResponderEntity(
            id = "resp-002",
            name = "Ns. Siti Rahmawati, S.Kep",
            role = RoleType.AIRWAY_NURSE.name,
            roleTitle = RoleType.AIRWAY_NURSE.title,
            phone = "0812-9876-5432",
            currentBuilding = "Gedung Bedah & Emergensi (GBE)",
            currentFloor = 2,
            state = "IDLE",
            isOnDuty = true,
            assignedAlertId = null,
            etaSeconds = 0,
            posX = 0.85f,
            posY = 0.22f,
            targetPosX = 0.85f,
            targetPosY = 0.22f
        ),
        ResponderEntity(
            id = "resp-003",
            name = "Ns. Budi Santoso, S.Kep",
            role = RoleType.CIRCULATION_NURSE.name,
            roleTitle = RoleType.CIRCULATION_NURSE.title,
            phone = "0813-1122-3344",
            currentBuilding = "Gedung Paviliun Teratai",
            currentFloor = 1,
            state = "IDLE",
            isOnDuty = true,
            assignedAlertId = null,
            etaSeconds = 0,
            posX = 0.30f,
            posY = 0.60f,
            targetPosX = 0.30f,
            targetPosY = 0.60f
        ),
        ResponderEntity(
            id = "resp-004",
            name = "Apt. Dewi Lestari, S.Farm",
            role = RoleType.DRUG_NURSE.name,
            roleTitle = RoleType.DRUG_NURSE.title,
            phone = "0813-5566-7788",
            currentBuilding = "Gedung Paviliun Teratai",
            currentFloor = 1,
            state = "IDLE",
            isOnDuty = true,
            assignedAlertId = null,
            etaSeconds = 0,
            posX = 0.70f,
            posY = 0.65f,
            targetPosX = 0.70f,
            targetPosY = 0.65f
        ),
        ResponderEntity(
            id = "resp-005",
            name = "Pak Joko Prasetyo",
            role = RoleType.DEFIB_EQUIPMENT.name,
            roleTitle = RoleType.DEFIB_EQUIPMENT.title,
            phone = "0813-9988-7766",
            currentBuilding = "Gedung Bedah & Emergensi (GBE)",
            currentFloor = 1,
            state = "IDLE",
            isOnDuty = true,
            assignedAlertId = null,
            etaSeconds = 0,
            posX = 0.25f,
            posY = 0.30f,
            targetPosX = 0.25f,
            targetPosY = 0.30f
        ),
        ResponderEntity(
            id = "resp-006",
            name = "Hendra Wijaya",
            role = RoleType.SECURITY_PORTER.name,
            roleTitle = RoleType.SECURITY_PORTER.title,
            phone = "0812-4455-6677",
            currentBuilding = "Gedung Rawat Inap Anggrek",
            currentFloor = 1,
            state = "IDLE",
            isOnDuty = false, // One initially off-duty for demonstration of toggle
            assignedAlertId = null,
            etaSeconds = 0,
            posX = 0.50f,
            posY = 0.45f,
            targetPosX = 0.50f,
            targetPosY = 0.45f
        )
    )

    fun getSampleHistoricalAlerts(): List<CodeBlueAlertEntity> {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600 * 1000 * 3
        val yesterday = now - 3600 * 1000 * 26

        return listOf(
            CodeBlueAlertEntity(
                id = "CB-2026-092",
                building = "Gedung Rawat Inap Anggrek",
                floor = 2,
                roomName = "Bangsal Anggrek VIP 201",
                bedNumber = "Bed 1",
                patientCategory = PatientCategory.ADULT.name,
                conditionSummary = "Henti Napas Mendadak pasca operasi",
                needsDefib = true,
                cprStartedLocally = true,
                callerName = "Ns. Ratna (Anggrek)",
                status = AlertStatus.RESOLVED.name,
                activatedAt = oneHourAgo,
                firstArrivalAt = oneHourAgo + 105 * 1000, // 1m 45s arrival time!
                resolvedAt = oneHourAgo + 18 * 60 * 1000,
                outcome = "ROSC Tercapai (Return of Spontaneous Circulation), Pasien Ditransfer ke ICU",
                leadDoctor = "Dr. Farhan Syahputra, Sp.An",
                totalShocks = 2,
                totalEpiDoses = 2,
                cprDurationSeconds = 720,
                isDrill = false,
                roomPosX = 0.50f,
                roomPosY = 0.50f
            ),
            CodeBlueAlertEntity(
                id = "CB-2026-091",
                building = "Gedung Paviliun Teratai",
                floor = 1,
                roomName = "Ruang Hemodialisa Bed 8",
                bedNumber = "Bed 8",
                patientCategory = PatientCategory.ADULT.name,
                conditionSummary = "Kolaps saat sesi cuci darah, bradikardia berat",
                needsDefib = false,
                cprStartedLocally = true,
                callerName = "Ns. Tri (Hemodialisa)",
                status = AlertStatus.RESOLVED.name,
                activatedAt = yesterday,
                firstArrivalAt = yesterday + 90 * 1000, // 1m 30s
                resolvedAt = yesterday + 12 * 60 * 1000,
                outcome = "Irama Sinus Kembali, Hemodinamik Stabil",
                leadDoctor = "Dr. Farhan Syahputra, Sp.An",
                totalShocks = 0,
                totalEpiDoses = 1,
                cprDurationSeconds = 360,
                isDrill = false,
                roomPosX = 0.75f,
                roomPosY = 0.70f
            )
        )
    }

    val resuscitationMedications = listOf(
        "Epinefrin / Adrenalin 1 mg IV/IO (Setiap 3-5 menit)",
        "Amiodarone 300 mg bolus IV/IO (Dosis 1 untuk VF/pVT)",
        "Amiodarone 150 mg bolus IV/IO (Dosis ke-2)",
        "Sulfas Atropin 1 mg IV (Bradikardia simtomatik)",
        "Natrium Bikarbonat 50 mEq IV (Asidosis metabolik)",
        "Kalsium Glukonat 10% 10 ml IV (Hiperkalemia/Hipokalsemia)",
        "Magnesium Sulfat 2 gram IV (Torsades de Pointes)",
        "Dextrose 40% 25-50 ml IV (Hipoglikemia berat)"
    )

    val cardiacRhythms = listOf(
        "Ventricular Fibrillation (VF) - [Shockable]",
        "Pulseless Ventricular Tachycardia (pVT) - [Shockable]",
        "Asystole (Garis Datar) - [Non-Shockable]",
        "PEA (Pulseless Electrical Activity) - [Non-Shockable]",
        "Sinus Rhythm / ROSC Tercapai"
    )
}
