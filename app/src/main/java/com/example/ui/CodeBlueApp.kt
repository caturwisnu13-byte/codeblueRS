package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HeadsUpAlertBanner
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.RequesterScreen
import com.example.ui.screens.ResponderScreen
import com.example.ui.screens.SupervisorScreen
import com.example.ui.theme.CodeBlueAccent
import com.example.ui.theme.DarkHospitalBg
import com.example.ui.theme.DarkHospitalSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.util.ReportExporter
import com.example.viewmodel.AppRole
import com.example.viewmodel.CodeBlueViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlueApp(
    viewModel: CodeBlueViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf(false) }

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    val responders by viewModel.responders.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val currentAlert by viewModel.currentAlert.collectAsStateWithLifecycle()
    val currentLogs by viewModel.currentAlertLogs.collectAsStateWithLifecycle()
    val selectedResponderId by viewModel.selectedResponderId.collectAsStateWithLifecycle()
    val headsUpAlert by viewModel.headsUpAlert.collectAsStateWithLifecycle()

    val isCprRunning by viewModel.isCprRunning.collectAsStateWithLifecycle()
    val cprTotalSeconds by viewModel.cprTotalSeconds.collectAsStateWithLifecycle()
    val cprCycleSecondsRemaining by viewModel.cprCycleSecondsRemaining.collectAsStateWithLifecycle()
    val cprCycleNumber by viewModel.cprCycleNumber.collectAsStateWithLifecycle()
    val isMetronomeSoundOn by viewModel.isMetronomeSoundOn.collectAsStateWithLifecycle()
    val epinephrineTimerRemaining by viewModel.epinephrineTimerRemaining.collectAsStateWithLifecycle()
    val selectedCardiacRhythm by viewModel.selectedCardiacRhythm.collectAsStateWithLifecycle()
    val metronomeTick by viewModel.metronomeTick.collectAsStateWithLifecycle()
    val isSirenActive by viewModel.isSirenActive.collectAsStateWithLifecycle()
    val mapSelectedFloor by viewModel.mapSelectedFloor.collectAsStateWithLifecycle()

    val boundRoom by viewModel.boundRoom.collectAsStateWithLifecycle()
    val boundRoomId by viewModel.boundRoomId.collectAsStateWithLifecycle()
    val currentTimestamp by viewModel.currentTimestamp.collectAsStateWithLifecycle()
    val canSilenceAlarm = viewModel.canCurrentResponderSilenceAlarm()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkHospitalBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (activeAlerts.isNotEmpty()) EmergencyRedBright
                                    else if (currentRole == AppRole.ADMIN) Color(0xFFD97706)
                                    else CodeBlueAccent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentRole == AppRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Emergency,
                                contentDescription = "Hospital Role Symbol",
                                tint = if (activeAlerts.isNotEmpty() || currentRole == AppRole.ADMIN) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (currentRole) {
                                        AppRole.ROOM_REQUESTER -> "CodeBlue RS • Ruangan"
                                        AppRole.CODE_BLUE_TEAM -> "CodeBlue RS • Tim Jaga"
                                        AppRole.SUPERVISOR -> "CodeBlue RS • Pengawas"
                                        AppRole.ADMIN -> "CodeBlue RS • Portal Admin"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                if (activeAlerts.isNotEmpty() && currentRole != AppRole.ADMIN) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = EmergencyRedBright
                                    ) {
                                        Text(
                                            text = "DARURAT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = when (currentRole) {
                                    AppRole.ROOM_REQUESTER -> "Unit: ${boundRoom.name}"
                                    AppRole.CODE_BLUE_TEAM -> {
                                        val me = responders.find { it.id == selectedResponderId }
                                        val leaderTag = if (me?.isLeader == true) " • LEADER ⭐" else ""
                                        "Petugas: ${me?.name ?: "Tim Medis"}${leaderTag} (${if (me?.isOnDuty == true) "ON-DUTY" else "OFF-DUTY"})"
                                    }
                                    AppRole.SUPERVISOR -> "Command Center, Radar & Rekap Laporan"
                                    AppRole.ADMIN -> "Kelola Master Ruangan, Anggota & Leader Tim"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                actions = {
                    // Button to switch separated mode / app role
                    IconButton(
                        onClick = { showRoleSwitcherDialog = true },
                        modifier = Modifier.testTag("appbar_switch_role_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Ganti Mode Peran",
                            tint = CodeBlueAccent
                        )
                    }

                    if (isSirenActive && canSilenceAlarm) {
                        IconButton(
                            onClick = { viewModel.stopSirenSound() },
                            modifier = Modifier.testTag("appbar_silence_siren_btn")
                        ) {
                            Icon(Icons.Default.VolumeOff, contentDescription = "Silence Siren", tint = Color(0xFFEF4444))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkHospitalSurface
                )
            )
        },
        bottomBar = {
            // Mode Switcher Navigation Bar only for the 3 operational roles (Admin has clean dedicated console)
            if (currentRole != AppRole.ADMIN) {
                NavigationBar(
                    containerColor = DarkHospitalSurface,
                    modifier = Modifier.testTag("role_bottom_navigation")
                ) {
                    // Tab 1: Unit Ruangan
                    NavigationBarItem(
                        selected = currentRole == AppRole.ROOM_REQUESTER,
                        onClick = { viewModel.switchRole(AppRole.ROOM_REQUESTER) },
                        icon = {
                            Icon(Icons.Default.MeetingRoom, contentDescription = "Unit Ruangan")
                        },
                        label = { Text("Ruangan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CodeBlueAccent,
                            indicatorColor = CodeBlueAccent,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_requester")
                    )

                    // Tab 2: Tim Code Blue
                    NavigationBarItem(
                        selected = currentRole == AppRole.CODE_BLUE_TEAM,
                        onClick = { viewModel.switchRole(AppRole.CODE_BLUE_TEAM) },
                        icon = {
                            BadgedBox(badge = {
                                if (activeAlerts.isNotEmpty()) {
                                    Badge(containerColor = EmergencyRedBright) {
                                        Text("${activeAlerts.size}", color = Color.White)
                                    }
                                }
                            }) {
                                Icon(Icons.Default.MedicalServices, contentDescription = "Tim Code Blue")
                            }
                        },
                        label = { Text("Tim Respon", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CodeBlueAccent,
                            indicatorColor = CodeBlueAccent,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_responder")
                    )

                    // Tab 3: Pengawas / Komando
                    NavigationBarItem(
                        selected = currentRole == AppRole.SUPERVISOR,
                        onClick = { viewModel.switchRole(AppRole.SUPERVISOR) },
                        icon = {
                            Icon(Icons.Default.Security, contentDescription = "Pengawas")
                        },
                        label = { Text("Pengawas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CodeBlueAccent,
                            indicatorColor = CodeBlueAccent,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_supervisor")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-visibility Heads-Up Alert Notification Banner
            if (currentRole != AppRole.ADMIN) {
                HeadsUpAlertBanner(
                    headsUpAlert = headsUpAlert,
                    isSirenActive = isSirenActive,
                    canSilenceSiren = canSilenceAlarm,
                    onOpenAlertDetails = { alertId ->
                        viewModel.selectAlert(alertId)
                        viewModel.switchRole(AppRole.CODE_BLUE_TEAM)
                    },
                    onSilenceSiren = { viewModel.stopSirenSound() },
                    onDismissBanner = { viewModel.dismissHeadsUpBanner() }
                )
            }

            // Dynamic Screen based on Role
            when (currentRole) {
                AppRole.ROOM_REQUESTER -> {
                    RequesterScreen(
                        boundRoom = boundRoom,
                        activeAlert = currentAlert,
                        responders = responders,
                        currentTimestamp = currentTimestamp,
                        onTriggerCodeBlue = { patientType, condition, needsDefib, cprStarted, bed ->
                            viewModel.triggerCodeBlueFromBoundRoom(
                                patientCategory = patientType,
                                conditionSummary = condition,
                                needsDefib = needsDefib,
                                cprStartedLocally = cprStarted,
                                bedNumber = bed
                            )
                        },
                        onMarkTeamArrivedAndDeactivate = {
                            currentAlert?.let { alert ->
                                viewModel.markArrivalAndDeactivate(alert.id, "Staf ${boundRoom.name}")
                            }
                        },
                        onCancelAlert = { reason ->
                            viewModel.cancelAlert(reason)
                        },
                        onSwitchRoomLink = { linkOrToken ->
                            viewModel.bindDeviceToLink(linkOrToken)
                        }
                    )
                }

                AppRole.CODE_BLUE_TEAM -> {
                    ResponderScreen(
                        activeAlert = currentAlert,
                        responders = responders,
                        selectedResponderId = selectedResponderId,
                        logs = currentLogs,
                        currentTimestamp = currentTimestamp,
                        isCprRunning = isCprRunning,
                        cprTotalSeconds = cprTotalSeconds,
                        cprCycleSecondsRemaining = cprCycleSecondsRemaining,
                        cprCycleNumber = cprCycleNumber,
                        epinephrineTimerRemaining = epinephrineTimerRemaining,
                        selectedCardiacRhythm = selectedCardiacRhythm,
                        isMetronomeSoundOn = isMetronomeSoundOn,
                        metronomeTick = metronomeTick,
                        isSirenActive = isSirenActive,
                        canSilenceAlarm = canSilenceAlarm,
                        onSelectResponder = { id -> viewModel.selectResponder(id) },
                        onToggleDuty = { id -> viewModel.toggleResponderDuty(id) },
                        onAcceptDispatch = { id -> viewModel.responderAcceptDispatch(id) },
                        onMarkArrived = { id -> viewModel.markResponderArrived(id) },
                        onStartPauseCpr = {
                            if (isCprRunning) viewModel.pauseCpr() else viewModel.startCpr()
                        },
                        onNextCprCycle = { viewModel.recordNextCprCycle() },
                        onToggleMetronomeSound = { viewModel.toggleMetronomeSound() },
                        onAdministerMedication = { med -> viewModel.recordMedication(med) },
                        onDeliverDefibrillation = { j, r -> viewModel.recordDefibrillation(j, r) },
                        onAirwayAction = { action -> viewModel.recordAirwayIntervention(action) },
                        onRoscAchieved = { viewModel.recordRoscAchieved() },
                        onResolveAlert = { outcome -> viewModel.resolveAlert(outcome) },
                        onSilenceSiren = { viewModel.stopSirenSound() }
                    )
                }

                AppRole.SUPERVISOR -> {
                    SupervisorScreen(
                        activeAlerts = activeAlerts,
                        allAlerts = allAlerts,
                        responders = responders,
                        rooms = rooms,
                        selectedFloor = mapSelectedFloor,
                        currentBoundRoomId = boundRoomId,
                        onSelectFloor = { floor -> viewModel.selectFloor(floor) },
                        onTriggerDrill = { room ->
                            viewModel.triggerCodeBlue(
                                room = room,
                                bedNumber = room.defaultBed,
                                patientCategory = com.example.data.PatientCategory.ADULT,
                                conditionSummary = "Simulasi Henti Jantung Dadakan (Drill)",
                                needsDefib = true,
                                cprStartedLocally = true,
                                callerName = "Pengawas Komando",
                                isDrill = true
                            )
                        },
                        onToggleDuty = { id -> viewModel.toggleResponderDuty(id) },
                        onBindRoomToDevice = { roomId -> viewModel.bindDeviceToRoom(roomId) },
                        onDownloadIndividualReport = { alert ->
                            coroutineScope.launch {
                                val logs = viewModel.getLogsForAlertOnce(alert.id)
                                ReportExporter.exportIndividualReport(context, alert, logs)
                            }
                        },
                        onDownloadRecapReport = { alertsList, periodTitle ->
                            ReportExporter.exportPeriodRecapCsv(context, alertsList, periodTitle)
                        }
                    )
                }

                AppRole.ADMIN -> {
                    AdminScreen(
                        rooms = rooms,
                        responders = responders,
                        onAddRoom = { viewModel.addRoom(it) },
                        onUpdateRoom = { viewModel.updateRoom(it) },
                        onDeleteRoom = { viewModel.deleteRoom(it) },
                        onAddResponder = { viewModel.addResponder(it) },
                        onUpdateResponder = { viewModel.updateResponder(it) },
                        onDeleteResponder = { viewModel.deleteResponder(it) },
                        onToggleLeader = { viewModel.toggleResponderLeader(it) },
                        onToggleDuty = { viewModel.toggleResponderDuty(it) },
                        onUpdateAdminPin = { viewModel.updateAdminPin(it) },
                        onExitAdmin = { viewModel.switchRole(AppRole.SUPERVISOR) }
                    )
                }
            }
        }
    }

    // Modal dialog to explicitly set or switch device role/mode
    if (showRoleSwitcherDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSwitcherDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = CodeBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pemisahan Peran Aplikasi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pilih peran perangkat ini di rumah sakit. Masing-masing peran terisolasi sesuai tugasnya:",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    // 1. Kios Ruangan
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchRole(AppRole.ROOM_REQUESTER)
                                showRoleSwitcherDialog = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentRole == AppRole.ROOM_REQUESTER) Color(0xFF1E3A5F) else Color(0xFF161F2E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentRole == AppRole.ROOM_REQUESTER) CodeBlueAccent else Color(0xFF27394E)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🏥 1. Unit Ruangan (Kios Kamar)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Khusus tablet kamar/ICU. Langsung tombol aktivasi & deaktifasi saat tim tiba.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    // 2. Tim Code Blue
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchRole(AppRole.CODE_BLUE_TEAM)
                                showRoleSwitcherDialog = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentRole == AppRole.CODE_BLUE_TEAM) Color(0xFF1E3A5F) else Color(0xFF161F2E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentRole == AppRole.CODE_BLUE_TEAM) CodeBlueAccent else Color(0xFF27394E)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🩺 2. Tim Code Blue (Petugas Jaga)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Khusus ponsel tim respon. Petugas memilih namanya, alarm anti-silent, radar peta & RJP.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    // 3. Pengawas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchRole(AppRole.SUPERVISOR)
                                showRoleSwitcherDialog = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentRole == AppRole.SUPERVISOR) Color(0xFF1E3A5F) else Color(0xFF161F2E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentRole == AppRole.SUPERVISOR) CodeBlueAccent else Color(0xFF27394E)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🛡️ 3. Pengawas (Command Center)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Monitoring roster tim jaga real-time, link admin kamar, serta unduh laporan per insiden & rekapitulasi.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Protected Admin Entrance (Khusus Administrator RS, tidak bisa dijangkau staf biasa tanpa PIN)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showRoleSwitcherDialog = false
                                adminPinInput = ""
                                adminPinError = false
                                showAdminPinDialog = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF231908)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🔒 Portal Administrator RS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFDE68A))
                                Text("Kelola master nama ruangan, anggota & leader tim (Memerlukan PIN)", fontSize = 10.sp, color = Color(0xFFF59E0B))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleSwitcherDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal dialog to enter Admin PIN (Protects Admin from ordinary users)
    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifikasi PIN Admin", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Portal ini terpisah dan hanya untuk Administrator / IT Rumah Sakit. Masukkan PIN Admin (Default: 1199):",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            if (it.length <= 6) {
                                adminPinInput = it
                                adminPinError = false
                            }
                        },
                        label = { Text("PIN Master Admin") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = adminPinError,
                        supportingText = if (adminPinError) {
                            { Text("PIN salah! Akses ditolak untuk staf umum.", color = EmergencyRed) }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.verifyAdminPin(adminPinInput)) {
                            viewModel.switchRole(AppRole.ADMIN)
                            showAdminPinDialog = false
                            Toast.makeText(context, "Selamat datang di Portal Administrator RS", Toast.LENGTH_SHORT).show()
                        } else {
                            adminPinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706), contentColor = Color.White)
                ) {
                    Text("Buka Admin", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPinDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
