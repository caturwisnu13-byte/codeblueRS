package com.example

import com.example.data.HospitalData
import com.example.location.HospitalLocationTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun hospitalData_isProperlyInitialized() {
        val rooms = HospitalData.rooms
        assertTrue(rooms.isNotEmpty())
        assertTrue(HospitalData.buildings.isNotEmpty())
        val initialResponders = HospitalData.getInitialResponders()
        assertEquals(6, initialResponders.size)
    }

    @Test
    fun distanceCalculation_sameFloor_returnsPlanarDistance() {
        val distance = HospitalLocationTracker.calculateHospitalDistanceMeters(
            x1 = 0.1f,
            y1 = 0.1f,
            floor1 = 1,
            x2 = 0.4f,
            y2 = 0.5f,
            floor2 = 1
        )
        assertTrue(distance > 0)
    }

    @Test
    fun distanceCalculation_differentFloors_addsFloorPenalty() {
        val distFloor1 = HospitalLocationTracker.calculateHospitalDistanceMeters(
            x1 = 0.2f, y1 = 0.2f, floor1 = 1,
            x2 = 0.2f, y2 = 0.2f, floor2 = 1
        )
        val distFloor2 = HospitalLocationTracker.calculateHospitalDistanceMeters(
            x1 = 0.2f, y1 = 0.2f, floor1 = 1,
            x2 = 0.2f, y2 = 0.2f, floor2 = 3
        )
        assertEquals(0, distFloor1)
        assertEquals(30, distFloor2)
    }
}
