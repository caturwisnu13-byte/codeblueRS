package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeBlueDao {

    @Query("SELECT * FROM code_blue_alerts ORDER BY activatedAt DESC")
    fun getAllAlerts(): Flow<List<CodeBlueAlertEntity>>

    @Query("SELECT * FROM code_blue_alerts WHERE status NOT IN ('RESOLVED', 'CANCELLED') ORDER BY activatedAt DESC")
    fun getActiveAlerts(): Flow<List<CodeBlueAlertEntity>>

    @Query("SELECT * FROM code_blue_alerts WHERE id = :id LIMIT 1")
    fun getAlertById(id: String): Flow<CodeBlueAlertEntity?>

    @Query("SELECT * FROM code_blue_alerts WHERE id = :id LIMIT 1")
    suspend fun getAlertByIdOnce(id: String): CodeBlueAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CodeBlueAlertEntity)

    @Update
    suspend fun updateAlert(alert: CodeBlueAlertEntity)

    // Logs
    @Query("SELECT * FROM incident_logs WHERE alertId = :alertId ORDER BY timestamp ASC")
    fun getLogsForAlert(alertId: String): Flow<List<IncidentLogEntity>>

    @Query("SELECT * FROM incident_logs WHERE alertId = :alertId ORDER BY timestamp ASC")
    suspend fun getLogsForAlertOnce(alertId: String): List<IncidentLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: IncidentLogEntity)

    // Responders
    @Query("SELECT * FROM responders ORDER BY role ASC")
    fun getAllResponders(): Flow<List<ResponderEntity>>

    @Query("SELECT * FROM responders WHERE id = :id LIMIT 1")
    suspend fun getResponderById(id: String): ResponderEntity?

    @Query("SELECT * FROM responders WHERE assignedAlertId = :alertId")
    fun getRespondersForAlert(alertId: String): Flow<List<ResponderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponders(responders: List<ResponderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponder(responder: ResponderEntity)

    @Update
    suspend fun updateResponder(responder: ResponderEntity)

    @Update
    suspend fun updateResponders(responders: List<ResponderEntity>)

    @Query("DELETE FROM responders WHERE id = :id")
    suspend fun deleteResponder(id: String)

    @Query("UPDATE responders SET state = 'IDLE', assignedAlertId = null, etaSeconds = 0 WHERE assignedAlertId = :alertId")
    suspend fun resetRespondersForAlert(alertId: String)

    // Hospital Rooms (Master Data Managed by Admin)
    @Query("SELECT * FROM hospital_rooms ORDER BY building ASC, floor ASC, name ASC")
    fun getAllRooms(): Flow<List<HospitalRoomEntity>>

    @Query("SELECT * FROM hospital_rooms WHERE id = :id LIMIT 1")
    suspend fun getRoomById(id: String): HospitalRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: HospitalRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<HospitalRoomEntity>)

    @Update
    suspend fun updateRoom(room: HospitalRoomEntity)

    @Query("DELETE FROM hospital_rooms WHERE id = :id")
    suspend fun deleteRoom(id: String)
}
