package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlertStatus
import com.example.data.CodeBlueAlertEntity
import com.example.data.HospitalData
import com.example.data.IncidentLogEntity
import com.example.data.ResponderEntity
import com.example.ui.components.CPRMetronomeComponent
import com.example.ui.components.HospitalMapComponent
import com.example.ui.theme.CodeBlueAccent
import com.example.ui.theme.CodeBluePrimary
import com.example.ui.theme.DarkHospitalCard
import com.example.ui.theme.EmergencyOnScene
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.EmergencySuccess
import com.example.ui.theme.EmergencyWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderScreen(
    activeAlert: CodeBlueAlertEntity?,
    responders: List<ResponderEntity>,
    selectedResponderId: String,
    logs: List<IncidentLogEntity>,
    currentTimestamp: Long,
    isCprRunning: Boolean,
    cprTotalSeconds: Int,
    cprCycleSecondsRemaining: Int,
    cprCycleNumber: Int,
    epinephrineTimerRemaining: Int,
    selectedCardiacRhythm: String,
    isMetronomeSoundOn: Boolean,
    metronomeTick: Long,
    isSirenActive: Boolean,
    canSilenceAlarm: Boolean,
    onSelectResponder: (String) -> Unit,
    onToggleDuty: (String) -> Unit,
    onAcceptDispatch: (String) -> Unit,
    onMarkArrived: (String) -> Unit,
    onStartPauseCpr: () -> Unit,
    onNextCprCycle: () -> Unit,
    onToggleMetronomeSound: () -> Unit,
    onAdministerMedication: (String) -> Unit,
    onDeliverDefibrillation: (Int, String) -> Unit,
    onAirwayAction: (String) -> Unit,
    onRoscAchieved: () -> Unit,
    onResolveAlert: (String) -> Unit,
    onSilenceSiren: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val myProfile = responders.find { it.id == selectedResponderId } ?: responders.firstOrNull()

    var showShockDialog by remember { mutableStateOf(false) }
    var selectedJoules by remember { mutableIntStateOf(200) }
    var showMedDialog by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf(HospitalData.resuscitationMedications.first()) }
    var showAirwayDialog by remember { mutableStateOf(false) }
    var airwayNote by remember { mutableStateOf("Intubasi ETT no 7.5 fiksasi 21cm, suara napas vesikuler kanan=kiri") }
    var showResolveDialog by remember { mutableStateOf(false) }
    var resolveDisposition by remember { mutableStateOf("ROSC Tercapai, Pasien Ditransfer ke ICU Dewasa") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("responder_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Responder Identity Switcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Identitas Petugas (Pilih Akun Anda):",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(responders) { resp ->
                            val isSelected = resp.id == selectedResponderId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectResponder(resp.id) },
                                label = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(if (resp.isOnDuty) EmergencySuccess else Color.Gray, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (resp.isLeader) "👑 ${resp.name}" else resp.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            text = if (resp.isLeader) "⭐ Leader • ${resp.roleTitle}" else resp.roleTitle,
                                            fontSize = 9.sp
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("select_responder_${resp.id}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CodeBlueAccent,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF0F1B29),
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }
                }
            }
        }

        // On-Duty / Off-Duty Toggle Card (Bagi anggota yang tidak berjaga, bisa menonaktifkan aplikasi)
        if (myProfile != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("duty_toggle_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (myProfile.isOnDuty) Color(0xFF0F2618) else Color(0xFF1E242B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (myProfile.isOnDuty) EmergencySuccess else Color(0xFF475569)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (myProfile.isOnDuty) EmergencySuccess else Color(0xFF334155),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (myProfile.isOnDuty) "STATUS: SEDANG BERJAGA (ON-DUTY)" else "STATUS: LEPAS DINAS (OFF-DUTY)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (myProfile.isOnDuty) EmergencySuccess else Color(0xFF94A3B8)
                                )
                                Text(
                                    text = if (myProfile.isOnDuty)
                                        "Ponsel Anda akan membunyikan alarm darurat bila ada Code Blue."
                                    else
                                        "Aplikasi dinonaktifkan. Alarm tidak akan berbunyi di ponsel Anda.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        Switch(
                            checked = myProfile.isOnDuty,
                            onCheckedChange = { onToggleDuty(myProfile.id) },
                            modifier = Modifier.testTag("toggle_duty_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmergencySuccess,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }

        val isAlertActive = activeAlert != null &&
                activeAlert.status != AlertStatus.RESOLVED.name &&
                activeAlert.status != AlertStatus.CANCELLED.name

        if (isAlertActive && activeAlert != null) {
            // Calculate response time live
            val elapsedSecs = if (activeAlert.firstArrivalAt != null) {
                ((activeAlert.firstArrivalAt - activeAlert.activatedAt) / 1000).toInt()
            } else {
                ((currentTimestamp - activeAlert.activatedAt) / 1000).toInt().coerceAtLeast(0)
            }
            val mins = elapsedSecs / 60
            val secs = elapsedSecs % 60
            val isUnder5Min = elapsedSecs <= 300

            // ================== EMERGENCY DISPATCH ACTION CARD ==================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_alert_dispatch_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = if (activeAlert.isDrill) "LATIHAN DRILL" else "🚨 PANGGILAN CODE BLUE RESUSITASI",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = EmergencyRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Alarm notification badge (Alarm tidak bisa disenyapkan untuk on-duty team)
                            if (isSirenActive) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (!canSilenceAlarm) "ALARM WAJIB" else "ALARM BUNYI",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Yellow
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "LOKASI: ${activeAlert.roomName} (${activeAlert.bedNumber})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "${activeAlert.building} • Lantai ${activeAlert.floor}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFEBEE)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Response Time Bar
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Waktu Respon: ${String.format("%02d:%02d", mins, secs)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = if (isUnder5Min) "Target: < 5 Menit" else "⚠️ > 5 Menit (Overdue)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnder5Min) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                                )
                            }
                        }

                        // Mandatory Alarm Notice
                        if (isSirenActive && !canSilenceAlarm) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ Alarm darurat tidak dapat disenyapkan hingga petugas tiba di lokasi.",
                                fontSize = 10.sp,
                                color = Color(0xFFFEF08A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Responder personal action buttons
                        val isMeOnScene = myProfile?.state == "ON_SCENE"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isMeOnScene) {
                                Button(
                                    onClick = { myProfile?.let { onMarkArrived(it.id) } },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("mark_arrived_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = EmergencyRed
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("TIBA DI LOKASI (STOP ALARM)", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Anda Telah Tiba di Lokasi", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            Button(
                                onClick = { showResolveDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencySuccess),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("resolve_codeblue_btn")
                            ) {
                                Text("Selesaikan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Tabs: Asisten CPR, Navigasi Rute, Log Kejadian
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkHospitalCard,
                    contentColor = CodeBlueAccent,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Asisten CPR", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_cpr")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Navigasi", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_navigation")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Log Kejadian", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_logs")
                    )
                }
            }

            // TAB 0: CPR Assistant
            if (selectedTab == 0) {
                item {
                    CPRMetronomeComponent(
                        isCprRunning = isCprRunning,
                        cycleSecondsRemaining = cprCycleSecondsRemaining,
                        totalSecondsElapsed = cprTotalSeconds,
                        cycleNumber = cprCycleNumber,
                        epinephrineTimerRemaining = epinephrineTimerRemaining,
                        totalShocks = activeAlert.totalShocks,
                        totalEpiDoses = activeAlert.totalEpiDoses,
                        isMetronomeSoundOn = isMetronomeSoundOn,
                        metronomeTick = metronomeTick,
                        onStartPauseCpr = onStartPauseCpr,
                        onNextCycle = onNextCprCycle,
                        onToggleMetronomeSound = onToggleMetronomeSound,
                        onAdministerEpinephrine = { onAdministerMedication("Epinefrin 1 mg IV bolus") },
                        onTriggerShock = { showShockDialog = true },
                        onRoscAchieved = onRoscAchieved
                    )
                }

                // Quick Clinical Interventions Bar
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Tindakan Medis Lanjutan (Advanced Life Support)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showMedDialog = true },
                                    modifier = Modifier.weight(1f).testTag("quick_med_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Obat IV", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { showAirwayDialog = true },
                                    modifier = Modifier.weight(1f).testTag("quick_airway_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Airway/ETT", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { showShockDialog = true },
                                    modifier = Modifier.weight(1f).testTag("quick_defib_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Defibrilasi", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: Navigation
            if (selectedTab == 1) {
                item {
                    HospitalMapComponent(
                        currentFloor = activeAlert.floor,
                        onFloorSelected = {},
                        activeAlert = activeAlert,
                        responders = responders,
                        highlightRoomName = activeAlert.roomName
                    )
                }
            }

            // TAB 2: Log Kejadian
            if (selectedTab == 2) {
                item {
                    Text(
                        text = "Kronologi Resusitasi Real-Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (logs.isEmpty()) {
                    item {
                        Text("Belum ada catatan kejadian.", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    items(logs.reversed()) { log ->
                        IncidentLogCard(log = log)
                    }
                }
            }

        } else {
            // Standby View
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (myProfile?.isOnDuty == true) EmergencySuccess.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = if (myProfile?.isOnDuty == true) EmergencySuccess else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (myProfile?.isOnDuty == true) "Status: SIAGA PENUH (ON-DUTY)" else "Status: LEPAS DINAS (OFF-DUTY)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (myProfile?.isOnDuty == true)
                                "Tidak ada panggilan Code Blue aktif saat ini. Anda siaga menerima panggilan darurat."
                            else
                                "Anda sedang tidak bertugas. Aktifkan switch di atas bila Anda mulai dinas jaga.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Checklist
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = CodeBlueAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kesiapan Alat Emergensi RS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        ChecklistItem("Defibrilator Biphasic / AED dengan baterai terisi", true)
                        ChecklistItem("Sungkup Bag-Valve-Mask (BVM) & Tabung O2", true)
                        ChecklistItem("Laringoskop & Set Intubasi ETT Lengkap", true)
                        ChecklistItem("Ampul Epinefrin 1mg & Amiodarone 150mg", true)
                    }
                }
            }
        }
    }

    // Modal: Defibrillator Shock
    if (showShockDialog) {
        AlertDialog(
            onDismissRequest = { showShockDialog = false },
            title = { Text("Pemberian Shock Defibrilator", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pilih Energi Joules (Biphasic):", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(120, 150, 200).forEach { j ->
                            FilterChip(
                                selected = selectedJoules == j,
                                onClick = { selectedJoules = j },
                                label = { Text("$j Joule", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("joules_chip_$j")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShockDialog = false
                        onDeliverDefibrillation(selectedJoules, selectedCardiacRhythm)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("SHOCK SEKARANG", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShockDialog = false }) { Text("Batal") }
            }
        )
    }

    // Modal: Administer Medication
    if (showMedDialog) {
        AlertDialog(
            onDismissRequest = { showMedDialog = false },
            title = { Text("Berikan Obat Resusitasi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    HospitalData.resuscitationMedications.forEach { med ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMedication = med }
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(if (selectedMedication == med) CodeBlueAccent else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(med, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMedDialog = false
                        onAdministerMedication(selectedMedication)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeBluePrimary)
                ) {
                    Text("Catat Obat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMedDialog = false }) { Text("Batal") }
            }
        )
    }

    // Modal: Airway Intervention
    if (showAirwayDialog) {
        AlertDialog(
            onDismissRequest = { showAirwayDialog = false },
            title = { Text("Tindakan Airway & Ventilasi", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = airwayNote,
                    onValueChange = { airwayNote = it },
                    label = { Text("Catatan Tindakan Jalan Napas") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAirwayDialog = false
                        onAirwayAction(airwayNote)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeBluePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAirwayDialog = false }) { Text("Batal") }
            }
        )
    }

    // Modal: Resolve Resuscitation
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Selesaikan Resusitasi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        "ROSC Tercapai, Pasien Ditransfer ke ICU Dewasa",
                        "Irama Sinus Kembali, Hemodinamik Stabil di Ruangan",
                        "Resusitasi Dihentikan Atas Permintaan Keluarga (DNR)",
                        "Resusitasi Dihentikan Karena Asistol Refrakter (Exitus)"
                    ).forEach { outcome ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { resolveDisposition = outcome }
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(if (resolveDisposition == outcome) EmergencySuccess else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(outcome, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResolveDialog = false
                        onResolveAlert(resolveDisposition)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencySuccess)
                ) {
                    Text("Selesai")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ChecklistItem(text: String, isReady: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Emergency,
            contentDescription = null,
            tint = if (isReady) EmergencySuccess else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 11.sp, color = Color(0xFFCBD5E1))
    }
}

@Composable
private fun IncidentLogCard(log: IncidentLogEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("incident_log_${log.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132030))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${log.authorName} (${log.authorRole})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = CodeBlueAccent
                )
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                Text(timeStr, fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(log.message, fontSize = 12.sp, color = Color.White)
        }
    }
}
