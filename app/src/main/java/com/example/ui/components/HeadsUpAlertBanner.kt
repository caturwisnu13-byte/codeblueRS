package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.HeadsUpAlert
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright

@Composable
fun HeadsUpAlertBanner(
    headsUpAlert: HeadsUpAlert?,
    isSirenActive: Boolean,
    canSilenceSiren: Boolean = true,
    onOpenAlertDetails: (String) -> Unit,
    onSilenceSiren: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = headsUpAlert != null && headsUpAlert.showBanner,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (headsUpAlert != null) {
            val alert = headsUpAlert.alert
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .border(2.dp, EmergencyRedBright, RoundedCornerShape(14.dp))
                    .testTag("heads_up_emergency_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "Alert Siren",
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (alert.isDrill) "🚨 SIMULASI CODE BLUE AKTIF" else "🚨 CODE BLUE DARURAT!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${alert.roomName} (${alert.building}, Lt. ${alert.floor})",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFEBEE)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSirenActive && canSilenceSiren) {
                                IconButton(
                                    onClick = onSilenceSiren,
                                    modifier = Modifier.testTag("silence_siren_banner_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeOff,
                                        contentDescription = "Silence Siren",
                                        tint = Color.White
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismissBanner,
                                modifier = Modifier.testTag("dismiss_banner_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Banner",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pasien: ${alert.patientCategory} • ${alert.conditionSummary}",
                            fontSize = 11.sp,
                            color = Color(0xFFFFCDD2),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onOpenAlertDetails(alert.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = EmergencyRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("view_active_alert_btn")
                        ) {
                            Text("Buka Respon", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
