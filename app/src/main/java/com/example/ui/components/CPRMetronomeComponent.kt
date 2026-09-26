package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBlueAccent
import com.example.ui.theme.DarkHospitalCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.EmergencySuccess
import com.example.ui.theme.EmergencyWarning

@Composable
fun CPRMetronomeComponent(
    isCprRunning: Boolean,
    cycleSecondsRemaining: Int,
    totalSecondsElapsed: Int,
    cycleNumber: Int,
    epinephrineTimerRemaining: Int,
    totalShocks: Int,
    totalEpiDoses: Int,
    isMetronomeSoundOn: Boolean,
    metronomeTick: Long,
    onStartPauseCpr: () -> Unit,
    onNextCycle: () -> Unit,
    onToggleMetronomeSound: () -> Unit,
    onAdministerEpinephrine: () -> Unit,
    onTriggerShock: () -> Unit,
    onRoscAchieved: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Heartbeat scale animation on each metronome tick
    val heartScale = remember { Animatable(1.0f) }

    LaunchedEffect(metronomeTick) {
        if (isCprRunning && metronomeTick > 0) {
            heartScale.animateTo(1.30f, animationSpec = tween(70, easing = LinearEasing))
            heartScale.animateTo(1.0f, animationSpec = tween(130, easing = FastOutLinearInEasing))
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cpr_metronome_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHospitalCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Resuscitation Assistant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isCprRunning) EmergencyRedBright else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Asisten Resusitasi CPR (AHA/ERC)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onToggleMetronomeSound,
                    modifier = Modifier.testTag("toggle_metronome_btn")
                ) {
                    Icon(
                        imageVector = if (isMetronomeSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Metronome Sound",
                        tint = if (isMetronomeSoundOn) CodeBlueAccent else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center Ring: 2-Minute CPR Cycle Progress & Pulsing Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Timer Box
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = (120 - cycleSecondsRemaining) / 120f
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(130.dp),
                        color = if (cycleSecondsRemaining <= 15) EmergencyRedBright else CodeBlueAccent,
                        trackColor = Color(0xFF22364E),
                        strokeWidth = 10.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "CPR Metronome Pulse",
                            tint = if (isCprRunning) EmergencyRedBright else Color.Gray,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(heartScale.value)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val mins = cycleSecondsRemaining / 60
                        val secs = cycleSecondsRemaining % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Siklus ke-$cycleNumber",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Resuscitation Metrics & Totals
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total CPR Duration
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF132235),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Total Durasi CPR", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            val totMins = totalSecondsElapsed / 60
                            val totSecs = totalSecondsElapsed % 60
                            Text(
                                text = String.format("%02d:%02d", totMins, totSecs),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Epinephrine countdown
                    val epiMins = epinephrineTimerRemaining / 60
                    val epiSecs = epinephrineTimerRemaining % 60
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (epinephrineTimerRemaining <= 30) EmergencyRed.copy(alpha = 0.3f) else Color(0xFF132235),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Timer Epinefrin (3-5m)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${String.format("%02d:%02d", epiMins, epiSecs)} (Dosis: $totalEpiDoses)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (epinephrineTimerRemaining <= 30) EmergencyRedBright else Color(0xFFFBBF24)
                            )
                        }
                    }

                    // Defibrillation count
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF132235),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Shock Defib", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$totalShocks Kali",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CodeBlueAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary CPR Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartPauseCpr,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cpr_start_pause_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCprRunning) EmergencyWarning else CodeBlueAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isCprRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCprRunning) "Jeda CPR" else "Mulai CPR (110 BPM)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onNextCycle,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cpr_next_cycle_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ganti Kompresor (2m)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Resuscitation Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Epinephrine 1mg
                FilledTonalButton(
                    onClick = onAdministerEpinephrine,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("administer_epi_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF854D0E),
                        contentColor = Color(0xFFFEF08A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Epinefrin 1mg", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Defibrillator Shock
                FilledTonalButton(
                    onClick = onTriggerShock,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("defib_shock_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF991B1B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shock 200J", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // ROSC achieved
                FilledTonalButton(
                    onClick = onRoscAchieved,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rosc_achieved_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = EmergencySuccess,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ROSC!", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
