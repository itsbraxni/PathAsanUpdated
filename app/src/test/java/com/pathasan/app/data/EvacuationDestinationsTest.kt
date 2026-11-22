package com.pathasan.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvacuationDestinationsTest {
    @Test
    fun destinationIdsAndNamesAreUnique() {
        val destinations = EvacuationDestinations.all

        assertEquals(destinations.size, destinations.map { it.id }.distinct().size)
        assertEquals(destinations.size, destinations.map { it.name }.distinct().size)
    }

    @Test
    fun coordinatesAreValidAndAllEntriesRemainUnverified() {
        assertTrue(EvacuationDestinations.all.all {
            it.latitude in -90.0..90.0 &&
                it.longitude in -180.0..180.0 &&
                it.designation == DestinationDesignation.PROPOSED_UNVERIFIED
        })
    }

    @Test
    fun catalogContainsTheFourBriefLocations() {
        assertEquals(
            setOf(
                "melvin-jones-football-grounds",
                "sunshine-park",
                "burnham-park",
                "baguio-athletic-bowl",
            ),
            EvacuationDestinations.all.map { it.id }.toSet(),
        )
    }
}
