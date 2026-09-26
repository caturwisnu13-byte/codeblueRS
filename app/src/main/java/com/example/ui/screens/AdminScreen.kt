package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HospitalData
import com.example.data.HospitalRoomEntity
import com.example.data.ResponderEntity
import com.example.data.RoleType
import com.example.ui.theme.CodeBlueAccent
import com.example.ui.theme.DarkHospitalCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencySuccess
import com.example.ui.theme.EmergencyWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    rooms: List<HospitalRoomEntity>,
    responders: List<ResponderEntity>,
    onAddRoom: (HospitalRoomEntity) -> Unit,
    onUpdateRoom: (HospitalRoomEntity) -> Unit,
    onDeleteRoom: (String) -> Unit,
    onAddResponder: (ResponderEntity) -> Unit,
    onUpdateResponder: (ResponderEntity) -> Unit,
    onDeleteResponder: (String) -> Unit,
    onToggleLeader: (String) -> Unit,
    onToggleDuty: (String) -> Unit,
    onUpdateAdminPin: (String) -> Unit,
    onExitAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var adminTab by remember { mutableIntStateOf(0) }

    // Dialog states for Room
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var editingRoom by remember { mutableStateOf<HospitalRoomEntity?>(null) }
    var roomToDelete by remember { mutableStateOf<HospitalRoomEntity?>(null) }

    // Dialog states for Responder
    var showAddResponderDialog by remember { mutableStateOf(false) }
    var editingResponder by remember { mutableStateOf<ResponderEntity?>(null) }
    var responderToDelete by remember { mutableStateOf<ResponderEntity?>(null) }
    var isAddingLeaderMode by remember { mutableStateOf(false) }

    // Dialog state for PIN
    var showPinChangeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CodeBlueAccent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(CodeBlueAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Portal Administrator RS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFD97706)
                                ) {
                                    Text(
                                        text = "SUPER ADMIN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Kelola Master Ruangan, Anggota & Leader Tim",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Exit Admin Button
                    Button(
                        onClick = onExitAdmin,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("exit_admin_btn")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keluar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Admin Tabs
        item {
            val leadersCount = responders.count { it.isLeader }
            val membersCount = responders.count { !it.isLeader }

            PrimaryTabRow(
                selectedTabIndex = adminTab,
                containerColor = DarkHospitalCard,
                contentColor = CodeBlueAccent,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = adminTab == 0,
                    onClick = { adminTab = 0 },
                    text = { Text("Ruangan (${rooms.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_rooms")
                )
                Tab(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    text = { Text("Anggota ($membersCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_members")
                )
                Tab(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    text = { Text("Leader ($leadersCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_leaders")
                )
                Tab(
                    selected = adminTab == 3,
                    onClick = { adminTab = 3 },
                    text = { Text("Keamanan", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_security")
                )
            }
        }

        // TAB 0: Master Ruangan Rumah Sakit
        if (adminTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daftar Master Ruangan RS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ruangan yang terdaftar otomatis tersedia di tablet kamar",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = { showAddRoomDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CodeBlueAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_add_room")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            if (rooms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                    ) {
                        Text(
                            text = "Belum ada ruangan yang terdaftar. Klik 'Tambah' untuk mendaftarkan ruangan rumah sakit.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(rooms) { room ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (room.isEmergencyPriority) EmergencyRed else CodeBlueAccent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MeetingRoom,
                                        contentDescription = null,
                                        tint = if (room.isEmergencyPriority) Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = room.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        if (room.isEmergencyPriority) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = EmergencyRed
                                            ) {
                                                Text(
                                                    text = "PRIORITAS",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Kode: ${room.id} • ${room.building}, Lt.${room.floor} • Default: ${room.defaultBed}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { editingRoom = room },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CodeBlueAccent, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { roomToDelete = room },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = EmergencyRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: Master Anggota Tim Code Blue
        if (adminTab == 1) {
            val members = responders.filter { !it.isLeader }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daftar Anggota Tim Code Blue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Petugas langsung memilih namanya di aplikasi tim jaga",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            isAddingLeaderMode = false
                            showAddResponderDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CodeBlueAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_add_member")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            items(members) { resp ->
                ResponderAdminCard(
                    responder = resp,
                    onEdit = { editingResponder = resp },
                    onDelete = { responderToDelete = resp },
                    onToggleLeader = { onToggleLeader(resp.id) },
                    onToggleDuty = { onToggleDuty(resp.id) }
                )
            }
        }

        // TAB 2: Master Leader Tim Code Blue (Dokter Penanggung Jawab)
        if (adminTab == 2) {
            val leaders = responders.filter { it.isLeader }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daftar Leader Tim (Dokter PJ Resusitasi)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Memimpin resusitasi ACLS & instruksi defibrilasi/obat",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            isAddingLeaderMode = true
                            showAddResponderDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_add_leader")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Leader", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            if (leaders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard)
                    ) {
                        Text(
                            text = "Belum ada Leader Tim yang ditandai. Klik 'Tambah Leader' atau tandai bintang (⭐) pada anggota di tab Anggota.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(leaders) { resp ->
                    ResponderAdminCard(
                        responder = resp,
                        onEdit = { editingResponder = resp },
                        onDelete = { responderToDelete = resp },
                        onToggleLeader = { onToggleLeader(resp.id) },
                        onToggleDuty = { onToggleDuty(resp.id) }
                    )
                }
            }
        }

        // TAB 3: Keamanan & Pengaturan PIN Admin
        if (adminTab == 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHospitalCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = CodeBlueAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Keamanan Portal Admin Rumah Sakit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "Portal ini dipisahkan dari staf ruangan biasa, tim perawat jaga, dan pengawas. Hanya personel IT / Tim Mutu Rumah Sakit yang memiliki akses untuk mengubah master ruangan dan tim.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )

                        Text(
                            text = "PIN Master Admin Default: 1199",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyWarning
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showPinChangeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CodeBlueAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ubah PIN Master Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add / Edit Room
    if (showAddRoomDialog || editingRoom != null) {
        val isEditing = editingRoom != null
        val target = editingRoom
        var roomId by remember { mutableStateOf(target?.id ?: "RM-${System.currentTimeMillis() % 1000}") }
        var roomName by remember { mutableStateOf(target?.name ?: "") }
        var building by remember { mutableStateOf(target?.building ?: HospitalData.buildings.first()) }
        var floor by remember { mutableIntStateOf(target?.floor ?: 1) }
        var defaultBed by remember { mutableStateOf(target?.defaultBed ?: "Bed 1") }
        var isEmergencyPriority by remember { mutableStateOf(target?.isEmergencyPriority ?: false) }

        AlertDialog(
            onDismissRequest = {
                showAddRoomDialog = false
                editingRoom = null
            },
            title = {
                Text(if (isEditing) "Edit Data Ruangan RS" else "Tambah Ruangan RS Baru", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roomId,
                        onValueChange = { roomId = it },
                        label = { Text("Kode Ruangan (ID)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isEditing
                    )
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Nama Ruangan (contoh: ICU Bed 1, Melati 203)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text("Nama Gedung") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = floor.toString(),
                            onValueChange = { floor = it.toIntOrNull() ?: 1 },
                            label = { Text("Lantai") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = defaultBed,
                            onValueChange = { defaultBed = it },
                            label = { Text("Nomor Bed") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isEmergencyPriority = !isEmergencyPriority }
                    ) {
                        Checkbox(
                            checked = isEmergencyPriority,
                            onCheckedChange = { isEmergencyPriority = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ruang Prioritas Gawat Darurat (ICU/IGD)", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (roomName.isBlank()) {
                            Toast.makeText(context, "Nama ruangan wajib diisi!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val entity = HospitalRoomEntity(
                            id = roomId.trim(),
                            name = roomName.trim(),
                            building = building.trim(),
                            floor = floor,
                            defaultBed = defaultBed.trim(),
                            isEmergencyPriority = isEmergencyPriority,
                            isActive = true
                        )
                        if (isEditing) {
                            onUpdateRoom(entity)
                            Toast.makeText(context, "Ruangan ${entity.name} berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        } else {
                            onAddRoom(entity)
                            Toast.makeText(context, "Ruangan ${entity.name} berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        }
                        showAddRoomDialog = false
                        editingRoom = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeBlueAccent, contentColor = Color.Black)
                ) {
                    Text(if (isEditing) "Simpan Perubahan" else "Tambah Ruangan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddRoomDialog = false
                    editingRoom = null
                }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Delete Room Confirmation
    roomToDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            title = { Text("Hapus Ruangan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus ruangan '${room.name}' (${room.id}) dari database?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRoom(room.id)
                        Toast.makeText(context, "Ruangan ${room.name} dihapus", Toast.LENGTH_SHORT).show()
                        roomToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { roomToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Add / Edit Responder
    if (showAddResponderDialog || editingResponder != null) {
        val isEditing = editingResponder != null
        val target = editingResponder
        var respId by remember { mutableStateOf(target?.id ?: "resp-${System.currentTimeMillis() % 1000}") }
        var name by remember { mutableStateOf(target?.name ?: "") }
        var isLeader by remember { mutableStateOf(target?.isLeader ?: isAddingLeaderMode) }
        var roleTitle by remember {
            mutableStateOf(
                target?.roleTitle ?: if (isAddingLeaderMode) "Dokter Penanggung Jawab Resusitasi" else "Perawat Mahir ICU"
            )
        }
        var phone by remember { mutableStateOf(target?.phone ?: "0812-") }
        var building by remember { mutableStateOf(target?.currentBuilding ?: "Gedung Bedah & Emergensi (GBE)") }
        var floor by remember { mutableIntStateOf(target?.currentFloor ?: 1) }

        AlertDialog(
            onDismissRequest = {
                showAddResponderDialog = false
                editingResponder = null
            },
            title = {
                Text(
                    text = if (isEditing) "Edit Data Petugas" else if (isLeader) "Tambah Dokter PJ / Leader Baru" else "Tambah Anggota Tim Baru",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap & Gelar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roleTitle,
                        onValueChange = { roleTitle = it },
                        label = { Text("Peran / Jabatan Klinis") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Nomor HP / Pager / Radio HT") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = building,
                            onValueChange = { building = it },
                            label = { Text("Gedung Standby") },
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = floor.toString(),
                            onValueChange = { floor = it.toIntOrNull() ?: 1 },
                            label = { Text("Lt.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isLeader = !isLeader }
                    ) {
                        Checkbox(
                            checked = isLeader,
                            onCheckedChange = { isLeader = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Jadikan Dokter PJ / Team Leader Code Blue ⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Nama petugas wajib diisi!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val entity = ResponderEntity(
                            id = respId.trim(),
                            name = name.trim(),
                            role = if (isLeader) RoleType.LEADER_DOCTOR.name else RoleType.AIRWAY_NURSE.name,
                            roleTitle = roleTitle.trim(),
                            phone = phone.trim(),
                            currentBuilding = building.trim(),
                            currentFloor = floor,
                            isLeader = isLeader,
                            isOnDuty = target?.isOnDuty ?: true,
                            state = target?.state ?: "IDLE"
                        )
                        if (isEditing) {
                            onUpdateResponder(entity)
                            Toast.makeText(context, "Petugas ${entity.name} berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        } else {
                            onAddResponder(entity)
                            Toast.makeText(context, "Petugas ${entity.name} berhasil didaftarkan", Toast.LENGTH_SHORT).show()
                        }
                        showAddResponderDialog = false
                        editingResponder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeBlueAccent, contentColor = Color.Black)
                ) {
                    Text(if (isEditing) "Simpan" else "Daftarkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddResponderDialog = false
                    editingResponder = null
                }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Delete Responder Confirmation
    responderToDelete?.let { resp ->
        AlertDialog(
            onDismissRequest = { responderToDelete = null },
            title = { Text("Hapus Petugas?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus petugas '${resp.name}' (${resp.roleTitle}) dari daftar Code Blue?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteResponder(resp.id)
                        Toast.makeText(context, "Petugas ${resp.name} dihapus", Toast.LENGTH_SHORT).show()
                        responderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { responderToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Change Master Admin PIN
    if (showPinChangeDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Ubah PIN Master Admin", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PIN ini digunakan untuk mengunci dan membuka Portal Admin.", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) newPin = it },
                        label = { Text("PIN Baru (min. 4 angka)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6) confirmPin = it },
                        label = { Text("Konfirmasi PIN Baru") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            Toast.makeText(context, "PIN minimal 4 angka!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            Toast.makeText(context, "Konfirmasi PIN tidak cocok!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onUpdateAdminPin(newPin)
                        Toast.makeText(context, "PIN Master Admin berhasil diubah!", Toast.LENGTH_SHORT).show()
                        showPinChangeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeBlueAccent, contentColor = Color.Black)
                ) {
                    Text("Simpan PIN Baru", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ResponderAdminCard(
    responder: ResponderEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleLeader: () -> Unit,
    onToggleDuty: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_responder_card_${responder.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (responder.isOnDuty) DarkHospitalCard else Color(0xFF141A22)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (responder.isLeader) Color(0xFFD97706) else if (responder.isOnDuty) Color(0xFF223E5E) else Color(0xFF27313D)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (responder.isLeader) Color(0xFFD97706) else if (responder.isOnDuty) EmergencySuccess else Color(0xFF475569),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (responder.isLeader) "👑" else responder.name.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
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
                                        text = "LEADER",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = responder.roleTitle,
                            fontSize = 11.sp,
                            color = CodeBlueAccent
                        )
                        Text(
                            text = "📞 ${responder.phone} • 📍 ${responder.currentBuilding}, Lt.${responder.currentFloor}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Actions: Toggle Duty, Edit, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleLeader,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (responder.isLeader) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Toggle Leader",
                            tint = if (responder.isLeader) Color(0xFFF59E0B) else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CodeBlueAccent, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = EmergencyRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duty status bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (responder.isOnDuty) EmergencySuccess.copy(alpha = 0.2f) else Color(0xFF334155)
                ) {
                    Text(
                        text = if (responder.isOnDuty) "● SEDANG BERTUGAS (ON-DUTY)" else "○ LEPAS DINAS (OFF-DUTY)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (responder.isOnDuty) EmergencySuccess else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Switch(
                    checked = responder.isOnDuty,
                    onCheckedChange = { onToggleDuty() },
                    modifier = Modifier.testTag("admin_duty_switch_${responder.id}"),
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
