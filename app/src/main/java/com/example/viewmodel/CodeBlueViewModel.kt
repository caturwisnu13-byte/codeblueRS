package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlertStatus
import com.example.data.AppDatabase
import com.example.data.CodeBlueAlertEntity
import com.example.data.CodeBlueRepository
import com.example.data.HospitalData
import com.example.data.HospitalRoom
import com.example.data.HospitalRoomEntity
import com.example.data.IncidentLogEntity
import com.example.data.PatientCategory
import com.example.data.ResponderEntity
import com.example.data.RoleType
import com.example.notification.EmergencyAlertManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

enum class AppRole(val label: String, val badge: String) {
    ROOM_REQUESTER("Unit Ruangan", "Pemanggil Darurat"),
    CODE_BLUE_TEAM("Tim Code Blue", "Responden Medis"),
    SUPERVISOR("Pengawas / Komando", "Command Center & KPI"),
    ADMIN("Portal Admin", "Master Data Ruangan & Tim")
}

data class HeadsUpAlert(
    val alert: CodeBlueAlertEntity,
    val showBanner: Boolean = true
)

class CodeBlueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CodeBlueRepository = CodeBlueRepository(AppDatabase.getDatabase(application).codeBlueDao())
    val alertManager = EmergencyAlertManager(application)
    private val sharedPrefs = application.getSharedPreferences("codeblue_prefs", Context.MODE_PRIVATE)

    // Current active role (Saved on device so room, team, and supervisor apps stay in their respective separated mode)
    private val _currentRole = MutableStateFlow(
        try {
            val savedRole = sharedPrefs.getString("app_assigned_mode", AppRole.ROOM_REQUESTER.name)
            AppRole.valueOf(savedRole ?: AppRole.ROOM_REQUESTER.name)
        } catch (_: Exception) {
            AppRole.ROOM_REQUESTER
        }
    )
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Bound Room Identity for the Device (Ruangan tidak perlu memilih dropdown)
    private val _boundRoomId = MutableStateFlow(
        sharedPrefs.getString("bound_room_id", "RM-IGD-01") ?: "RM-IGD-01"
    )
    val boundRoomId: StateFlow<String> = _boundRoomId.asStateFlow()

    // Master hospital rooms loaded from Room DB
    val rooms: StateFlow<List<HospitalRoomEntity>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val boundRoom: StateFlow<HospitalRoom> = combine(_boundRoomId, rooms) { id, roomEntities ->
        val entity = roomEntities.find { it.id.equals(id, ignoreCase = true) }
        if (entity != null) {
            HospitalRoom(
                id = entity.id,
                name = entity.name,
                building = entity.building,
                floor = entity.floor,
                bedCount = 1,
                mapX = entity.posX,
                mapY = entity.posY,
                defaultBed = entity.defaultBed
            )
        } else {
            HospitalData.findRoomById(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HospitalData.rooms.first())

    // Selected active responder profile (when role is CODE_BLUE_TEAM)
    private val _selectedResponderId = MutableStateFlow("resp-001")
    val selectedResponderId: StateFlow<String> = _selectedResponderId.asStateFlow()

    // Heads-up push notification banner simulation
    private val _headsUpAlert = MutableStateFlow<HeadsUpAlert?>(null)
    val headsUpAlert: StateFlow<HeadsUpAlert?> = _headsUpAlert.asStateFlow()

    // Selected active alert ID for details / resuscitation
    private val _selectedAlertId = MutableStateFlow<String?>(null)
    val selectedAlertId: StateFlow<String?> = _selectedAlertId.asStateFlow()

    // CPR & Resuscitation State
    private val _isCprRunning = MutableStateFlow(false)
    val isCprRunning: StateFlow<Boolean> = _isCprRunning.asStateFlow()

    private val _cprTotalSeconds = MutableStateFlow(0)
    val cprTotalSeconds: StateFlow<Int> = _cprTotalSeconds.asStateFlow()

    private val _cprCycleSecondsRemaining = MutableStateFlow(120) // 2-min cycle
    val cprCycleSecondsRemaining: StateFlow<Int> = _cprCycleSecondsRemaining.asStateFlow()

    private val _cprCycleNumber = MutableStateFlow(1)
    val cprCycleNumber: StateFlow<Int> = _cprCycleNumber.asStateFlow()

    private val _isMetronomeSoundOn = MutableStateFlow(true)
    val isMetronomeSoundOn: StateFlow<Boolean> = _isMetronomeSoundOn.asStateFlow()

    private val _epinephrineTimerRemaining = MutableStateFlow(240) // 4-minute timer
    val epinephrineTimerRemaining: StateFlow<Int> = _epinephrineTimerRemaining.asStateFlow()

    private val _selectedCardiacRhythm = MutableStateFlow(HospitalData.cardiacRhythms[0])
    val selectedCardiacRhythm: StateFlow<String> = _selectedCardiacRhythm.asStateFlow()

    // Metronome pulse trigger for visual animation
    private val _metronomeTick = MutableStateFlow(0L)
    val metronomeTick: StateFlow<Long> = _metronomeTick.asStateFlow()

    // Floor filter for Supervisor hospital map
    private val _mapSelectedFloor = MutableStateFlow(1)
    val mapSelectedFloor: StateFlow<Int> = _mapSelectedFloor.asStateFlow()

    // Siren playing indicator
    private val _isSirenActive = MutableStateFlow(false)
    val isSirenActive: StateFlow<Boolean> = _isSirenActive.asStateFlow()

    // Real-time second-by-second ticker for stopwatch and countdown
    private val _currentTimestamp = MutableStateFlow(System.currentTimeMillis())
    val currentTimestamp: StateFlow<Long> = _currentTimestamp.asStateFlow()

    private var simulationLoopJob: Job? = null
    private var cprTimerJob: Job? = null

    val allAlerts: StateFlow<List<CodeBlueAlertEntity>>
    val activeAlerts: StateFlow<List<CodeBlueAlertEntity>>
    val responders: StateFlow<List<ResponderEntity>>

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentAlert: StateFlow<CodeBlueAlertEntity?>
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentAlertLogs: StateFlow<List<IncidentLogEntity>>

    init {
        allAlerts = repository.allAlerts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeAlerts = repository.activeAlerts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        responders = repository.responders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        currentAlert = _selectedAlertId.flatMapLatest { id ->
            if (id != null) {
                repository.getAlertById(id)
            } else {
                activeAlerts.flatMapLatest { list ->
                    flowOf(list.firstOrNull())
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        currentAlertLogs = currentAlert.flatMapLatest { alert ->
            if (alert != null) {
                repository.getLogsForAlert(alert.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }

        startSimulationTicker()
    }

    fun switchRole(role: AppRole) {
        _currentRole.value = role
        // Only persist operational roles (not temporary admin session)
        if (role != AppRole.ADMIN) {
            sharedPrefs.edit().putString("app_assigned_mode", role.name).apply()
        }
    }

    fun verifyAdminPin(pin: String): Boolean {
        val storedPin = sharedPrefs.getString("admin_master_pin", "1199") ?: "1199"
        return pin.trim() == storedPin.trim()
    }

    fun updateAdminPin(newPin: String) {
        if (newPin.length >= 4) {
            sharedPrefs.edit().putString("admin_master_pin", newPin.trim()).apply()
        }
    }

    fun addRoom(room: HospitalRoomEntity) {
        viewModelScope.launch {
            repository.insertRoom(room)
        }
    }

    fun updateRoom(room: HospitalRoomEntity) {
        viewModelScope.launch {
            repository.updateRoom(room)
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            repository.deleteRoom(roomId)
        }
    }

    fun addResponder(responder: ResponderEntity) {
        viewModelScope.launch {
            repository.insertResponder(responder)
        }
    }

    fun updateResponder(responder: ResponderEntity) {
        viewModelScope.launch {
            repository.updateResponder(responder)
        }
    }

    fun deleteResponder(responderId: String) {
        viewModelScope.launch {
            repository.deleteResponder(responderId)
        }
    }

    fun toggleResponderLeader(responderId: String) {
        viewModelScope.launch {
            val list = responders.value
            val target = list.find { it.id == responderId } ?: return@launch
            val updated = target.copy(isLeader = !target.isLeader)
            repository.updateResponder(updated)
        }
    }

    suspend fun getLogsForAlertOnce(alertId: String): List<IncidentLogEntity> {
        return repository.getLogsForAlertOnce(alertId)
    }

    fun selectResponder(id: String) {
        _selectedResponderId.value = id
    }

    fun selectAlert(alertId: String?) {
        _selectedAlertId.value = alertId
    }

    fun selectFloor(floor: Int) {
        _mapSelectedFloor.value = floor
    }

    // Bind this device to a specific hospital room via Room Account Link / Token
    fun bindDeviceToRoom(roomId: String) {
        val room = HospitalData.findRoomById(roomId)
        _boundRoomId.value = room.id
        sharedPrefs.edit().putString("bound_room_id", room.id).apply()
    }

    fun bindDeviceToLink(linkOrToken: String) {
        val trimmed = linkOrToken.trim()
        val roomId = if (trimmed.contains("/unit/")) {
            trimmed.substringAfterLast("/unit/").trim()
        } else if (trimmed.contains("/room/")) {
            trimmed.substringAfterLast("/room/").trim()
        } else {
            trimmed
        }
        bindDeviceToRoom(roomId)
    }

    // Toggle duty status for a responder (Bagi anggota yang tidak berjaga bisa menonaktifkan aplikasi)
    fun toggleResponderDuty(responderId: String) {
        viewModelScope.launch {
            val resp = responders.value.find { it.id == responderId } ?: return@launch
            val newDutyStatus = !resp.isOnDuty
            val updated = resp.copy(
                isOnDuty = newDutyStatus,
                state = if (!newDutyStatus) "IDLE" else resp.state,
                assignedAlertId = if (!newDutyStatus) null else resp.assignedAlertId
            )
            repository.updateResponder(updated)

            // If this device was selected as this responder, and is toggled off-duty, stop alarm!
            if (selectedResponderId.value == responderId && !newDutyStatus) {
                stopSirenSound()
            }
        }
    }

    // Determine if the alarm can be silenced manually by the current user
    // RULE: Tim Code Blue yang sedang berjaga (ON-DUTY) TIDAK BISA menyenyapkan alarm ketika aktivasi sedang berlangsung
    fun canCurrentResponderSilenceAlarm(): Boolean {
        if (_currentRole.value != AppRole.CODE_BLUE_TEAM) {
            return true // Ruangan & Supervisor can silence audio if needed
        }
        val currentResp = responders.value.find { it.id == _selectedResponderId.value }
        val hasActiveCall = activeAlerts.value.isNotEmpty()
        // If on-duty and not yet arrived at scene, alarm CANNOT be silenced
        return !(currentResp != null && currentResp.isOnDuty && hasActiveCall && currentResp.state != "ON_SCENE")
    }

    fun dismissHeadsUpBanner() {
        _headsUpAlert.value = null
        if (canCurrentResponderSilenceAlarm()) {
            stopSirenSound()
        }
    }

    fun stopSirenSound() {
        alertManager.stopSiren()
        _isSirenActive.value = false
    }

    // 1-Tap Trigger Code Blue from Bound Room (Ruangan tidak perlu memilih dropdown)
    fun triggerCodeBlueFromBoundRoom(
        patientCategory: PatientCategory = PatientCategory.ADULT,
        conditionSummary: String = "Henti Jantung / Tidak Sadar",
        needsDefib: Boolean = true,
        cprStartedLocally: Boolean = true,
        bedNumber: String = ""
    ) {
        val room = boundRoom.value
        val actualBed = bedNumber.ifBlank { room.defaultBed }
        triggerCodeBlue(
            room = room,
            bedNumber = actualBed,
            patientCategory = patientCategory,
            conditionSummary = conditionSummary,
            needsDefib = needsDefib,
            cprStartedLocally = cprStartedLocally,
            callerName = "Staf ${room.name}",
            isDrill = false
        )
    }

    fun triggerCodeBlue(
        room: HospitalRoom,
        bedNumber: String,
        patientCategory: PatientCategory,
        conditionSummary: String,
        needsDefib: Boolean,
        cprStartedLocally: Boolean,
        callerName: String,
        isDrill: Boolean
    ) {
        viewModelScope.launch {
            val count = allAlerts.value.size + 1
            val alertId = "CB-2026-" + String.format("%03d", count)
            val newAlert = CodeBlueAlertEntity(
                id = alertId,
                building = room.building,
                floor = room.floor,
                roomName = room.name,
                roomId = room.id,
                bedNumber = bedNumber.ifBlank { "Bed 1" },
                patientCategory = patientCategory.name,
                conditionSummary = conditionSummary.ifBlank { "Henti Jantung / Tidak Sadar" },
                needsDefib = needsDefib,
                cprStartedLocally = cprStartedLocally,
                callerName = callerName.ifBlank { "Perawat Ruangan" },
                status = AlertStatus.DISPATCHED.name,
                activatedAt = System.currentTimeMillis(),
                isDrill = isDrill,
                roomPosX = room.mapX,
                roomPosY = room.mapY
            )

            repository.insertAlert(newAlert)
            _selectedAlertId.value = alertId
            _mapSelectedFloor.value = room.floor

            // Initial incident log
            val drillLabel = if (isDrill) "[SIMULASI DRILL] " else ""
            repository.insertLog(
                IncidentLogEntity(
                    alertId = alertId,
                    authorName = callerName.ifBlank { "Perawat Ruangan" },
                    authorRole = "Pelapor Ruangan",
                    logType = "ALERT_TRIGGERED",
                    message = "${drillLabel}Aktivasi CODE BLUE di ${room.name} (${room.building}, Lt. ${room.floor}). Pasien ${patientCategory.label}. Kondisi: ${newAlert.conditionSummary}. Kebutuhan Defibrilator: ${if (needsDefib) "YA" else "TIDAK"}. Target Respon: < 5 Menit."
                )
            )

            // Trigger continuous siren sound and vibration (ALARM NON-STOP)
            alertManager.playSiren(continuous = true)
            _isSirenActive.value = true

            // Send instant system push notification
            alertManager.sendInstantPushNotification(
                alertId = alertId,
                roomName = room.name,
                building = room.building,
                floor = room.floor,
                patientCategory = patientCategory.label,
                needsDefib = needsDefib
            )

            // Show Heads-up banner in-app
            _headsUpAlert.value = HeadsUpAlert(alert = newAlert, showBanner = true)

            // Dispatch all ON-DUTY responders towards target
            dispatchRespondersToAlert(newAlert)
        }
    }

    private suspend fun dispatchRespondersToAlert(alert: CodeBlueAlertEntity) {
        val currentResponders = responders.value
        val updated = currentResponders.map { resp ->
            if (resp.isOnDuty) {
                val dist = hypot(alert.roomPosX - resp.posX, alert.roomPosY - resp.posY)
                val floorDiff = kotlin.math.abs(alert.floor - resp.currentFloor)
                val etaSecs = max(30, (dist * 150 + floorDiff * 25).toInt())
                resp.copy(
                    state = "RESPONDING",
                    assignedAlertId = alert.id,
                    targetPosX = alert.roomPosX,
                    targetPosY = alert.roomPosY,
                    etaSeconds = etaSecs
                )
            } else {
                resp // Off-duty responders are not dispatched
            }
        }
        repository.updateResponders(updated)

        // Update alert status to RESPONDING
        val updatedAlert = alert.copy(status = AlertStatus.RESPONDING.name)
        repository.updateAlert(updatedAlert)

        val onDutyCount = updated.count { it.isOnDuty }
        repository.insertLog(
            IncidentLogEntity(
                alertId = alert.id,
                authorName = "Sistem Komando Otomatis",
                authorRole = "Hospital Dispatch",
                logType = "STATUS_UPDATE",
                message = "Notifikasi darurat dan alarm sirine diaktifkan ke $onDutyCount anggota Tim Code Blue yang sedang ON-DUTY. Stopwatch respon time aktif."
            )
        )
    }

    fun responderAcceptDispatch(responderId: String) {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            val resp = responders.value.find { it.id == responderId } ?: return@launch
            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = resp.name,
                    authorRole = resp.roleTitle,
                    logType = "STATUS_UPDATE",
                    message = "${resp.name} (${resp.roleTitle}) mengonfirmasi panggilan darurat dan sedang menuju ke lokasi. ETA: ${resp.etaSeconds} detik."
                )
            )
        }
    }

    // Either Room user or Responder can mark that team has arrived at the location
    // This stops the alarm and records exact Response Time (< 5 minutes target)
    fun markArrivalAndDeactivate(alertId: String, deactivatedBy: String = "Staf Ruangan") {
        viewModelScope.launch {
            val alert = repository.getAlertByIdOnce(alertId) ?: return@launch
            val now = System.currentTimeMillis()
            val firstArrival = alert.firstArrivalAt ?: now
            val responseSecs = ((firstArrival - alert.activatedAt) / 1000).toInt()
            val isUnder5Min = responseSecs <= 300

            val updatedAlert = alert.copy(
                status = AlertStatus.ON_SCENE.name,
                firstArrivalAt = firstArrival,
                deactivatedBy = deactivatedBy
            )
            repository.updateAlert(updatedAlert)

            // Stop the non-stop emergency siren!
            stopSirenSound()

            // Update all responding responders to ON_SCENE
            val currentResps = responders.value
            val updatedResps = currentResps.map { resp ->
                if (resp.assignedAlertId == alert.id) {
                    resp.copy(
                        state = "ON_SCENE",
                        posX = alert.roomPosX,
                        posY = alert.roomPosY,
                        etaSeconds = 0,
                        currentFloor = alert.floor
                    )
                } else {
                    resp
                }
            }
            repository.updateResponders(updatedResps)

            val mins = responseSecs / 60
            val secs = responseSecs % 60
            val slaText = if (isUnder5Min) "✅ MEMENUHI TARGET (< 5 Menit)" else "⚠️ MELEBIHI TARGET (> 5 Menit)"

            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = deactivatedBy,
                    authorRole = "Aktivasi Dinonaktifkan",
                    logType = "ARRIVAL_RECORDED",
                    message = "Tim Code Blue telah TIBA DI LOKASI! Alarm darurat dinonaktifkan oleh $deactivatedBy. Waktu respon: ${mins}m ${secs}d. Evaluasi Mutu: $slaText."
                )
            )

            // Start resuscitation CPR cycle automatically
            if (!_isCprRunning.value) {
                startCpr()
            }
        }
    }

    fun markResponderArrived(responderId: String) {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            val resp = responders.value.find { it.id == responderId } ?: return@launch
            markArrivalAndDeactivate(alert.id, "${resp.name} (${resp.roleTitle})")
        }
    }

    fun startCpr() {
        _isCprRunning.value = true
        startCprTimerLoop()
        if (_isMetronomeSoundOn.value) {
            alertManager.startCprMetronome(110) {
                _metronomeTick.value = System.currentTimeMillis()
            }
        }

        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            if (alert.status != AlertStatus.RESUSCITATION.name) {
                repository.updateAlert(alert.copy(status = AlertStatus.RESUSCITATION.name))
                repository.insertLog(
                    IncidentLogEntity(
                        alertId = alert.id,
                        authorName = alert.leadDoctor,
                        authorRole = "Team Leader",
                        logType = "CPR_CYCLE",
                        message = "Siklus Resusitasi CPR dimulai. Ritme kompresi dada 100-120x/menit aktif."
                    )
                )
            }
        }
    }

    fun pauseCpr() {
        _isCprRunning.value = false
        cprTimerJob?.cancel()
        cprTimerJob = null
        alertManager.stopCprMetronome()
    }

    fun toggleMetronomeSound() {
        _isMetronomeSoundOn.value = !_isMetronomeSoundOn.value
        if (_isCprRunning.value) {
            if (_isMetronomeSoundOn.value) {
                alertManager.startCprMetronome(110) {
                    _metronomeTick.value = System.currentTimeMillis()
                }
            } else {
                alertManager.stopCprMetronome()
            }
        }
    }

    fun recordNextCprCycle() {
        _cprCycleNumber.value += 1
        _cprCycleSecondsRemaining.value = 120
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = alert.leadDoctor,
                    authorRole = "Team Leader",
                    logType = "CPR_CYCLE",
                    message = "Evaluasi Siklus CPR ke-${_cprCycleNumber.value - 1} selesai. Pergantian kompresor dada. Memulai Siklus ke-${_cprCycleNumber.value}."
                )
            )
        }
    }

    fun recordDefibrillation(joules: Int, rhythm: String) {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            val newCount = alert.totalShocks + 1
            repository.updateAlert(alert.copy(totalShocks = newCount))

            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = "Petugas Defibrilator",
                    authorRole = "Defib Specialist",
                    logType = "DEFIB_SHOCK",
                    message = "DEFIBRILASI / SHOCK ke-$newCount diberikan: $joules Joule Biphasic. Irama terpantau: $rhythm. Lanjutkan CPR segera."
                )
            )
            // Reset cycle timer after shock
            _cprCycleSecondsRemaining.value = 120
        }
    }

    fun recordMedication(medName: String) {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            val isEpi = medName.contains("Epinefrin", ignoreCase = true) || medName.contains("Adrenalin", ignoreCase = true)
            val newEpiCount = if (isEpi) alert.totalEpiDoses + 1 else alert.totalEpiDoses
            if (isEpi) {
                _epinephrineTimerRemaining.value = 240 // Reset 4-minute countdown
            }

            repository.updateAlert(alert.copy(totalEpiDoses = newEpiCount))

            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = "Farmasi Klinis",
                    authorRole = "Drug Administration",
                    logType = "DRUG_GIVEN",
                    message = "Pemberian obat darurat: $medName berhasil masuk melalui IV line.${if (isEpi) " (Dosis Epinefrin ke-$newEpiCount). Timer dosis berikutnya: 4 menit." else ""}"
                )
            )
        }
    }

    fun recordAirwayIntervention(action: String) {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = "Perawat Airway",
                    authorRole = "Airway Specialist",
                    logType = "AIRWAY_PROCEDURE",
                    message = "Tindakan Airway: $action. Ventilasi adekuat dengan oksigen 100%."
                )
            )
        }
    }

    fun recordRoscAchieved() {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            pauseCpr()
            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = alert.leadDoctor,
                    authorRole = "Team Leader",
                    logType = "ROSC_ACHIEVED",
                    message = "🎉 ROSC TERCAPAI (Return of Spontaneous Circulation)! Nadi karotis teraba, irama sinus stabil. Perawatan pasca resusitasi dimulai."
                )
            )
        }
    }

    fun resolveAlert(outcome: String) {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            pauseCpr()
            stopSirenSound()
            val now = System.currentTimeMillis()
            val cprDuration = _cprTotalSeconds.value

            val updatedAlert = alert.copy(
                status = AlertStatus.RESOLVED.name,
                resolvedAt = now,
                outcome = outcome,
                cprDurationSeconds = cprDuration
            )
            repository.updateAlert(updatedAlert)
            repository.resetRespondersForAlert(alert.id)

            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = alert.leadDoctor,
                    authorRole = "Team Leader",
                    logType = "STATUS_UPDATE",
                    message = "Kondisi darurat Code Blue RESMI DISELESAIKAN. Disposisi pasien: $outcome. Total durasi resusitasi: ${cprDuration / 60}m ${cprDuration % 60}d."
                )
            )

            _headsUpAlert.value = null
        }
    }

    fun cancelAlert(reason: String) {
        viewModelScope.launch {
            val alert = currentAlert.value ?: return@launch
            pauseCpr()
            stopSirenSound()
            val now = System.currentTimeMillis()

            val updatedAlert = alert.copy(
                status = AlertStatus.CANCELLED.name,
                resolvedAt = now,
                outcome = "Dibatalkan: $reason"
            )
            repository.updateAlert(updatedAlert)
            repository.resetRespondersForAlert(alert.id)

            repository.insertLog(
                IncidentLogEntity(
                    alertId = alert.id,
                    authorName = "Supervisi Komando",
                    authorRole = "Pengawas",
                    logType = "STATUS_UPDATE",
                    message = "Panggilan Code Blue dibatalkan ($reason). Seluruh personel kembali ke unit masing-masing."
                )
            )

            _headsUpAlert.value = null
        }
    }

    private fun startCprTimerLoop() {
        cprTimerJob?.cancel()
        cprTimerJob = viewModelScope.launch {
            while (isActive && _isCprRunning.value) {
                delay(1000)
                _cprTotalSeconds.value += 1
                if (_cprCycleSecondsRemaining.value > 0) {
                    _cprCycleSecondsRemaining.value -= 1
                } else {
                    recordNextCprCycle()
                }

                if (_epinephrineTimerRemaining.value > 0) {
                    _epinephrineTimerRemaining.value -= 1
                }
            }
        }
    }

    // Real-time location simulation loop & response timer tick
    private fun startSimulationTicker() {
        simulationLoopJob?.cancel()
        simulationLoopJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _currentTimestamp.value = System.currentTimeMillis()
                advanceRespondersMovement()
            }
        }
    }

    private suspend fun advanceRespondersMovement() {
        val activeList = activeAlerts.value
        if (activeList.isEmpty()) return

        val respondersList = responders.value
        var hasChanges = false
        val updatedResponders = respondersList.map { resp ->
            if (resp.isOnDuty && resp.state == "RESPONDING" && resp.assignedAlertId != null) {
                val targetAlert = activeList.find { it.id == resp.assignedAlertId }
                if (targetAlert != null) {
                    val dx = targetAlert.roomPosX - resp.posX
                    val dy = targetAlert.roomPosY - resp.posY
                    val dist = hypot(dx, dy)

                    val newEta = max(0, resp.etaSeconds - 1)
                    if (dist < 0.04f || newEta == 0) {
                        // Responder reached scene!
                        hasChanges = true
                        val now = System.currentTimeMillis()
                        // If alert had no arrival time, set it and stop siren!
                        if (targetAlert.firstArrivalAt == null) {
                            val updatedAlert = targetAlert.copy(
                                status = AlertStatus.ON_SCENE.name,
                                firstArrivalAt = now
                            )
                            repository.updateAlert(updatedAlert)
                            stopSirenSound()
                            val responseSecs = ((now - targetAlert.activatedAt) / 1000).toInt()
                            val isWithinSla = responseSecs <= 300
                            val mins = responseSecs / 60
                            val secs = responseSecs % 60
                            val slaText = if (isWithinSla) "✅ TARGET TERCAPAI (< 5 Menit)" else "⚠️ MELEBIHI TARGET (> 5 Menit)"

                            repository.insertLog(
                                IncidentLogEntity(
                                    alertId = targetAlert.id,
                                    authorName = resp.name,
                                    authorRole = resp.roleTitle,
                                    logType = "ARRIVAL_RECORDED",
                                    message = "🚨 ${resp.name} (${resp.roleTitle}) tiba di lokasi! Respon time tercatat: ${mins}m ${secs}d. $slaText."
                                )
                            )
                        }
                        resp.copy(
                            state = "ON_SCENE",
                            posX = targetAlert.roomPosX,
                            posY = targetAlert.roomPosY,
                            etaSeconds = 0,
                            currentFloor = targetAlert.floor
                        )
                    } else {
                        // Step towards target
                        hasChanges = true
                        val step = 0.025f // Realistic corridor walking speed
                        val ratio = step / dist
                        val newX = resp.posX + dx * ratio
                        val newY = resp.posY + dy * ratio
                        resp.copy(
                            posX = newX,
                            posY = newY,
                            etaSeconds = newEta,
                            currentFloor = targetAlert.floor
                        )
                    }
                } else {
                    resp
                }
            } else {
                resp
            }
        }

        if (hasChanges) {
            repository.updateResponders(updatedResponders)
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationLoopJob?.cancel()
        cprTimerJob?.cancel()
        alertManager.stopSiren()
        alertManager.stopCprMetronome()
    }
}
