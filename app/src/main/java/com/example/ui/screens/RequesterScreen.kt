package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AlertStatus
import com.example.data.CodeBlueAlertEntity
import com.example.data.HospitalData
import com.example.data.HospitalRoom
import com.example.data.PatientCategory
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequesterScreen(
    boundRoom: HospitalRoom,
    activeAlert: CodeBlueAlertEntity?,
    responders: List<ResponderEntity>,
    currentTimestamp: Long,
    onTriggerCodeBlue: (patientType: PatientCategory, condition: String, needsDefib: Boolean, cprStarted: Boolean, bed: String) -> Unit,
    onMarkTeamArrivedAndDeactivate: () -> Unit,
    onCancelAlert: (String) -> Unit,
    onSwitchRoomLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(PatientCategory.ADULT) }
    var needsDefibrillator by remember { mutableStateOf(true) }
    var cprStartedLocally by remember { mutableStateOf(true) }
    var bedNumber by remember { mutableStateOf(boundRoom.defaultBed) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSwitchRoomDialog by remember { mutableStateOf(false) }
    var inputRoomLinkOrToken by remember { mutableStateOf(boundRoom.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("requester_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Room Identity Header (Linked Account / Tanpa Dropdown)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bound_room_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkHospitalCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF223A5E))
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
                                .size(42.dp)
                                .background(CodeBluePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ruangan Terhubung",
                                tint = CodeBlueAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E3A5F)
                            ) {
                                Text(
                                    text = "AKUN RUANGAN TERHUBUNG: ${boundRoom.id}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CodeBlueAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = boundRoom.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "${boundRoom.building}, Lantai ${boundRoom.floor}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Button to switch or enter link (for admin / transfer testing)
                    IconButton(
                        onClick = { showSwitchRoomDialog = true },
                        modifier = Modifier.testTag("switch_room_link_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Ganti Link Ruangan",
                            tint = CodeBlueAccent
                        )
                    }
                }
            }
        }

        val isAlertActive = activeAlert != null &&
                activeAlert.status != AlertStatus.RESOLVED.name &&
                activeAlert.status != AlertStatus.CANCELLED.name

        if (isAlertActive && activeAlert != null) {
            // ================== ACTIVE EMERGENCY STATUS VIEW ==================

            // Calculate live response time
            val elapsedSecs = if (activeAlert.firstArrivalAt != null) {
                ((activeAlert.firstArrivalAt - activeAlert.activatedAt) / 1000).toInt()
            } else {
                ((currentTimestamp - activeAlert.activatedAt) / 1000).toInt().coerceAtLeast(0)
            }

            val mins = elapsedSecs / 60
            val secs = elapsedSecs % 60
            val isUnder5Min = elapsedSecs <= 300 // Target 5 Menit SLA

            // High Visibility Response Time Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, if (isUnder5Min) EmergencyRedBright else Color(0xFFEF4444), RoundedCornerShape(16.dp))
                        .testTag("live_response_time_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0A0A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmergencyRed
                            ) {
                                Text(
                                    text = if (activeAlert.firstArrivalAt != null) "TIM TIBA DI LOKASI" else "🚨 CODE BLUE BERJALAN",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salah Panggil", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Digital Stopwatch
                        Text(
                            text = "STOPWATCH WAKTU RESPON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isUnder5Min) Color.White else EmergencyRedBright
                        )

                        // SLA Indicator: Target < 5 Menit
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUnder5Min) EmergencySuccess.copy(alpha = 0.25f) else EmergencyRed.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isUnder5Min) EmergencySuccess else EmergencyRedBright
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isUnder5Min) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isUnder5Min) EmergencySuccess else EmergencyRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUnder5Min) "Target Respon: < 5 Menit (Memenuhi Standar RS)" else "⚠️ PERINGATAN: RESPON MELEBIHI 5 MENIT!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnder5Min) EmergencySuccess else EmergencyRedBright
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PRIMARY ACTION: Deactivate / Confirm Arrival
                        Button(
                            onClick = onMarkTeamArrivedAndDeactivate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("deactivate_on_arrival_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmergencySuccess,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TIM CODE BLUE SUDAH TIBA (HENTIKAN ALARM)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Real-Time Hospital Map with responding personnel
            item {
                Text(
                    text = "Pelacakan Posisi Tim Menuju ${activeAlert.roomName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                HospitalMapComponent(
                    currentFloor = activeAlert.floor,
                    onFloorSelected = {},
                    activeAlert = activeAlert,
                    responders = responders,
                    highlightRoomName = activeAlert.roomName
                )
            }

            // Live Responders List
            item {
                Text(
                    text = "Daftar Tim yang Sedang Menuju ke Sini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            val onDutyResponders = responders.filter { it.isOnDuty && (it.assignedAlertId == activeAlert.id || it.state != "IDLE") }
            items(if (onDutyResponders.isNotEmpty()) onDutyResponders else responders.filter { it.isOnDuty }) { resp ->
                ResponderEtaCard(responder = resp)
            }

            // Instructions while waiting
            item {
                BedsideChecklistCard()
            }

        } else {
            // ================== STANDBY: 1-TAP ACTIVATION VIEW ==================
            // Hero Visual Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.hospital_banner),
                            contentDescription = "Hospital Emergency Center",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmergencyRed,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "SISTEM AKTIVASI INSTAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Aktivasi Code Blue RS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Respons darurat terhubung langsung ke ponsel Tim Medis",
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }

            // 1-TAP GIANT ACTIVATION BUTTON
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHospitalCard),
                    border = androidx.compose.foundation.BorderStroke(2.dp, EmergencyRed)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("instant_activate_codeblue_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmergencyRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "AKTIFKAN CODE BLUE",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Lokasi: ${boundRoom.name} (${boundRoom.defaultBed})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFEBEE)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚡ Cukup tekan 1 tombol di atas. Alarm akan berbunyi di ponsel seluruh Tim Code Blue yang bertugas.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Quick Parameters (Patient Category, Defib, Bed)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Parameter Klinis Tambahan (Opsional):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PatientCategory.values().forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.label, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("cat_chip_${cat.name}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmergencyRed,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF0F1B2B),
                                        labelColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bed Number TextField
                        OutlinedTextField(
                            value = bedNumber,
                            onValueChange = { bedNumber = it },
                            label = { Text("Nomor Bed / Lokasi Pasien") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bed_number_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CodeBlueAccent,
                                unfocusedBorderColor = Color(0xFF334E68),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Defib & CPR Checkboxes
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = needsDefibrillator,
                                onCheckedChange = { needsDefibrillator = it }
                            )
                            Text(
                                text = "Bawa Mesin Defibrilator / AED Segera!",
                                fontWeight = FontWeight.Bold,
                                color = if (needsDefibrillator) EmergencyRedBright else Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = cprStartedLocally,
                                onCheckedChange = { cprStartedLocally = it }
                            )
                            Text(
                                text = "Kompresi dada (CPR) sudah dimulai oleh staf ruangan",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = EmergencyRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pancarkan CODE BLUE?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Alarm akan berbunyi di ponsel seluruh Tim Code Blue yang sedang berjaga.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📍 Ruangan: ${boundRoom.name}", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("🏢 ${boundRoom.building}, Lantai ${boundRoom.floor}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("🛏️ Bed: $bedNumber", fontSize = 12.sp, color = Color.White)
                            Text("👤 Kategori: ${selectedCategory.label}", fontSize = 12.sp, color = Color.White)
                            Text("⏱️ Target Respon: < 5 Menit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CodeBlueAccent)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onTriggerCodeBlue(
                            selectedCategory,
                            "Henti Jantung / Tidak Sadar",
                            needsDefibrillator,
                            cprStartedLocally,
                            bedNumber
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    modifier = Modifier.testTag("confirm_instant_activate_btn")
                ) {
                    Text("Ya, Aktifkan Sekarang!", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Batal") }
            }
        )
    }

    // Modal: Switch / Enter Room Link (Admin Link Support)
    if (showSwitchRoomDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchRoomDialog = false },
            title = { Text("Tautkan Akun Ruangan", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Masukkan Token Ruangan (misal: RM-IGD-01, RM-ICU-03) atau Tautan Akun Ruangan:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputRoomLinkOrToken,
                        onValueChange = { inputRoomLinkOrToken = it },
                        label = { Text("ID atau Link Ruangan") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CodeBlueAccent,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Pilih Cepat:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        HospitalData.rooms.take(4).forEach { rm ->
                            Text(
                                text = "• ${rm.name} (${rm.id})",
                                fontSize = 11.sp,
                                color = CodeBlueAccent,
                                modifier = Modifier
                                    .clickable { inputRoomLinkOrToken = rm.id }
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSwitchRoomDialog = false
                        onSwitchRoomLink(inputRoomLinkOrToken)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeBluePrimary),
                    modifier = Modifier.testTag("save_room_link_btn")
                ) {
                    Text("Tautkan Ruangan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchRoomDialog = false }) { Text("Batal") }
            }
        )
    }

    // Modal: Cancel Alert
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Panggilan Code Blue?") },
            text = { Text("Hanya gunakan opsi ini jika terjadi kekeliruan tekan (false alarm).") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelAlert("Panggilan dibatalkan oleh staf ruangan")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Batalkan Panggilan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Kembali") }
            }
        )
    }
}

@Composable
private fun ResponderEtaCard(responder: ResponderEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("responder_eta_card_${responder.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            when (responder.state) {
                                "ON_SCENE" -> EmergencyOnScene
                                "RESPONDING" -> EmergencyWarning
                                else -> CodeBluePrimary
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (responder.state == "ON_SCENE") Icons.Default.CheckCircle else Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = responder.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = responder.roleTitle,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (responder.state) {
                    "ON_SCENE" -> EmergencySuccess.copy(alpha = 0.2f)
                    "RESPONDING" -> EmergencyWarning.copy(alpha = 0.2f)
                    else -> Color(0xFF1E293B)
                }
            ) {
                Text(
                    text = when (responder.state) {
                        "ON_SCENE" -> "TIBA DI LOKASI"
                        "RESPONDING" -> "ETA ~${responder.etaSeconds} detik"
                        else -> "SIAGA"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = when (responder.state) {
                        "ON_SCENE" -> EmergencySuccess
                        "RESPONDING" -> Color(0xFFFBBF24)
                        else -> Color(0xFF94A3B8)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BedsideChecklistCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111E2E))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = CodeBlueAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Instruksi Staf Ruangan Sambil Menunggu Tim",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("1. Lanjutkan kompresi dada tanpa jeda (100-120x/menit, kedalaman 5-6 cm).", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            Text("2. Pasang papan resusitasi (cardiac backboard) di bawah punggung pasien.", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            Text("3. Siapkan mesin suction dan pasang sungkup bag-valve-mask ke tabung O2 15 Lpm.", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            Text("4. Buka pintu ruangan lebar-lebar untuk akses troli resusitasi.", fontSize = 11.sp, color = Color(0xFFCBD5E1))
        }
    }
}
