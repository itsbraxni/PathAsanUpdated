package com.pathasan.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathasan.app.R
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.UserSession
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.SectionCard
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed
import com.pathasan.app.ui.theme.PathAsanRedSoft
import com.pathasan.app.ui.theme.PathAsanWhite
import com.pathasan.app.viewmodel.AlertFeedStatus
import com.pathasan.app.viewmodel.DisasterAlertsUiState

private data class PhotoFeature(
    val en: String,
    val fil: String,
    val subtitleEn: String,
    val subtitleFil: String,
    val route: String,
    @DrawableRes val image: Int,
)

@Composable
fun HomeScreen(
    language: AppLanguage,
    user: UserSession?,
    alertsState: DisasterAlertsUiState,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val fil = language == AppLanguage.FILIPINO
    val firstName = user?.fullName?.substringBefore(" ") ?: if (fil) "Manlalakbay" else "Explorer"
    val features = listOf(
        PhotoFeature(
            "Weather & Disaster Alerts",
            "Panahon at Mga Alerto",
            "Open-Meteo conditions and official authority links",
            "Open-Meteo weather at opisyal na authority links",
            "alerts",
            R.drawable.feature_weather,
        ),
        PhotoFeature(
            "Evacuation Centers",
            "Mga Evacuation Center",
            "Map view; verified center feed not connected",
            "Map view; walang konektadong verified center feed",
            "routes",
            R.drawable.feature_baguio,
        ),
        PhotoFeature(
            "Emergency Contacts",
            "Mga Emergency Contact",
            "Verified numbers available offline",
            "Verified na numero na available offline",
            "contacts",
            R.drawable.feature_emergency,
        ),
        PhotoFeature(
            "Offline Maps",
            "Mga Offline na Mapa",
            "Download interactive Baguio CBD map data",
            "I-download ang interactive Baguio CBD map data",
            "offline_maps",
            R.drawable.feature_offline_map,
        ),
    )

    PathAsanScaffold(
        title = stringResource(R.string.app_name),
        currentRoute = "home",
        onNavigate = onNavigate,
        action = {
            TextButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = PathAsanRed)
                Text(
                    if (fil) "Log out" else "Log out",
                    color = PathAsanRed,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            IconButton(onClick = { onNavigate("settings") }) {
                Icon(Icons.Outlined.Settings, stringResource(R.string.settings))
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("Baguio City, Philippines", color = PathAsanMuted)
                Text(
                    if (fil) "Kumusta, $firstName!" else "Hello, $firstName!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                user?.email?.let {
                    Text(it, color = PathAsanMuted, style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                AlertSummary(alertsState, fil) { onNavigate("alerts") }
            }

            item {
                Text(
                    if (fil) "Mabilis na access" else "Quick access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            items(features, key = { it.route }) { feature ->
                PhotoFeatureCard(feature, fil) { onNavigate(feature.route) }
            }

            item {
                SectionCard(Modifier.clickable { onNavigate("guide") }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Book,
                            null,
                            tint = PathAsanWhite,
                            modifier = Modifier.size(46.dp)
                                .background(PathAsanRed, RoundedCornerShape(12.dp))
                                .padding(11.dp),
                        )
                        Column {
                            Text(
                                if (fil) "Gabay sa Emergency" else "Emergency Guide",
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                if (fil) "Mga hakbang bago, habang, at pagkatapos"
                                else "Steps for before, during, and after",
                                color = PathAsanMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertSummary(
    state: DisasterAlertsUiState,
    fil: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .background(PathAsanRedSoft, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Warning, null, tint = PathAsanRed, modifier = Modifier.size(30.dp))
        Column(Modifier.weight(1f)) {
            val latest = state.alerts.firstOrNull()
            Text(
                when {
                    latest != null && state.isStale ->
                        if (fil) "CACHED ALERT · MAAARING LUMA" else "CACHED ALERT · MAY BE OUTDATED"
                    latest != null -> latest.hazardType.uppercase()
                    state.status == AlertFeedStatus.LOADING ->
                        if (fil) "SINUSURI ANG ALERTS" else "CHECKING ALERTS"
                    state.status == AlertFeedStatus.READY ->
                        if (fil) "WALANG KASALUKUYANG ALERT" else "NO CURRENT ALERTS"
                    state.status == AlertFeedStatus.LIVE_UNAVAILABLE ->
                        if (fil) "HINDI AVAILABLE ANG LIVE ALERTS" else "LIVE ALERTS UNAVAILABLE"
                    else -> if (fil) "HINDI MA-REFRESH ANG ALERTS" else "ALERT REFRESH UNAVAILABLE"
                },
                color = PathAsanRed,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                latest?.title ?: if (fil) {
                    "Walang validated government alert feed na nakakonekta."
                } else {
                    "No validated government alert feed is connected."
                },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PhotoFeatureCard(feature: PhotoFeature, fil: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
    ) {
        Box(Modifier.fillMaxWidth().height(132.dp)) {
            Image(
                painter = painterResource(feature.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                    )
                )
            )
            Column(
                Modifier.align(Alignment.BottomStart).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    if (fil) feature.fil else feature.en,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    if (fil) feature.subtitleFil else feature.subtitleEn,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
