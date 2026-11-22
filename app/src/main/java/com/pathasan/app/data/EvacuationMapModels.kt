package com.pathasan.app.data

data class EvacuationDestination(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val designation: DestinationDesignation,
)

enum class DestinationDesignation {
    PROPOSED_UNVERIFIED,
    OFFICIALLY_CONFIRMED,
}

object EvacuationDestinations {
    /**
     * These places and coordinates came from the existing project brief. They are intentionally
     * not described as official evacuation centers until Baguio CDRRMO verifies them.
     */
    val all: List<EvacuationDestination> = listOf(
        EvacuationDestination(
            id = "melvin-jones-football-grounds",
            name = "Melvin Jones Football Grounds",
            latitude = 16.409338,
            longitude = 120.595391,
            description = "Open grounds near Burnham Park. Suitability and current emergency use must be confirmed by local authorities.",
            designation = DestinationDesignation.PROPOSED_UNVERIFIED,
        ),
        EvacuationDestination(
            id = "sunshine-park",
            name = "Sunshine Park",
            latitude = 16.408913,
            longitude = 120.596516,
            description = "Public park in the Baguio CBD. Suitability and current emergency use must be confirmed by local authorities.",
            designation = DestinationDesignation.PROPOSED_UNVERIFIED,
        ),
        EvacuationDestination(
            id = "burnham-park",
            name = "Burnham Park",
            latitude = 16.409938,
            longitude = 120.595438,
            description = "Public park in central Baguio. This proposed point is not proof of an officially activated evacuation site.",
            designation = DestinationDesignation.PROPOSED_UNVERIFIED,
        ),
        EvacuationDestination(
            id = "baguio-athletic-bowl",
            name = "Baguio Athletic Bowl",
            latitude = 16.410438,
            longitude = 120.595297,
            description = "Sports facility near Burnham Park. Suitability and current emergency use must be confirmed by local authorities.",
            designation = DestinationDesignation.PROPOSED_UNVERIFIED,
        ),
    )

    init {
        require(all.map { it.id }.distinct().size == all.size) {
            "Evacuation destination IDs must be unique."
        }
        require(all.all { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }) {
            "Evacuation destination coordinates must be valid."
        }
    }
}

data class RoutePoint(val latitude: Double, val longitude: Double)

data class WalkingRoute(
    val points: List<RoutePoint>,
    val distanceMeters: Double,
    val durationSeconds: Double,
)

sealed interface RouteResult {
    data class Success(val route: WalkingRoute) : RouteResult
    data class Error(val message: String) : RouteResult
}
