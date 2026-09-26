package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EmergencyAlertManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var toneGenerator: ToneGenerator? = null
    private var sirenJob: Job? = null
    private var metronomeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        createNotificationChannel()
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Peringatan Darurat Code Blue RS",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi darurat henti jantung dan resusitasi Code Blue"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 800)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun playSiren(continuous: Boolean = true) {
        stopSiren()
        sirenJob = scope.launch {
            vibrateAlert()
            val startTime = System.currentTimeMillis()
            while (isActive) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 750)
                } catch (_: Exception) {}
                delay(850)
                if (!continuous && (System.currentTimeMillis() - startTime) >= 12000L) {
                    break
                }
            }
        }
    }

    val isSirenPlaying: Boolean
        get() = sirenJob != null && sirenJob?.isActive == true

    fun stopSiren() {
        sirenJob?.cancel()
        sirenJob = null
    }

    fun startCprMetronome(bpm: Int = 110, onTick: () -> Unit = {}) {
        stopCprMetronome()
        val intervalMs = (60000.0 / bpm).toLong()
        metronomeJob = scope.launch {
            while (isActive) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                } catch (_: Exception) {}
                onTick()
                delay(intervalMs)
            }
        }
    }

    fun stopCprMetronome() {
        metronomeJob?.cancel()
        metronomeJob = null
    }

    fun vibrateAlert() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 400, 200, 400, 200, 600),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 400, 200, 400, 200, 600), -1)
            }
        } catch (_: Exception) {}
    }

    fun sendInstantPushNotification(
        alertId: String,
        roomName: String,
        building: String,
        floor: Int,
        patientCategory: String,
        needsDefib: Boolean
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALERT_ID", alertId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alertId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defibText = if (needsDefib) "⚠️ BUTUH DEFIBRILATOR SEGERA!" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 CODE BLUE AKTIF: $roomName")
            .setContentText("$building, Lt.$floor • $patientCategory $defibText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 PANGGILAN CODE BLUE RESUSITASI!\nLokasi: $roomName ($building, Lt. $floor)\nPasien: $patientCategory\n$defibText\nSegera lakukan respons dan tuju lokasi!")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 800))
            .build()

        try {
            notificationManager.notify(alertId.hashCode(), notification)
        } catch (_: Exception) {}
    }

    companion object {
        const val CHANNEL_ID = "code_blue_emergency_channel"
    }
}
