package com.pathasan.app.data

import com.pathasan.app.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject

class GeoapifyRepository {
    suspend fun walkingRoute(
        startLatitude: Double,
        startLongitude: Double,
        destination: EvacuationDestination,
    ): RouteResult {
        if (BuildConfig.GEOAPIFY_API_KEY.isBlank()) {
            return RouteResult.Error(
                "Geoapify is not configured. Add GEOAPIFY_API_KEY to local.properties."
            )
        }
        if (!validCoordinate(startLatitude, startLongitude) ||
            !validCoordinate(destination.latitude, destination.longitude)
        ) {
            return RouteResult.Error("The route contains invalid coordinates.")
        }

        // Geoapify waypoints are latitude,longitude. Returned GeoJSON is longitude,latitude.
        val endpoint = URL(
            "https://api.geoapify.com/v1/routing" +
                "?waypoints=$startLatitude,$startLongitude%7C${destination.latitude},${destination.longitude}" +
                "&mode=walk&apiKey=${BuildConfig.GEOAPIFY_API_KEY}"
        )
        return suspendCancellableCoroutine { continuation ->
            var connection: HttpURLConnection? = null
            continuation.invokeOnCancellation { connection?.disconnect() }
            thread(name = "geoapify-routing", isDaemon = true) {
                val result = try {
                    connection = (endpoint.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 15_000
                        readTimeout = 20_000
                        setRequestProperty("Accept", "application/geo+json, application/json")
                    }
                    val responseCode = connection?.responseCode ?: -1
                    if (responseCode !in 200..299) {
                        val message = when (responseCode) {
                            401, 403 -> "Geoapify authentication failed. Check the configured API key."
                            429 -> "Geoapify request limit reached. Try again later."
                            else -> "Geoapify routing failed (HTTP $responseCode)."
                        }
                        RouteResult.Error(message)
                    } else {
                        val body = connection!!.inputStream.bufferedReader().use { it.readText() }
                        parseRoute(body)
                    }
                } catch (_: java.net.SocketTimeoutException) {
                    RouteResult.Error("The routing request timed out. Check your connection and try again.")
                } catch (_: java.io.IOException) {
                    RouteResult.Error("Walking directions are unavailable. Check your internet connection.")
                } catch (_: Exception) {
                    RouteResult.Error("Geoapify returned an invalid routing response.")
                } finally {
                    connection?.disconnect()
                }
                if (continuation.isActive) continuation.resume(result)
            }
        }
    }

    private fun parseRoute(body: String): RouteResult {
        val features = JSONObject(body).optJSONArray("features")
            ?: return RouteResult.Error("Geoapify returned no routes.")
        if (features.length() == 0) return RouteResult.Error("No walking route was found.")

        val feature = features.getJSONObject(0)
        val properties = feature.optJSONObject("properties") ?: JSONObject()
        val distance = properties.optDouble("distance", Double.NaN)
        val duration = properties.optDouble("time", Double.NaN)
        val geometry = feature.optJSONObject("geometry")
            ?: return RouteResult.Error("The route response has no geometry.")
        val points = mutableListOf<RoutePoint>()
        flattenCoordinates(geometry.getJSONArray("coordinates"), points)

        if (points.size < 2 || distance.isNaN() || duration.isNaN()) {
            return RouteResult.Error("Geoapify returned an incomplete walking route.")
        }
        return RouteResult.Success(WalkingRoute(points, distance, duration))
    }

    private fun flattenCoordinates(array: JSONArray, output: MutableList<RoutePoint>) {
        if (array.length() == 0) return
        val first = array.opt(0)
        if (first is Number) {
            if (array.length() >= 2) {
                val longitude = array.getDouble(0)
                val latitude = array.getDouble(1)
                if (validCoordinate(latitude, longitude)) {
                    output += RoutePoint(latitude, longitude)
                }
            }
            return
        }
        for (index in 0 until array.length()) {
            (array.opt(index) as? JSONArray)?.let { flattenCoordinates(it, output) }
        }
    }

    private fun validCoordinate(latitude: Double, longitude: Double): Boolean =
        latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0
}
