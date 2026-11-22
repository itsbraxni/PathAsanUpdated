package com.pathasan.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface WeatherResult {
    data class Success(
        val weather: WeatherSnapshot,
        val isStale: Boolean = false,
        val message: String? = null,
    ) : WeatherResult
    data class Error(val message: String) : WeatherResult
}

class WeatherRepository(context: Context) {
    private val cache = context.applicationContext.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)

    suspend fun fetchBaguioWeather(): WeatherResult = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(OPEN_METEO_BAGUIO_URL).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                if (connection.responseCode !in 200..299) {
                    error("Weather service returned ${connection.responseCode}.")
                }
                val current = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                    .getJSONObject("current")
                WeatherSnapshot(
                    temperatureCelsius = current.getDouble("temperature_2m"),
                    apparentTemperatureCelsius = current.getDouble("apparent_temperature"),
                    relativeHumidityPercent = current.getInt("relative_humidity_2m"),
                    windSpeedKph = current.getDouble("wind_speed_10m"),
                    precipitationMm = current.getDouble("precipitation"),
                    weatherCode = current.getInt("weather_code"),
                    observedAt = current.getString("time"),
                    fetchedAtEpochMillis = System.currentTimeMillis(),
                )
            } finally {
                connection.disconnect()
            }
        }.fold(
            onSuccess = {
                saveCached(it)
                WeatherResult.Success(it)
            },
            onFailure = { error ->
                val message = error.message ?: "Weather data is unavailable."
                loadCached()?.let { WeatherResult.Success(it, isStale = true, message = message) }
                    ?: WeatherResult.Error(message)
            },
        )
    }

    private fun saveCached(weather: WeatherSnapshot) {
        val json = JSONObject()
            .put("temperatureCelsius", weather.temperatureCelsius)
            .put("apparentTemperatureCelsius", weather.apparentTemperatureCelsius)
            .put("relativeHumidityPercent", weather.relativeHumidityPercent)
            .put("windSpeedKph", weather.windSpeedKph)
            .put("precipitationMm", weather.precipitationMm)
            .put("weatherCode", weather.weatherCode)
            .put("observedAt", weather.observedAt)
            .put("fetchedAtEpochMillis", weather.fetchedAtEpochMillis)
        cache.edit().putString(CACHE_KEY, json.toString()).apply()
    }

    private fun loadCached(): WeatherSnapshot? = runCatching {
        val json = JSONObject(cache.getString(CACHE_KEY, null) ?: return null)
        WeatherSnapshot(
            temperatureCelsius = json.getDouble("temperatureCelsius"),
            apparentTemperatureCelsius = json.getDouble("apparentTemperatureCelsius"),
            relativeHumidityPercent = json.getInt("relativeHumidityPercent"),
            windSpeedKph = json.getDouble("windSpeedKph"),
            precipitationMm = json.getDouble("precipitationMm"),
            weatherCode = json.getInt("weatherCode"),
            observedAt = json.getString("observedAt"),
            fetchedAtEpochMillis = json.getLong("fetchedAtEpochMillis"),
        )
    }.getOrNull()

    private companion object {
        const val CACHE_KEY = "baguio_weather"
        const val OPEN_METEO_BAGUIO_URL =
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=16.4023&longitude=120.5960" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
                "precipitation,weather_code,wind_speed_10m" +
                "&timezone=Asia%2FManila"
    }
}
