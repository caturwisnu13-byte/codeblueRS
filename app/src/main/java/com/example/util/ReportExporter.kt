package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.CodeBlueAlertEntity
import com.example.data.IncidentLogEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))
    private val fileDateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Download / Export individual incident report as a detailed clinical dossier document (.txt / .csv)
     */
    fun exportIndividualReport(
        context: Context,
        alert: CodeBlueAlertEntity,
        logs: List<IncidentLogEntity>
    ) {
        val activatedDateStr = dateFormat.format(Date(alert.activatedAt))
        val arrivalDateStr = alert.firstArrivalAt?.let { dateFormat.format(Date(it)) } ?: "Belum tercatat"
        val resolvedDateStr = alert.resolvedAt?.let { dateFormat.format(Date(it)) } ?: "Belum selesai"

        val responseSecs = if (alert.firstArrivalAt != null) {
            ((alert.firstArrivalAt - alert.activatedAt) / 1000).toInt()
        } else {
            0
        }
        val mins = responseSecs / 60
        val secs = responseSecs % 60
        val isUnder5Min = responseSecs in 1..300
        val slaStatus = if (responseSecs == 0) "Belum Tiba" else if (isUnder5Min) "MEMENUHI TARGET (< 5 Menit)" else "MELEBIHI TARGET (> 5 Menit)"

        val reportContent = buildString {
            appendLine("================================================================================")
            appendLine("                      RUMAH SAKIT UMUM DAERAH")
            appendLine("               KOMITE MUTU & KESELAMATAN PASIEN (KMKP)")
            appendLine("               LAPORAN RESMI AUDIT INSIDEN CODE BLUE")
            appendLine("================================================================================")
            appendLine("ID Panggilan         : ${alert.id}")
            appendLine("Tipe Kejadian        : ${if (alert.isDrill) "SIMULASI LATIHAN (DRILL)" else "KEJADIAN DARURAT NYATA"}")
            appendLine("Ruangan / Unit       : ${alert.roomName} (${alert.roomId})")
            appendLine("Gedung & Lantai      : ${alert.building}, Lantai ${alert.floor}")
            appendLine("Nomor Bed            : ${alert.bedNumber}")
            appendLine("Kategori Pasien      : ${alert.patientCategory}")
            appendLine("Kondisi Awal         : ${alert.conditionSummary}")
            appendLine("Pelapor Panggilan    : ${alert.callerName}")
            appendLine("Dokter Penanggung Jwb: ${alert.leadDoctor}")
            appendLine("Petugas Deaktivasi   : ${alert.deactivatedBy ?: "-"}")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("EVALUASI WAKTU RESPON (KPI STANDAR AKREDITASI KARS < 5 MENIT):")
            appendLine("Waktu Aktivasi       : $activatedDateStr")
            appendLine("Waktu Tim Tiba       : $arrivalDateStr")
            appendLine("Waktu Respon Tim     : ${mins} menit ${secs} detik")
            appendLine("Status Capaian Mutu  : $slaStatus")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("RINGKASAN TINDAKAN RESUSITASI:")
            appendLine("Total Kejutan Defib  : ${alert.totalShocks} kali")
            appendLine("Total Dosis Epinefrin: ${alert.totalEpiDoses} ampul")
            appendLine("Total Durasi CPR/RJP : ${alert.cprDurationSeconds / 60} menit ${alert.cprDurationSeconds % 60} detik")
            appendLine("Status Selesai       : $resolvedDateStr")
            appendLine("Hasil / Disposisi    : ${alert.outcome ?: "Masih Dalam Penanganan"}")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("KRONOLOGI & AUDIT TRAIL LOG KLINIS:")
            if (logs.isEmpty()) {
                appendLine("  (Tidak ada catatan log tambahan)")
            } else {
                logs.forEachIndexed { index, log ->
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                    appendLine("  [${index + 1}] $timeStr - ${log.authorName} (${log.authorRole}):")
                    appendLine("      [${log.logType}] ${log.message}")
                }
            }
            appendLine("================================================================================")
            appendLine("Dicetak secara digital oleh Sistem CodeBlue RS pada: ${dateFormat.format(Date())}")
            appendLine("Dokumen ini sah sebagai arsip rekam medis & audit komite mutu rumah sakit.")
            appendLine("================================================================================")
        }

        val fileName = "Laporan_CodeBlue_${alert.id}_${fileDateSuffix.format(Date())}.txt"
        saveAndShareFile(context, fileName, reportContent, "text/plain")
    }

    /**
     * Download / Export periodic recap spreadsheet (.csv) based on selected period
     */
    fun exportPeriodRecapCsv(
        context: Context,
        alerts: List<CodeBlueAlertEntity>,
        periodTitle: String
    ) {
        val totalIncidents = alerts.size
        val arrivalsWithTime = alerts.filter { it.firstArrivalAt != null }
        val compliantCount = alerts.count {
            it.firstArrivalAt != null && ((it.firstArrivalAt - it.activatedAt) / 1000) <= 300
        }
        val avgResponseSecs = if (arrivalsWithTime.isNotEmpty()) {
            arrivalsWithTime.map { ((it.firstArrivalAt!! - it.activatedAt) / 1000).toInt() }.average().toInt()
        } else {
            0
        }
        val compliancePct = if (arrivalsWithTime.isNotEmpty()) {
            (compliantCount * 100.0 / arrivalsWithTime.size)
        } else {
            0.0
        }
        val roscCount = alerts.count { it.outcome?.contains("ROSC", ignoreCase = true) == true }

        val csvContent = buildString {
            // Header comments
            appendLine("\"REKAPITULASI LAPORAN INDIKATOR MUTU CODE BLUE RS\"")
            appendLine("\"Periode Laporan: $periodTitle\"")
            appendLine("\"Tanggal Ekspor: ${dateFormat.format(Date())}\"")
            appendLine("\"Total Panggilan: $totalIncidents | Respon < 5 Menit: $compliantCount (${String.format(Locale.US, "%.1f", compliancePct)}%) | Rata-rata Respon: ${avgResponseSecs / 60}m ${avgResponseSecs % 60}d | Keberhasilan ROSC: $roscCount\"")
            appendLine()

            // CSV Columns Header
            appendLine("\"ID Insiden\",\"Tanggal Aktivasi\",\"Gedung\",\"Lantai\",\"Ruangan\",\"Nomor Bed\",\"Kategori Pasien\",\"Kondisi Awal\",\"Waktu Respon (Detik)\",\"Waktu Respon (Format)\",\"Status Mutu (< 5 Menit)\",\"Jumlah Shock Defib\",\"Total Epinefrin\",\"Durasi CPR (Detik)\",\"Status Kejadian\",\"Hasil / Disposisi Pasien\",\"Petugas Deaktivasi\",\"Jenis Kejadian\"")

            // Data rows
            alerts.forEach { alert ->
                val dateStr = dateFormat.format(Date(alert.activatedAt))
                val respSec = if (alert.firstArrivalAt != null) {
                    ((alert.firstArrivalAt - alert.activatedAt) / 1000).toInt()
                } else {
                    -1
                }
                val respFormatted = if (respSec >= 0) "${respSec / 60}m ${respSec % 60}d" else "Belum Tiba"
                val slaStatus = if (respSec < 0) "Belum Tiba" else if (respSec <= 300) "MEMENUHI TARGET (< 5 Min)" else "MELEBIHI TARGET (> 5 Min)"
                val drillStatus = if (alert.isDrill) "Simulasi Drill" else "Kejadian Nyata"

                append("\"${alert.id}\",")
                append("\"$dateStr\",")
                append("\"${alert.building}\",")
                append("\"${alert.floor}\",")
                append("\"${alert.roomName}\",")
                append("\"${alert.bedNumber}\",")
                append("\"${alert.patientCategory}\",")
                append("\"${alert.conditionSummary.replace("\"", "'")}\",")
                append("\"${if (respSec >= 0) respSec else ""}\",")
                append("\"$respFormatted\",")
                append("\"$slaStatus\",")
                append("\"${alert.totalShocks}\",")
                append("\"${alert.totalEpiDoses}\",")
                append("\"${alert.cprDurationSeconds}\",")
                append("\"${alert.status}\",")
                append("\"${(alert.outcome ?: "-").replace("\"", "'")}\",")
                append("\"${(alert.deactivatedBy ?: "-").replace("\"", "'")}\",")
                append("\"$drillStatus\"")
                appendLine()
            }
        }

        val safePeriodName = periodTitle.lowercase().replace(" ", "_").replace("/", "-")
        val fileName = "Rekap_CodeBlue_${safePeriodName}_${fileDateSuffix.format(Date())}.csv"
        saveAndShareFile(context, fileName, csvContent, "text/csv")
    }

    private fun saveAndShareFile(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String
    ) {
        try {
            // 1. Write to app internal cache for FileProvider
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val file = File(reportsDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }

            // 2. Also save to Public MediaStore Downloads (Android 10+) or External Storage so file is accessible in Downloads folder
            saveToPublicDownloads(context, fileName, content, mimeType)

            // 3. Create Share/Open Intent
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, "Laporan Code Blue Rumah Sakit: $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(sendIntent, "Unduh / Bagikan: $fileName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Toast.makeText(context, "Laporan $fileName berhasil dibuat & disimpan ke Downloads!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuat file laporan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToPublicDownloads(context: Context, fileName: String, content: String, mimeType: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CodeBlue_Reports")
                }
                val downloadUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (downloadUri != null) {
                    resolver.openOutputStream(downloadUri)?.use { out ->
                        out.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(publicDir, "CodeBlue_Reports")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    out.write(content.toByteArray(Charsets.UTF_8))
                }
            }
        } catch (_: Exception) {
            // Non-fatal, cache FileProvider will still allow sharing/opening
        }
    }
}
