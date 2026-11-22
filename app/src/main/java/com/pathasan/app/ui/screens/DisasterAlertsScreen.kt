package com.pathasan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pathasan.app.R
import com.pathasan.app.data.AlertSeverity
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.DisasterAlert
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.SectionCard
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed
import com.pathasan.app.viewmodel.AlertFeedStatus
import com.pathasan.app.viewmodel.DisasterAlertsViewModel
import com.pathasan.app.viewmodel.WeatherUiState
import com.pathasan.app.viewmodel.WeatherViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DisasterAlertsScreen(
    language: AppLanguage,
    viewModel: DisasterAlertsViewModel,
    weatherViewModel: WeatherViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val weatherState by weatherViewModel.state.collectAsStateWithLifecycle()
    val fil = language == AppLanguage.FILIPINO
    val uriHandler = LocalUriHandler.current

    PathAsanScaffold(
        title = stringResource(R.string.disaster_alerts),
        currentRoute = "alerts",
        onBack = onBack,
        onNavigate = onNavigate,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                WeatherCard(
                    state = weatherState,
                    fil = fil,
                    onRefresh = weatherViewModel::refresh,
                )
            }

            when {
                state.status == AlertFeedStatus.LOADING && state.alerts.isEmpty() -> item {
                    Row(
                        Modifier.fillMaxWidth().padding(28.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = PathAsanRed)
                    }
                }
                state.status == AlertFeedStatus.LIVE_UNAVAILABLE && state.alerts.isEmpty() -> item {
                    AlertStatusCard(
                        title = if (fil) "Hindi available ang live alerts" else "Live alerts are not currently available",
                        body = if (fil)
                            "Walang validated government alert feed na nakakonekta. Walang pekeng alert na ipapakita."
                        else
                            "No validated government alert feed is connected. No placeholder alerts are shown.",
                    )
                }
                state.status == AlertFeedStatus.ERROR && state.alerts.isEmpty() -> item {
                    AlertStatusCard(
                        title = if (fil) "Hindi ma-refresh ang alerts" else "Alerts could not be refreshed",
                        body = state.errorMessage ?: if (fil) "Subukan muli mamaya." else "Try again later.",
                    )
                }
                state.status == AlertFeedStatus.READY && state.alerts.isEmpty() -> item {
                    AlertStatusCard(
                        title = if (fil) "Walang kasalukuyang alert" else "No current alerts",
                        body = if (fil)
                            "Walang alert na ibinalik ng nakakonektang source."
                        else
                            "The connected source returned no alerts.",
                    )
                }
            }

            if (state.isStale) {
                item {
                    AlertStatusCard(
                        title = if (fil) "Cached alerts — maaaring luma" else "Cached alerts — may be outdated",
                        body = if (fil)
                            "Hindi available ang live refresh. Suriin ang oras at opisyal na source ng bawat alert."
                        else
                            "Live refresh is unavailable. Check each alert's time and official source.",
                    )
                }
            }

            items(state.alerts, key = { it.id }) { alert ->
                AlertCard(alert, language)
            }

            item {
                TextButton(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Refresh, null, tint = PathAsanRed)
                    Text(
                        if (fil) "I-refresh ang alerts" else "Refresh alerts",
                        color = PathAsanRed,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }

            item {
                SectionCard {
                    Text(
                        if (fil) "Opisyal na impormasyon" else "Official information",
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        if (fil)
                            "Buksan ang mga opisyal na channel para sa pinakabagong bulletin. External links ang mga ito; hindi kino-convert ng app ang posts bilang live alerts."
                        else
                            "Open the official channels for the latest bulletins. These are external links; the app does not convert their posts into live alerts.",
                        color = PathAsanMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    OfficialSourceButton(
                        label = if (fil) "PAGASA weather bulletins" else "PAGASA weather bulletins",
                        onClick = { uriHandler.openUri(PAGASA_URL) },
                    )
                    OfficialSourceButton(
                        label = if (fil) "Baguio CDRRMO official page" else "Baguio CDRRMO official page",
                        onClick = { uriHandler.openUri(BAGUIO_CDRRMO_URL) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(
    state: WeatherUiState,
    fil: Boolean,
    onRefresh: () -> Unit,
) {
    SectionCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Thermostat, null, tint = PathAsanRed)
            Column(Modifier.weight(1f)) {
                Text(
                    if (fil) "Kasalukuyang panahon sa Baguio" else "Current Baguio weather",
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "Open-Meteo · ${if (fil) "hindi opisyal na PAGASA data" else "not official PAGASA data"}",
                    color = PathAsanMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            TextButton(onClick = onRefresh, enabled = !state.loading) {
                Icon(Icons.Outlined.Refresh, null, tint = PathAsanRed)
            }
        }
        when {
            state.loading && state.weather == null -> Row(
                Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = PathAsanRed)
            }
            state.weather != null -> {
                val weather = state.weather
                Text(
                    "${weather.temperatureCelsius.formatOneDecimal()}°C · " +
                        weatherDescription(weather.weatherCode, fil),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    if (fil) {
                        "Pakiramdam ${weather.apparentTemperatureCelsius.formatOneDecimal()}°C · " +
                            "Humidity ${weather.relativeHumidityPercent}% · " +
                            "Hangin ${weather.windSpeedKph.formatOneDecimal()} km/h · " +
                            "Ulan ${weather.precipitationMm.formatOneDecimal()} mm"
                    } else {
                        "Feels like ${weather.apparentTemperatureCelsius.formatOneDecimal()}°C · " +
                            "Humidity ${weather.relativeHumidityPercent}% · " +
                            "Wind ${weather.windSpeedKph.formatOneDecimal()} km/h · " +
                            "Rain ${weather.precipitationMm.formatOneDecimal()} mm"
                    },
                    color = PathAsanMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    (if (fil) "Huling nakuha: " else "Last updated: ") +
                        formatTime(weather.fetchedAtEpochMillis),
                    color = PathAsanMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (state.isStale) {
                    Text(
                        if (fil) "Naka-save na weather — maaaring luma."
                        else "Saved weather — may be outdated.",
                        color = PathAsanRed,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            else -> Text(
                if (fil) "Hindi makuha ang weather ngayon. Tingnan ang PAGASA link sa ibaba."
                else "Weather is unavailable right now. Check the PAGASA link below.",
                color = PathAsanMuted,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun OfficialSourceButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.OpenInNew, null, tint = PathAsanRed)
        Text(label, color = PathAsanRed, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun AlertStatusCard(title: String, body: String) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Info, null, tint = PathAsanRed)
            Column {
                Text(title, fontWeight = FontWeight.ExtraBold)
                Text(body, color = PathAsanMuted, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun AlertCard(alert: DisasterAlert, language: AppLanguage) {
    val fil = language == AppLanguage.FILIPINO
    val uriHandler = LocalUriHandler.current
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Warning, null, tint = PathAsanRed)
            Column(Modifier.weight(1f)) {
                Text(
                    "${alert.hazardType} · ${severityLabel(alert.severity, fil)}",
                    color = PathAsanRed,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(alert.title, fontWeight = FontWeight.ExtraBold)
            }
        }
        Text(alert.summary, modifier = Modifier.padding(top = 10.dp))
        Text(
            (if (fil) "Apektadong lugar: " else "Affected area: ") + alert.affectedArea,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            (if (fil) "Awtoridad: " else "Authority: ") + alert.authority,
            color = PathAsanMuted,
        )
        Text(
            (if (fil) "Inilabas: " else "Issued: ") + formatTime(alert.issuedAtEpochMillis),
            color = PathAsanMuted,
            style = MaterialTheme.typography.bodySmall,
        )
        alert.updatedAtEpochMillis?.let { updatedAt ->
            Text(
                (if (fil) "Huling update: " else "Last updated: ") + formatTime(updatedAt),
                color = PathAsanMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (alert.sourceUrl.isNotBlank()) {
            TextButton(onClick = { uriHandler.openUri(alert.sourceUrl) }) {
                Icon(Icons.Outlined.OpenInNew, null, tint = PathAsanRed)
                Text(
                    if (fil) "Buksan ang source" else "Open source",
                    color = PathAsanRed,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

private fun severityLabel(severity: AlertSeverity, fil: Boolean): String = when (severity) {
    AlertSeverity.INFORMATIONAL -> if (fil) "IMPORMASYON" else "INFORMATIONAL"
    AlertSeverity.WATCH -> if (fil) "PAGBABANTAY" else "WATCH"
    AlertSeverity.WARNING -> if (fil) "BABALA" else "WARNING"
    AlertSeverity.EMERGENCY -> if (fil) "EMERGENCY" else "EMERGENCY"
}

private fun formatTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(epochMillis))
}

private fun Double.formatOneDecimal(): String = String.format(Locale.US, "%.1f", this)

private fun weatherDescription(code: Int, fil: Boolean): String = when (code) {
    0 -> if (fil) "Maaliwalas" else "Clear"
    1, 2 -> if (fil) "Bahagyang maulap" else "Partly cloudy"
    3 -> if (fil) "Maulap" else "Overcast"
    45, 48 -> if (fil) "Maulap na may fog" else "Foggy"
    51, 53, 55, 56, 57 -> if (fil) "Ambon" else "Drizzle"
    61, 63, 65, 66, 67, 80, 81, 82 -> if (fil) "Umuulan" else "Rain"
    71, 73, 75, 77, 85, 86 -> if (fil) "Niyebe" else "Snow"
    95, 96, 99 -> if (fil) "Thunderstorm" else "Thunderstorm"
    else -> if (fil) "Hindi matukoy" else "Unknown conditions"
}

private const val PAGASA_URL = "https://www.pagasa.dost.gov.ph/"
private const val BAGUIO_CDRRMO_URL = "https://www.facebook.com/baguiocdrrmo"
