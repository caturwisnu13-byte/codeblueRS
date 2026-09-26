package com.example.location

import com.example.data.CodeBlueAlertEntity
import com.example.data.CodeBlueRepository
import com.example.data.ResponderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class HospitalLocationTracker(
    private val repository: CodeBlueRepository,
    private val onResponderArrived: (ResponderEntity, CodeBlueAlertEntity) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var trackingJob: Job? = null

    fun startTrackingForAlert(alert: CodeBlueAlertEntity, initialResponders: List<ResponderEntity>) {
        trackingJob?.cancel()
        trackingJob = scope.launch {
            // Assign alert and calculate initial target & ETA for available responders
            val updatedList = initialResponders.map { resp ->
                val dist = hypot(alert.roomPosX - resp.posX, alert.roomPosY - resp.posY)
                val floorDiff = kotlin.math.abs(alert.floor - resp.currentFloor)
                // Calculate realistic hospital ETA: 40-120 seconds base + floor traversal
                val calculatedEta = max(25, (dist * 180 + floorDiff * 35).toInt())
                resp.copy(
                    state = "RESPONDING",
                    assignedAlertId = alert.id,
                    targetPosX = alert.roomPosX,
                    targetPosY = alert.roomPosY,
                    etaSeconds = calculatedEta
                )
            }
            repository.updateResponders(updatedList)

            // Real-time movement simulation loop (every 1.5 seconds)
            while (isActive) {
                delay(1500)
                val currentAlert = repository.getAlertByIdOnce(alert.id)
                if (currentAlert == null || currentAlert.status == "RESOLVED" || currentAlert.status == "CANCELLED") {
                    break
                }

                // Check and advance responders
                // Fetch fresh responders
                // Note: we can do a localized step
                // In repository we have updateResponders
            }
        }
    }

    suspend fun advanceRespondersStep(alertId: String): Boolean {
        val currentAlert = repository.getAlertByIdOnce(alertId) ?: return false
        val assignedResponders = repository.getAlertByIdOnce(alertId)?.let {
            // fetch responders
            // we will query or update
        }
        return true
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    companion object {
        fun calculateHospitalDistanceMeters(x1: Float, y1: Float, floor1: Int, x2: Float, y2: Float, floor2: Int): Int {
            val planarMeters = (hypot(x2 - x1, y2 - y1) * 150).toInt()
            val floorMeters = kotlin.math.abs(floor2 - floor1) * 15
            return planarMeters + floorMeters
        }
    }
}
