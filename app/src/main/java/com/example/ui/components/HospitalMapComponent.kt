package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeBlueAlertEntity
import com.example.data.HospitalData
import com.example.data.HospitalRoom
import com.example.data.ResponderEntity
import com.example.ui.theme.CodeBlueAccent
import com.example.ui.theme.CodeBluePrimary
import com.example.ui.theme.DarkHospitalBg
import com.example.ui.theme.DarkHospitalCard
import com.example.ui.theme.EmergencyOnScene
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.EmergencyWarning

@Composable
fun HospitalMapComponent(
    currentFloor: Int,
    onFloorSelected: (Int) -> Unit,
    activeAlert: CodeBlueAlertEntity?,
    responders: List<ResponderEntity>,
    modifier: Modifier = Modifier,
    highlightRoomName: String? = null
) {
    var selectedEntityInfo by remember { mutableStateOf<String?>(null) }

    // Pulsing animation for active emergency room
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hospital_map_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with Floor Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Hospital Floor Radar",
                        tint = CodeBlueAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Denah Real-Time Lantai $currentFloor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..4).forEach { floor ->
                        FilterChip(
                            selected = currentFloor == floor,
                            onClick = { onFloorSelected(floor) },
                            label = { Text("Lt.$floor", fontSize = 11.sp) },
                            modifier = Modifier.testTag("floor_chip_$floor"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CodeBlueAccent,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkHospitalBg,
                                labelColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Interactive Floor Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkHospitalBg)
                    .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp))
            ) {
                val canvasWidth = maxWidth
                val canvasHeight = maxHeight

                val roomsOnFloor = remember(currentFloor) {
                    HospitalData.rooms.filter { it.floor == currentFloor }
                }

                val floorResponders = remember(responders, currentFloor) {
                    responders.filter { it.currentFloor == currentFloor }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Architectural Grid & Corridors (Hallways)
                    val gridPaint = Color(0xFF112233)
                    val corridorColor = Color(0xFF15263A)

                    // Background grid
                    var gx = 0f
                    while (gx < w) {
                        drawLine(gridPaint, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                        gx += 40f
                    }
                    var gy = 0f
                    while (gy < h) {
                        drawLine(gridPaint, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                        gy += 40f
                    }

                    // Main Corridor (Horizontal central spine)
                    drawRect(
                        color = corridorColor,
                        topLeft = Offset(w * 0.1f, h * 0.42f),
                        size = Size(w * 0.8f, h * 0.16f)
                    )
                    // North/South Corridors
                    drawRect(
                        color = corridorColor,
                        topLeft = Offset(w * 0.44f, h * 0.1f),
                        size = Size(w * 0.12f, h * 0.8f)
                    )

                    // Corridor navigation guide dashed center line
                    val corridorDash = Path().apply {
                        moveTo(w * 0.15f, h * 0.50f)
                        lineTo(w * 0.85f, h * 0.50f)
                        moveTo(w * 0.50f, h * 0.15f)
                        lineTo(w * 0.50f, h * 0.85f)
                    }
                    drawPath(
                        corridorDash,
                        color = Color(0xFF1E3A5F),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )

                    // Elevator / Lift Core
                    drawRoundRect(
                        color = Color(0xFF2C3E50),
                        topLeft = Offset(w * 0.45f, h * 0.05f),
                        size = Size(w * 0.10f, h * 0.08f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // 2. Draw Rooms
                    roomsOnFloor.forEach { room ->
                        val rx = room.mapX * w
                        val ry = room.mapY * h
                        val isTarget = (activeAlert != null && activeAlert.floor == currentFloor &&
                                activeAlert.roomName.equals(room.name, ignoreCase = true)) ||
                                (highlightRoomName != null && highlightRoomName.equals(room.name, ignoreCase = true))

                        val roomColor = if (isTarget) EmergencyRed.copy(alpha = 0.25f) else Color(0xFF1A2D42)
                        val roomBorder = if (isTarget) EmergencyRedBright else Color(0xFF2D4B6E)

                        drawRoundRect(
                            color = roomColor,
                            topLeft = Offset(rx - 38f, ry - 24f),
                            size = Size(76f, 48f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = roomBorder,
                            topLeft = Offset(rx - 38f, ry - 24f),
                            size = Size(76f, 48f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = if (isTarget) 3f else 1.5f)
                        )

                        // Room Door notch
                        drawCircle(
                            color = if (isTarget) EmergencyRedBright else Color(0xFF00E5FF),
                            radius = 3.5f,
                            center = Offset(rx, ry + 24f)
                        )
                    }

                    // 3. Draw Emergency Beacon / Ripple on active alert room
                    if (activeAlert != null && activeAlert.floor == currentFloor) {
                        val ax = activeAlert.roomPosX * w
                        val ay = activeAlert.roomPosY * h

                        // Expanding ripple
                        drawCircle(
                            color = EmergencyRedBright.copy(alpha = pulseAlpha),
                            radius = pulseRadius,
                            center = Offset(ax, ay)
                        )
                        // Second concentric ring
                        drawCircle(
                            color = EmergencyRed.copy(alpha = 0.9f),
                            radius = 16f,
                            center = Offset(ax, ay)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = Offset(ax, ay)
                        )
                    }

                    // 4. Draw Responders moving in real time
                    floorResponders.forEach { resp ->
                        val px = resp.posX * w
                        val py = resp.posY * h

                        val isResponding = resp.state == "RESPONDING"
                        val isOnScene = resp.state == "ON_SCENE"

                        val respColor = when {
                            isOnScene -> EmergencyOnScene
                            isResponding -> EmergencyWarning
                            else -> CodeBlueAccent
                        }

                        // Draw path trajectory line to target if responding
                        if (isResponding && activeAlert != null && activeAlert.floor == currentFloor) {
                            val tx = activeAlert.roomPosX * w
                            val ty = activeAlert.roomPosY * h
                            drawLine(
                                color = EmergencyWarning.copy(alpha = 0.6f),
                                start = Offset(px, py),
                                end = Offset(tx, ty),
                                strokeWidth = 2.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        }

                        // Responder avatar circle
                        drawCircle(
                            color = respColor,
                            radius = 11f,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 11f,
                            center = Offset(px, py),
                            style = Stroke(width = 2f)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 4f,
                            center = Offset(px, py)
                        )
                    }
                }

                // Interactive info badge overlay
                if (activeAlert != null && activeAlert.floor == currentFloor) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = EmergencyRed.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "Active Alert Room",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOKASI CODE BLUE: ${activeAlert.roomName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmergencyRedBright, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lokasi Pasien", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmergencyWarning, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tim Menuju Lokasi", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmergencyOnScene, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tiba di Lokasi", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CodeBlueAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Personel Siaga", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }
            }
        }
    }
}
