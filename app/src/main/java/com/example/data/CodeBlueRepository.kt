package com.example.data

import kotlinx.coroutines.flow.Flow

class CodeBlueRepository(private val dao: CodeBlueDao) {

    val allAlerts: Flow<List<CodeBlueAlertEntity>> = dao.getAllAlerts()
    val activeAlerts: Flow<List<CodeBlueAlertEntity>> = dao.getActiveAlerts()
    val responders: Flow<List<ResponderEntity>> = dao.getAllResponders()
    val allRooms: Flow<List<HospitalRoomEntity>> = dao.getAllRooms()

    fun getAlertById(id: String): Flow<CodeBlueAlertEntity?> = dao.getAlertById(id)

    suspend fun getAlertByIdOnce(id: String): CodeBlueAlertEntity? = dao.getAlertByIdOnce(id)

    fun getLogsForAlert(alertId: String): Flow<List<IncidentLogEntity>> = dao.getLogsForAlert(alertId)

    suspend fun getLogsForAlertOnce(alertId: String): List<IncidentLogEntity> = dao.getLogsForAlertOnce(alertId)

    fun getRespondersForAlert(alertId: String): Flow<List<ResponderEntity>> = dao.getRespondersForAlert(alertId)

    suspend fun insertAlert(alert: CodeBlueAlertEntity) = dao.insertAlert(alert)

    suspend fun updateAlert(alert: CodeBlueAlertEntity) = dao.updateAlert(alert)

    suspend fun insertLog(log: IncidentLogEntity) = dao.insertLog(log)

    suspend fun updateResponder(responder: ResponderEntity) = dao.updateResponder(responder)

    suspend fun insertResponder(responder: ResponderEntity) = dao.insertResponder(responder)

    suspend fun deleteResponder(id: String) = dao.deleteResponder(id)

    suspend fun updateResponders(responders: List<ResponderEntity>) = dao.updateResponders(responders)

    suspend fun resetRespondersForAlert(alertId: String) = dao.resetRespondersForAlert(alertId)

    // Master Room Management
    suspend fun getRoomById(id: String) = dao.getRoomById(id)

    suspend fun insertRoom(room: HospitalRoomEntity) = dao.insertRoom(room)

    suspend fun updateRoom(room: HospitalRoomEntity) = dao.updateRoom(room)

    suspend fun deleteRoom(id: String) = dao.deleteRoom(id)

    suspend fun initializeDefaultDataIfEmpty() {
        val existingAlerts = dao.getAlertByIdOnce("CB-2026-092")
        if (existingAlerts == null) {
            val samples = HospitalData.getSampleHistoricalAlerts()
            samples.forEach { dao.insertAlert(it) }

            // Add sample logs
            dao.insertLog(
                IncidentLogEntity(
                    alertId = "CB-2026-092",
                    timestamp = samples[0].activatedAt,
                    authorName = "Ns. Ratna",
                    authorRole = "Perawat Ruangan",
                    logType = "ALERT_TRIGGERED",
                    message = "Code Blue diaktifkan di Bangsal Anggrek VIP 201 Bed 1. Pasien henti napas."
                )
            )
            dao.insertLog(
                IncidentLogEntity(
                    alertId = "CB-2026-092",
                    timestamp = samples[0].firstArrivalAt ?: (samples[0].activatedAt + 105000),
                    authorName = "Dr. Farhan Syahputra, Sp.An",
                    authorRole = "Team Leader",
                    logType = "STATUS_UPDATE",
                    message = "Tim Code Blue tiba di lokasi dalam 1 menit 45 detik. Resusitasi CPR dimulai."
                )
            )
            dao.insertLog(
                IncidentLogEntity(
                    alertId = "CB-2026-092",
                    timestamp = samples[0].activatedAt + 300000,
                    authorName = "Apt. Dewi Lestari",
                    authorRole = "Farmasi",
                    logType = "DRUG_GIVEN",
                    message = "Epinefrin 1 mg IV bolus diberikan."
                )
            )
        }

        // Check if responders exist
        val firstResp = dao.getResponderById("resp-001")
        if (firstResp == null) {
            dao.insertResponders(HospitalData.getInitialResponders())
        }

        // Check if rooms exist
        val firstRoom = dao.getRoomById("RM-IGD-01")
        if (firstRoom == null) {
            val defaultRooms = HospitalData.rooms.map { r ->
                HospitalRoomEntity(
                    id = r.id,
                    name = r.name,
                    building = r.building,
                    floor = r.floor,
                    defaultBed = r.defaultBed,
                    posX = r.mapX,
                    posY = r.mapY,
                    isEmergencyPriority = r.name.contains("ICU", true) || r.name.contains("IGD", true) || r.name.contains("OK", true),
                    isActive = true
                )
            }
            dao.insertRooms(defaultRooms)
        }
    }
}
