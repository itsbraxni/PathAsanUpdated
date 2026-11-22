package com.pathasan.app.data

enum class AppLanguage(val tag: String) { ENGLISH("en"), FILIPINO("fil") }
enum class TextSize { STANDARD, LARGE, EXTRA_LARGE }

data class UserSession(val id: Long, val fullName: String, val email: String)

data class WeatherSnapshot(
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double,
    val relativeHumidityPercent: Int,
    val windSpeedKph: Double,
    val precipitationMm: Double,
    val weatherCode: Int,
    val observedAt: String,
    val fetchedAtEpochMillis: Long,
)

enum class AlertSeverity { INFORMATIONAL, WATCH, WARNING, EMERGENCY }

data class DisasterAlert(
    val id: String,
    val title: String,
    val hazardType: String,
    val severity: AlertSeverity,
    val affectedArea: String,
    val authority: String,
    val issuedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long?,
    val summary: String,
    val sourceUrl: String,
    val fetchedAtEpochMillis: Long,
)

data class GuideStep(val title: String, val body: String)
