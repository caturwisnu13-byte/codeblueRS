package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlertStatus
import com.example.data.CodeBlueAlertEntity
import com.example.data.HospitalData
import com.example.data.HospitalRoom
import com.example.data.ResponderEntity
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
fun SupervisorScreen(
    activeAlerts: List<CodeBlueAlertEntity>,
    allAlerts: List<CodeBlueAlertEntity>,
    responders: List<ResponderEntity>,
    rooms: List<com.example.data.HospitalRoomEntity> = emptyList(),
    selectedFloor: Int,
    currentBoundRoomId: String,
    onSelectFloor: (Int) -> Unit,
    onTriggerDrill: (room: HospitalRoom) -> Unit,
    onToggleDuty: (String) -> Unit,
    onBindRoomToDevice: (String) -> Unit,
    onDownloadIndividualReport: (CodeBlueAlertEntity) -> Unit = {},
    onDownloadRecapReport: (List<CodeBlueAlertEntity>, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedAlertId by remember { mutableStateOf<String?>(null) }
    var supervisorTab by remember { mutableIntStateOf(0) }
    var selectedPeriodIndex by remember { mutableIntStateOf(0) }

    val periodOptions = listOf(
        "Semua Periode",
        "Hari Ini",
        "7 Hari Terakhir",
        "Bulan Ini",
        "Kejadian Nyata",
        "Simulasi Drill"
    )

    val onDutyList = responders.filter { it.isOnDuty }
    val offDutyList = responders.filter { !it.isOnDuty }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("supervisor_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Command Center Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CodeBlueAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pengawas & Command Center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Monitoring Tim Jaga, Radar & Link Admin",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeAlerts.isNotEmpty()) EmergencyRed else EmergencySuccess
                    ) {
                        Text(
                            text = if (activeAlerts.isNotEmpty()) "${activeAlerts.size} Panggilan Aktif" else "Zona Siaga Aman",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Sub-Navigation Tabs
        item {
            PrimaryTabRow(
                selectedTabIndex = supervisorTab,
                containerColor = DarkHospitalCard,
                contentColor = CodeBlueAccent,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = supervisorTab == 0,
                    onClick = { supervisorTab = 0 },
                    text = { Text("Tim Jaga", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("subtab_roster")
                )
                Tab(
                    selected = supervisorTab == 1,
                    onClick = { supervisorTab = 1 },
                    text = { Text("Link Unit", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("subtab_room_links")
                )
                Tab(
                    selected = supervisorTab == 2,
                    onClick = { supervisorTab = 2 },
                    text = { Text("Radar & KPI", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("subtab_kpi")
                )
                Tab(
                    selected = supervisorTab == 3,
                    onClick = { supervisorTab = 3 },
                    text = { Text("Unduh Laporan", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("subtab_reports")
                )
            }
        }

        // TAB 0: Tim Code Blue On-Duty vs Off-Duty (Pengawas mengetahui siapa saja yang berjaga)
        if (supervisorTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daftar Tim Sedang Berjaga (ON-DUTY)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${onDutyList.size} personel aktif siaga menerima panggilan darurat.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmergencySuccess
                    ) {
                        Text(
                            text = "${onDutyList.size} Aktif",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (onDutyList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "⚠️ PERINGATAN: Tidak ada petugas yang sedang ON-DUTY! Aktifkan minimal 1 anggota tim di bawah ini.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRedBright,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                items(onDutyList) { resp ->
                    RosterMemberCard(
                        responder = resp,
                        onToggleDuty = { onToggleDuty(resp.id) }
                    )
                }
            }

            // Off-Duty Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daftar Petugas Lepas Dinas (OFF-DUTY)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "Petugas berikut menonaktifkan aplikasi (tidak menerima alarm).",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF334155)
                    ) {
                        Text(
                            text = "${offDutyList.size} Lepas Dinas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            items(offDutyList) { resp ->
                RosterMemberCard(
                    responder = resp,
                    onToggleDuty = { onToggleDuty(resp.id) }
                )
            }
        }

        // TAB 1: Admin Link Ruangan (Setiap Ruangan Punya 1 Link Akun)
        if (supervisorTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1E33)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CodeBlueAccent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = CodeBlueAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Link Generator Akun Ruangan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Setiap unit kamar rumah sakit memiliki 1 link unik. Perangkat tablet di ruangan yang membuka link ini akan otomatis terhubung ke identitas ruangan tersebut tanpa perlu memilih dropdown.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            val displayRooms = if (rooms.isNotEmpty()) {
                rooms.map { r ->
                    HospitalRoom(
                        id = r.id,
                        name = r.name,
                        building = r.building,
                        floor = r.floor,
                        bedCount = 1,
                        mapX = r.posX,
                        mapY = r.posY,
                        defaultBed = r.defaultBed
                    )
                }
            } else {
                HospitalData.rooms
            }

            items(displayRooms) { room ->
                val isCurrentBound = room.id == currentBoundRoomId
                RoomLinkCard(
                    room = room,
                    isCurrentBound = isCurrentBound,
                    onCopyLink = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Code Blue Room Link", room.accessLink)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Link Akun ${room.name} disalin!", Toast.LENGTH_SHORT).show()
                    },
                    onBindThisDevice = {
                        onBindRoomToDevice(room.id)
                        Toast.makeText(context, "Perangkat ditautkan ke ${room.name}!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // TAB 2: Radar Real-Time, Indikator Mutu (< 5 Menit) & Audit Trail
        if (supervisorTab == 2) {
            // KPI Analytics (< 5 Menit SLA)
            item {
                Text(
                    text = "Indikator Mutu Standar RS (Target Respon < 5 Menit)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))

                val completedAlerts = allAlerts.filter { it.status == AlertStatus.RESOLVED.name }
                val avgResponseSecs = if (completedAlerts.isNotEmpty()) {
                    completedAlerts.mapNotNull {
                        if (it.firstArrivalAt != null) ((it.firstArrivalAt - it.activatedAt) / 1000).toInt() else null
                    }.average().takeIf { !it.isNaN() }?.toInt() ?: 98
                } else 98

                val under5MinPercent = if (completedAlerts.isNotEmpty()) {
                    val fastCount = completedAlerts.count {
                        val diff = if (it.firstArrivalAt != null) (it.firstArrivalAt - it.activatedAt) / 1000 else 98
                        diff <= 300 // 5 minutes SLA
                    }
                    (fastCount * 100) / completedAlerts.size
                } else 100

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "Rata-Rata Waktu Respon",
                        value = "${avgResponseSecs / 60}m ${avgResponseSecs % 60}d",
                        subtitle = "Target KARS: < 5 Menit",
                        icon = Icons.Default.Speed,
                        accentColor = if (avgResponseSecs <= 300) CodeBlueAccent else EmergencyRedBright,
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "Kepatuhan < 5 Menit",
                        value = "$under5MinPercent%",
                        subtitle = "Standar Mutu Tercapai",
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmergencySuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Radar Lantai RS
            item {
                HospitalMapComponent(
                    currentFloor = selectedFloor,
                    onFloorSelected = onSelectFloor,
                    activeAlert = activeAlerts.firstOrNull(),
                    responders = responders
                )
            }

            // Drill Launcher
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simulasi Tanggap Darurat (Drill)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Luncurkan simulasi dadakan untuk mengukur kecepatan respon tim.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val randomRoom = HospitalData.rooms.random()
                                onTriggerDrill(randomRoom)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CodeBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("launch_drill_btn")
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mulai Drill", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Incident Audit List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Laporan Respon Time & Audit Kejadian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${allAlerts.size} Rekaman",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            items(allAlerts) { alert ->
                val isExpanded = expandedAlertId == alert.id
                IncidentReportItem(
                    alert = alert,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedAlertId = if (isExpanded) null else alert.id
                    },
                    onDownloadReport = {
                        onDownloadIndividualReport(alert)
                    }
                )
            }
        }

        // TAB 3: Unduh Laporan Satu Persatu dan Rekapitulasi Periode
        if (supervisorTab == 3) {
            val now = System.currentTimeMillis()
            val filteredAlerts = when (selectedPeriodIndex) {
                1 -> allAlerts.filter { (now - it.activatedAt) <= 86400000L } // Hari ini
                2 -> allAlerts.filter { (now - it.activatedAt) <= 7 * 86400000L } // 7 hari terakhir
                3 -> allAlerts.filter { (now - it.activatedAt) <= 30 * 86400000L } // Bulan ini
                4 -> allAlerts.filter { !it.isDrill } // Kasus nyata
                5 -> allAlerts.filter { it.isDrill } // Simulasi Drill
                else -> allAlerts
            }

            // Period Selector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHospitalCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CodeBlueAccent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = CodeBlueAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pilih Periode Laporan Rekapitulasi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(periodOptions) { index, option ->
                                val isSelected = selectedPeriodIndex == index
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPeriodIndex = index },
                                    label = {
                                        Text(
                                            option,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CodeBlueAccent,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF0F1D2C),
                                        labelColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Summary Card for Selected Period
            item {
                val arrivalsInPeriod = filteredAlerts.filter { it.firstArrivalAt != null }
                val compliantInPeriod = filteredAlerts.count {
                    it.firstArrivalAt != null && ((it.firstArrivalAt - it.activatedAt) / 1000) <= 300
                }
                val avgResponseInPeriod = if (arrivalsInPeriod.isNotEmpty()) {
                    arrivalsInPeriod.map { ((it.firstArrivalAt!! - it.activatedAt) / 1000).toInt() }.average().toInt()
                } else 0
                val pctInPeriod = if (arrivalsInPeriod.isNotEmpty()) (compliantInPeriod * 100) / arrivalsInPeriod.size else 100
                val roscInPeriod = filteredAlerts.count { it.outcome?.contains("ROSC", ignoreCase = true) == true }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1929)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Statistik Mutu: ${periodOptions[selectedPeriodIndex]}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (pctInPeriod >= 80) EmergencySuccess else EmergencyWarning
                            ) {
                                Text(
                                    text = "$pctInPeriod% Sesuai SLA (< 5m)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Insiden", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("${filteredAlerts.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column {
                                Text("Rata-rata Respon", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("${avgResponseInPeriod / 60}m ${avgResponseInPeriod % 60}d", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CodeBlueAccent)
                            }
                            Column {
                                Text("Respon < 5 Menit", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("$compliantInPeriod kasus", fontSize = 16.sp, fontWeight = FontWeight.Black, color = EmergencySuccess)
                            }
                            Column {
                                Text("ROSC Berhasil", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("$roscInPeriod kasus", fontSize = 16.sp, fontWeight = FontWeight.Black, color = EmergencySuccess)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Button to download recap report
                        Button(
                            onClick = {
                                onDownloadRecapReport(filteredAlerts, periodOptions[selectedPeriodIndex])
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("download_period_recap_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmergencySuccess,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "UNDUH REKAPAN CSV (${filteredAlerts.size} LAPORAN)",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // List of Reports in Period
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Unduh Laporan Satu Persatu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${filteredAlerts.size} Berkas",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            if (filteredAlerts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                    ) {
                        Text(
                            text = "Tidak ada laporan pada periode yang dipilih.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(filteredAlerts) { alert ->
                    val isExpanded = expandedAlertId == alert.id
                    IncidentReportItem(
                        alert = alert,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedAlertId = if (isExpanded) null else alert.id
                        },
                        onDownloadReport = {
                            onDownloadIndividualReport(alert)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterMemberCard(
    responder: ResponderEntity,
    onToggleDuty: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("roster_member_card_${responder.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (responder.isOnDuty) DarkHospitalCard else Color(0xFF141A22)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (responder.isOnDuty) Color(0xFF223E5E) else Color(0xFF27313D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (responder.isOnDuty) EmergencySuccess else Color(0xFF475569),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = responder.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = responder.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        if (responder.isLeader) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD97706)
                            ) {
                                Text(
                                    text = "👑 LEADER",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (responder.isOnDuty) EmergencySuccess else Color(0xFF334155)
                        ) {
                            Text(
                                text = if (responder.isOnDuty) "ON-DUTY" else "LEPAS DINAS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = responder.roleTitle,
                        fontSize = 11.sp,
                        color = CodeBlueAccent
                    )
                    Text(
                        text = "📍 ${responder.currentBuilding}, Lt.${responder.currentFloor} • 📞 ${responder.phone}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Switch to toggle duty
            Switch(
                checked = responder.isOnDuty,
                onCheckedChange = { onToggleDuty() },
                modifier = Modifier.testTag("duty_switch_${responder.id}"),
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

@Composable
private fun RoomLinkCard(
    room: HospitalRoom,
    isCurrentBound: Boolean,
    onCopyLink: () -> Unit,
    onBindThisDevice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("room_link_card_${room.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentBound) Color(0xFF0F2B48) else DarkHospitalCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentBound) CodeBlueAccent else Color(0xFF22364E)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = room.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        if (isCurrentBound) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CodeBlueAccent
                            ) {
                                Text(
                                    text = "TERTAUT DI SINI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${room.building}, Lantai ${room.floor} • Token: ${room.id}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = room.id,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CodeBlueAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = room.accessLink,
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyLink,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin Link", fontSize = 11.sp)
                }

                Button(
                    onClick = onBindThisDevice,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentBound) EmergencySuccess else CodeBluePrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isCurrentBound) "Aktif di Tab Ruangan" else "Tautkan ke Sini", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(subtitle, fontSize = 10.sp, color = accentColor)
        }
    }
}

@Composable
private fun IncidentReportItem(
    alert: CodeBlueAlertEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDownloadReport: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("incident_report_card_${alert.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (alert.status) {
                            AlertStatus.RESOLVED.name -> EmergencySuccess
                            AlertStatus.CANCELLED.name -> Color.Gray
                            else -> EmergencyRed
                        }
                    ) {
                        Text(
                            text = alert.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.id,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDownloadReport,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Unduh Laporan",
                            tint = CodeBlueAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${alert.roomName} (${alert.building}, Lt.${alert.floor})",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White
            )

            // Response time badge (Target < 5 Menit)
            if (alert.firstArrivalAt != null) {
                val respSecs = ((alert.firstArrivalAt - alert.activatedAt) / 1000).toInt()
                val isUnder5Min = respSecs <= 300
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Waktu Respon Tim: ${respSecs / 60}m ${respSecs % 60}d",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnder5Min) EmergencySuccess else EmergencyRedBright
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUnder5Min) "• Target < 5m Tercapai ✅" else "• Melebihi 5m ⚠️",
                        fontSize = 10.sp,
                        color = if (isUnder5Min) EmergencySuccess else EmergencyRedBright
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .border(1.dp, Color(0xFF22364E), RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1824))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Detail Klinis & Penatalaksanaan:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CodeBlueAccent)
                    Text("• Pasien: ${alert.patientCategory} (${alert.conditionSummary})", fontSize = 11.sp, color = Color.White)
                    Text("• Team Leader: ${alert.leadDoctor}", fontSize = 11.sp, color = Color.White)
                    if (alert.deactivatedBy != null) {
                        Text("• Dinonaktifkan Oleh: ${alert.deactivatedBy}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }
                    Text("• Total Durasi CPR: ${alert.cprDurationSeconds / 60}m ${alert.cprDurationSeconds % 60}d", fontSize = 11.sp, color = Color.White)
                    Text("• Defibrilasi / Shock: ${alert.totalShocks} kali", fontSize = 11.sp, color = Color.White)
                    Text("• Dosis Epinefrin: ${alert.totalEpiDoses} ampul", fontSize = 11.sp, color = Color.White)

                    if (alert.outcome != null) {
                        Text("• Hasil Akhir: ${alert.outcome}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmergencySuccess)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onDownloadReport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A5F),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unduh Berkas Laporan (${alert.id})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
