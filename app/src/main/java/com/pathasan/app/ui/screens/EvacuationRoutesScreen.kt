package com.pathasan.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pathasan.app.R
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.DestinationDesignation
import com.pathasan.app.data.EvacuationDestination
import com.pathasan.app.data.EvacuationDestinations
import com.pathasan.app.location.LocationState
import com.pathasan.app.ui.components.MapFocusRequest
import com.pathasan.app.ui.components.PathAsanMap
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.PrimaryButton
import com.pathasan.app.ui.components.SectionCard
import com.pathasan.app.ui.components.rememberIsOnline
import com.pathasan.app.ui.theme.PathAsanBlack
import com.pathasan.app.ui.theme.PathAsanLine
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed
import com.pathasan.app.ui.theme.PathAsanRedSoft
import com.pathasan.app.viewmodel.EvacuationRouteViewModel
import com.pathasan.app.viewmodel.LocationViewModel
import com.pathasan.app.viewmodel.OfflineMapsViewModel
import kotlin.math.ceil

@Composable
fun EvacuationRoutesScreen(
    language: AppLanguage,
    locationViewModel: LocationViewModel,
    routeViewModel: EvacuationRouteViewModel,
    offlineMapsViewModel: OfflineMapsViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val fil = language == AppLanguage.FILIPINO
    val context = LocalContext.current
    val locationState by locationViewModel.state.collectAsStateWithLifecycle()
    val routeState by routeViewModel.state.collectAsStateWithLifecycle()
    val offlineState by offlineMapsViewModel.state.collectAsStateWithLifecycle()
    val isOnline by rememberIsOnline()
    val destinations = EvacuationDestinations.all
    val selected = destinations.firstOrNull { it.id == routeState.selectedDestinationId }
    var focusRequest by remember { mutableStateOf<MapFocusRequest?>(null) }
    var focusCounter by remember { mutableLongStateOf(0L) }
    var outsideOfflineArea by remember { mutableStateOf(false) }

    fun hasPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    fun focus(latitude: Double, longitude: Double) {
        focusCounter += 1
        focusRequest = MapFocusRequest(latitude, longitude, focusCounter)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        locationViewModel.retry(hasPermission())
    }

    fun requestLocationAndFocus() {
        if (!hasPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            return
        }
        val actual = locationState as? LocationState.Available
        if (actual != null) {
            focus(actual.latitude, actual.longitude)
        } else {
            locationViewModel.retry(true)
        }
    }

    fun selectDestination(destination: EvacuationDestination) {
        routeViewModel.select(destination)
        focus(destination.latitude, destination.longitude)
    }

    LaunchedEffect(Unit) {
        locationViewModel.refresh(hasPermission())
        offlineMapsViewModel.refresh()
    }

    PathAsanScaffold(
        title = stringResource(R.string.evacuation_routes),
        currentRoute = "routes",
        onBack = onBack,
        onNavigate = onNavigate,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PathAsanMap(
                    language = language,
                    actualLocation = locationState as? LocationState.Available,
                    focusRequest = focusRequest,
                    isOnline = isOnline,
                    offlineMapReady = offlineState.isDownloaded,
                    destinations = destinations,
                    selectedDestinationId = selected?.id,
                    route = routeState.route,
                    onDestinationSelected = ::selectDestination,
                    onMyLocationRequested = ::requestLocationAndFocus,
                    onOutsideOfflineAreaChanged = { outsideOfflineArea = it },
                    modifier = Modifier.fillMaxWidth().height(390.dp),
                )
            }

            if (outsideOfflineArea) {
                item {
                    StatusNotice(
                        if (fil) "Nasa labas ka ng na-download na Baguio CBD map area."
                        else "You are outside the downloaded Baguio CBD map area."
                    )
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = { onNavigate("offline_maps") },
                        modifier = Modifier.weight(1f)
                            .background(PathAsanRedSoft, RoundedCornerShape(12.dp)),
                    ) {
                        Icon(Icons.Outlined.CloudDownload, null, tint = PathAsanRed)
                        Text(
                            if (offlineState.isDownloaded) {
                                if (fil) "Offline map" else "Offline map"
                            } else {
                                if (fil) "I-download ang mapa" else "Download map"
                            },
                            color = PathAsanRed,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    TextButton(
                        onClick = ::requestLocationAndFocus,
                        modifier = Modifier.weight(1f)
                            .background(PathAsanRedSoft, RoundedCornerShape(12.dp)),
                    ) {
                        Icon(Icons.Outlined.MyLocation, null, tint = PathAsanRed)
                        Text(
                            if (fil) "Aking lokasyon" else "My location",
                            color = PathAsanRed,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }

            item {
                StatusNotice(
                    if (fil) {
                        "Ang mga lokasyon sa ibaba ay mga mungkahi na kailangan pang beripikahin ng Baguio CDRRMO. Sundin ang opisyal na abiso sa emergency."
                    } else {
                        "The locations below are proposals requiring Baguio CDRRMO verification. Follow official instructions during an emergency."
                    }
                )
            }

            selected?.let { destination ->
                item(key = "selected-${destination.id}") {
                    SelectedDestinationCard(
                        destination = destination,
                        fil = fil,
                        locationState = locationState,
                        routeLoading = routeState.loading,
                        routeDistanceMeters = routeState.route?.distanceMeters,
                        routeDurationSeconds = routeState.route?.durationSeconds,
                        routeError = routeState.errorMessage,
                        onDirections = {
                            val location = locationState as? LocationState.Available
                            if (location == null) {
                                requestLocationAndFocus()
                            } else {
                                routeViewModel.requestRoute(location, destination, isOnline)
                            }
                        },
                    )
                }
            }

            item {
                Text(
                    if (fil) "Mga iminungkahing lokasyon" else "Proposed locations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            items(destinations, key = { it.id }) { destination ->
                DestinationListItem(
                    destination = destination,
                    selected = destination.id == selected?.id,
                    fil = fil,
                    onClick = { selectDestination(destination) },
                )
            }

            item {
                LocationStatusCard(
                    state = locationState,
                    fil = fil,
                    onAllow = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    },
                    onSettings = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    },
                    onRetry = { locationViewModel.retry(hasPermission()) },
                    onShow = { latitude, longitude -> focus(latitude, longitude) },
                )
            }
        }
    }
}

@Composable
private fun SelectedDestinationCard(
    destination: EvacuationDestination,
    fil: Boolean,
    locationState: LocationState,
    routeLoading: Boolean,
    routeDistanceMeters: Double?,
    routeDurationSeconds: Double?,
    routeError: String?,
    onDirections: () -> Unit,
) {
    SectionCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Place, null, tint = PathAsanRed)
            Text(
                destination.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Text(
            destination.description,
            color = PathAsanMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
        DesignationLabel(destination.designation, fil)

        if (routeDistanceMeters != null && routeDurationSeconds != null) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Route, null, tint = PathAsanRed)
                Text(
                    "${formatDistance(routeDistanceMeters)} · ${ceil(routeDurationSeconds / 60.0).toInt()} min walk",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        routeError?.let {
            Text(
                it,
                color = PathAsanRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        PrimaryButton(
            text = if (routeLoading) {
                if (fil) "Kinukuha ang ruta…" else "Getting route…"
            } else {
                if (fil) "Kumuha ng Walking Directions" else "Get Walking Directions"
            },
            onClick = onDirections,
            enabled = !routeLoading && locationState !is LocationState.Loading,
        )
    }
}

@Composable
private fun DestinationListItem(
    destination: EvacuationDestination,
    selected: Boolean,
    fil: Boolean,
    onClick: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) PathAsanRed else PathAsanLine,
                shape = RoundedCornerShape(14.dp),
            )
            .background(
                if (selected) PathAsanRedSoft else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Place,
                null,
                tint = if (selected) PathAsanRed else PathAsanBlack,
                modifier = Modifier.size(22.dp),
            )
            Text(
                destination.name,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                fontWeight = FontWeight.Bold,
            )
        }
        DesignationLabel(destination.designation, fil)
    }
}

@Composable
private fun DesignationLabel(designation: DestinationDesignation, fil: Boolean) {
    val label = when (designation) {
        DestinationDesignation.PROPOSED_UNVERIFIED ->
            if (fil) "Mungkahi · kailangan ng beripikasyon" else "Proposed · verification required"
        DestinationDesignation.OFFICIALLY_CONFIRMED ->
            if (fil) "Opisyal na nakumpirma" else "Officially confirmed"
    }
    Text(
        label,
        color = if (designation == DestinationDesignation.OFFICIALLY_CONFIRMED) {
            PathAsanBlack
        } else {
            PathAsanRed
        },
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun LocationStatusCard(
    state: LocationState,
    fil: Boolean,
    onAllow: () -> Unit,
    onSettings: () -> Unit,
    onRetry: () -> Unit,
    onShow: (Double, Double) -> Unit,
) {
    when (state) {
        LocationState.PermissionRequired -> SectionCard {
            Icon(Icons.Outlined.LocationOff, null, tint = PathAsanRed)
            Text(
                if (fil) "Kailangan ang location permission para sa panimulang punto ng ruta."
                else "Location permission is required to use your actual position as the route start.",
                modifier = Modifier.padding(vertical = 8.dp),
            )
            PrimaryButton(if (fil) "Payagan ang Lokasyon" else "Allow Location", onClick = onAllow)
        }
        LocationState.Loading -> Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(color = PathAsanRed)
        }
        LocationState.ServicesDisabled -> SectionCard {
            Text(
                if (fil) "Naka-off ang location services." else "Location services are turned off.",
                fontWeight = FontWeight.Bold,
            )
            PrimaryButton(
                if (fil) "Buksan ang Location Settings" else "Open Location Settings",
                onClick = onSettings,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        is LocationState.Error -> SectionCard {
            Text(state.message, color = PathAsanRed)
            PrimaryButton(
                if (fil) "Subukan Muli" else "Try Again",
                onClick = onRetry,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        is LocationState.Available -> SectionCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.MyLocation, null, tint = PathAsanRed)
                Column(Modifier.weight(1f)) {
                    Text(
                        if (fil) "Aktwal na lokasyon" else "Actual device location",
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "%.5f, %.5f · ±%.0f m".format(
                            state.latitude,
                            state.longitude,
                            state.accuracyMeters,
                        ),
                        color = PathAsanMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = { onShow(state.latitude, state.longitude) }) {
                    Text(if (fil) "Ipakita" else "Show", color = PathAsanRed)
                }
            }
        }
    }
}

@Composable
private fun StatusNotice(text: String) {
    Row(
        Modifier.fillMaxWidth()
            .background(PathAsanRedSoft, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Outlined.Warning, null, tint = PathAsanRed, modifier = Modifier.size(20.dp))
        Text(text, color = PathAsanBlack, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatDistance(meters: Double): String =
    if (meters >= 1_000.0) "%.1f km".format(meters / 1_000.0) else "${meters.toInt()} m"
