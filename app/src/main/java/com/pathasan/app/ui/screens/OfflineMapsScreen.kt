package com.pathasan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pathasan.app.R
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.BaguioCbdOfflineArea
import com.pathasan.app.data.OfflineDownloadStatus
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.PathAsanMap
import com.pathasan.app.ui.components.PrimaryButton
import com.pathasan.app.ui.components.SectionCard
import com.pathasan.app.ui.components.rememberIsOnline
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed
import com.pathasan.app.viewmodel.OfflineMapsViewModel
import java.util.Locale

@Composable
fun OfflineMapsScreen(
    language: AppLanguage,
    viewModel: OfflineMapsViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isOnline by rememberIsOnline()
    val fil = language == AppLanguage.FILIPINO

    LaunchedEffect(Unit) { viewModel.refresh() }

    PathAsanScaffold(
        title = stringResource(R.string.offline_maps),
        currentRoute = "offline_maps",
        onBack = onBack,
        onNavigate = onNavigate,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PathAsanMap(
                        language = language,
                        actualLocation = null,
                        focusRequest = null,
                        isOnline = isOnline,
                        offlineMapReady = state.isDownloaded,
                        showDownloadBoundary = true,
                        showMyLocationButton = false,
                        onMyLocationRequested = {},
                        onOutsideOfflineAreaChanged = {},
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                    )
                    Text(
                        if (fil) "Ang pulang boundary ang eksaktong area na ise-save."
                        else "The red boundary is the exact area that will be saved.",
                        color = PathAsanMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                SectionCard {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Map, null, tint = PathAsanRed)
                        Column {
                            Text("Baguio CBD", fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (fil)
                                    "Interactive MapLibre tiles para sa CBD, zoom ${BaguioCbdOfflineArea.minZoom.toInt()}–${BaguioCbdOfflineArea.maxZoom.toInt()}."
                                else
                                    "Interactive MapLibre tiles for the CBD, zoom ${BaguioCbdOfflineArea.minZoom.toInt()}–${BaguioCbdOfflineArea.maxZoom.toInt()}.",
                                color = PathAsanMuted,
                            )
                        }
                    }
                    Text(
                        "N ${BaguioCbdOfflineArea.north}, S ${BaguioCbdOfflineArea.south}, " +
                            "E ${BaguioCbdOfflineArea.east}, W ${BaguioCbdOfflineArea.west}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PathAsanMuted,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            item {
                SectionCard {
                    Text(
                        if (fil) "Katayuan ng offline map" else "Offline map status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        statusText(state.status, fil),
                        color = if (state.status == OfflineDownloadStatus.ERROR) PathAsanRed else PathAsanMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    if (state.status == OfflineDownloadStatus.DOWNLOADING ||
                        state.status == OfflineDownloadStatus.PAUSED
                    ) {
                        LinearProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            color = PathAsanRed,
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                        )
                        Text(
                            "${state.progressPercent}% · ${formatBytes(state.completedBytes)}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        if (state.completedResources > 0) {
                            Text(
                                if (state.requiredResources > 0) {
                                    "${state.completedResources} / ${state.requiredResources} resources"
                                } else {
                                    "${state.completedResources} resources"
                                },
                                color = PathAsanMuted,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    state.errorMessage?.let {
                        Text(it, color = PathAsanRed, modifier = Modifier.padding(top = 8.dp))
                    }
                    Text(
                        (if (fil) "Available na storage: " else "Available storage: ") +
                            formatBytes(state.availableStorageBytes),
                        color = PathAsanMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            item {
                when (state.status) {
                    OfflineDownloadStatus.CHECKING,
                    OfflineDownloadStatus.REMOVING -> {
                        LinearProgressIndicator(
                            color = PathAsanRed,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OfflineDownloadStatus.NOT_DOWNLOADED,
                    OfflineDownloadStatus.ERROR -> {
                        PrimaryButton(
                            if (state.status == OfflineDownloadStatus.ERROR) {
                                if (fil) "Subukan muli / ipagpatuloy" else "Retry / resume"
                            } else {
                                if (fil) "I-download ang Baguio CBD map" else "Download Baguio CBD map"
                            },
                            onClick = viewModel::download,
                            enabled = isOnline,
                        )
                        if (!isOnline) {
                            Text(
                                if (fil) "Kailangan ng internet upang simulan ang download."
                                else "An internet connection is required to start the download.",
                                color = PathAsanMuted,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                    OfflineDownloadStatus.DOWNLOADING -> {
                        TextButton(onClick = viewModel::cancel, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Cancel, null, tint = PathAsanRed)
                            Text(
                                if (fil) "Kanselahin ang download" else "Cancel download",
                                color = PathAsanRed,
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                    OfflineDownloadStatus.PAUSED -> {
                        PrimaryButton(
                            if (fil) "Ipagpatuloy ang download" else "Resume download",
                            onClick = viewModel::download,
                            enabled = isOnline,
                        )
                        TextButton(onClick = viewModel::remove, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Delete, null, tint = PathAsanRed)
                            Text(if (fil) "Alisin ang partial data" else "Remove partial data", color = PathAsanRed)
                        }
                    }
                    OfflineDownloadStatus.COMPLETE -> {
                        PrimaryButton(
                            if (fil) "Tingnan ang offline map" else "View offline map",
                            onClick = { onNavigate("routes") },
                        )
                        TextButton(onClick = viewModel::remove, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Delete, null, tint = PathAsanRed)
                            Text(if (fil) "Alisin ang downloaded map" else "Remove downloaded map", color = PathAsanRed)
                        }
                    }
                }
            }

            item {
                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (state.isDownloaded) Icons.Outlined.CheckCircle else Icons.Outlined.CloudDownload,
                            null,
                            tint = PathAsanRed,
                        )
                        Column {
                            Text(
                                if (fil) "Mahalagang paalala" else "Important notice",
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                if (fil)
                                    "Map data at style resources lamang ang download. Walang verified evacuation-center data, live alert, o offline routing."
                                else
                                    "The download contains map data and style resources only. It does not include verified evacuation-center data, live alerts, or offline routing.",
                                color = PathAsanMuted,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                "© OpenFreeMap © OpenMapTiles · Data from OpenStreetMap",
                                color = PathAsanMuted,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            item {
                TextButton(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Refresh, null, tint = PathAsanRed)
                    Text(if (fil) "I-refresh ang status" else "Refresh status", color = PathAsanRed)
                }
            }
        }
    }
}

private fun statusText(status: OfflineDownloadStatus, fil: Boolean): String = when (status) {
    OfflineDownloadStatus.CHECKING -> if (fil) "Sinusuri ang local map storage…" else "Checking local map storage…"
    OfflineDownloadStatus.NOT_DOWNLOADED -> if (fil) "Hindi pa downloaded" else "Not downloaded"
    OfflineDownloadStatus.DOWNLOADING -> if (fil) "Nagda-download…" else "Downloading…"
    OfflineDownloadStatus.PAUSED -> if (fil) "Naka-pause" else "Paused"
    OfflineDownloadStatus.COMPLETE -> if (fil) "Available offline" else "Available offline"
    OfflineDownloadStatus.REMOVING -> if (fil) "Inaalis…" else "Removing…"
    OfflineDownloadStatus.ERROR -> if (fil) "Hindi natapos ang operasyon" else "The operation did not complete"
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val megabytes = bytes / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f MB", megabytes)
}
