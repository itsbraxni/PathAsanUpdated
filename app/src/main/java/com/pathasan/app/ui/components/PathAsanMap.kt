package com.pathasan.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.BaguioCbdOfflineArea
import com.pathasan.app.data.EvacuationDestination
import com.pathasan.app.data.OPEN_FREE_MAP_STYLE_URL
import com.pathasan.app.data.WalkingRoute
import com.pathasan.app.location.LocationState
import com.pathasan.app.ui.theme.PathAsanBlack
import com.pathasan.app.ui.theme.PathAsanLightGray
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed
import com.pathasan.app.ui.theme.PathAsanWhite
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Icon as MapIcon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polygon
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

private const val BAGUIO_CBD_LATITUDE = 16.4065
private const val BAGUIO_CBD_LONGITUDE = 120.5980
private const val BAGUIO_CBD_ZOOM = 15.0

data class MapFocusRequest(
    val latitude: Double,
    val longitude: Double,
    val requestId: Long,
)

@Composable
fun PathAsanMap(
    language: AppLanguage,
    actualLocation: LocationState.Available?,
    focusRequest: MapFocusRequest?,
    isOnline: Boolean,
    offlineMapReady: Boolean,
    destinations: List<EvacuationDestination> = emptyList(),
    selectedDestinationId: String? = null,
    route: WalkingRoute? = null,
    onDestinationSelected: (EvacuationDestination) -> Unit = {},
    showDownloadBoundary: Boolean = false,
    showMyLocationButton: Boolean = true,
    onMyLocationRequested: () -> Unit,
    onOutsideOfflineAreaChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var styleLoaded by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var deviceMarker by remember { mutableStateOf<org.maplibre.android.annotations.Marker?>(null) }
    var boundaryPolygon by remember { mutableStateOf<Polygon?>(null) }
    var destinationMarkers by remember {
        mutableStateOf<Map<Long, EvacuationDestination>>(emptyMap())
    }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    val deviceIcon = remember { createPinIcon(context, selected = false, device = true) }
    val destinationIcon = remember { createPinIcon(context, selected = false, device = false) }
    val selectedDestinationIcon = remember { createPinIcon(context, selected = true, device = false) }
    val currentDestinationSelected by rememberUpdatedState(onDestinationSelected)
    val mapView = remember {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply {
            onCreate(null)
            addOnDidFailLoadingMapListener { error ->
                styleLoaded = false
                mapError = error
            }
            getMapAsync { readyMap ->
                map = readyMap
                readyMap.uiSettings.isScrollGesturesEnabled = true
                readyMap.uiSettings.isZoomGesturesEnabled = true
                readyMap.uiSettings.isRotateGesturesEnabled = true
                readyMap.uiSettings.isCompassEnabled = true
                readyMap.uiSettings.isAttributionEnabled = true
                readyMap.uiSettings.isLogoEnabled = true
                readyMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(BAGUIO_CBD_LATITUDE, BAGUIO_CBD_LONGITUDE),
                        BAGUIO_CBD_ZOOM,
                    )
                )
                readyMap.setStyle(OPEN_FREE_MAP_STYLE_URL) {
                    styleLoaded = true
                    mapError = null
                }
            }
        }
    }

    MapViewLifecycle(mapView, lifecycle)

    DisposableEffect(map, styleLoaded, showDownloadBoundary) {
        val readyMap = map
        if (readyMap == null || !styleLoaded) return@DisposableEffect onDispose { }
        if (showDownloadBoundary) {
            boundaryPolygon = readyMap.addPolygon(
                PolygonOptions()
                    .add(
                        LatLng(BaguioCbdOfflineArea.north, BaguioCbdOfflineArea.west),
                        LatLng(BaguioCbdOfflineArea.north, BaguioCbdOfflineArea.east),
                        LatLng(BaguioCbdOfflineArea.south, BaguioCbdOfflineArea.east),
                        LatLng(BaguioCbdOfflineArea.south, BaguioCbdOfflineArea.west),
                    )
                    .fillColor(android.graphics.Color.argb(38, 225, 6, 22))
                    .strokeColor(android.graphics.Color.rgb(225, 6, 22))
            )
            readyMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(BaguioCbdOfflineArea.bounds, 48),
                500,
            )
        }
        onDispose {
            boundaryPolygon?.let(readyMap::removePolygon)
            boundaryPolygon = null
        }
    }

    LaunchedEffect(actualLocation, map, styleLoaded) {
        val readyMap = map ?: return@LaunchedEffect
        val available = actualLocation
        if (!styleLoaded || available == null) {
            deviceMarker?.let(readyMap::removeMarker)
            deviceMarker = null
            return@LaunchedEffect
        }
        val position = LatLng(available.latitude, available.longitude)
        val existing = deviceMarker
        if (existing == null) {
            deviceMarker = readyMap.addMarker(
                MarkerOptions().position(position).title("My location").icon(deviceIcon)
            )
        } else {
            existing.position = position
        }
    }

    DisposableEffect(map, styleLoaded, destinations, selectedDestinationId) {
        val readyMap = map
        if (readyMap == null || !styleLoaded) return@DisposableEffect onDispose { }
        val markerRecords = destinations.map { destination ->
            destination to readyMap.addMarker(
                MarkerOptions()
                    .position(LatLng(destination.latitude, destination.longitude))
                    .title(destination.name)
                    .snippet("Tap for destination details")
                    .icon(
                        if (destination.id == selectedDestinationId) {
                            selectedDestinationIcon
                        } else {
                            destinationIcon
                        }
                    )
            )
        }
        val markers = markerRecords.associate { (destination, marker) -> marker.id to destination }
        destinationMarkers = markers
        readyMap.setOnMarkerClickListener { marker ->
            markers[marker.id]?.let {
                currentDestinationSelected(it)
                true
            } ?: false
        }
        onDispose {
            markerRecords.forEach { (_, marker) -> readyMap.removeMarker(marker) }
            destinationMarkers = emptyMap()
            readyMap.setOnMarkerClickListener { false }
        }
    }

    DisposableEffect(map, styleLoaded, route) {
        val readyMap = map
        val currentRoute = route
        if (readyMap == null || !styleLoaded || currentRoute == null) {
            return@DisposableEffect onDispose { }
        }
        val coordinates = currentRoute.points.map { LatLng(it.latitude, it.longitude) }
        routePolyline = readyMap.addPolyline(
            PolylineOptions()
                .addAll(coordinates)
                .color(android.graphics.Color.rgb(0, 122, 150))
                .width(7f)
        )
        if (coordinates.size >= 2) {
            val bounds = LatLngBounds.Builder().includes(coordinates).build()
            readyMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80), 700)
        }
        onDispose {
            routePolyline?.let(readyMap::removePolyline)
            routePolyline = null
        }
    }

    LaunchedEffect(focusRequest, map) {
        val request = focusRequest ?: return@LaunchedEffect
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(request.latitude, request.longitude),
                BAGUIO_CBD_ZOOM + 1,
            ),
            650,
        )
    }

    DisposableEffect(map, isOnline, offlineMapReady) {
        val readyMap = map ?: return@DisposableEffect onDispose { }
        val listener = MapLibreMap.OnCameraIdleListener {
            val target = readyMap.cameraPosition.target
            val outside = !isOnline && offlineMapReady &&
                target != null && !BaguioCbdOfflineArea.bounds.contains(target)
            onOutsideOfflineAreaChanged(outside)
        }
        readyMap.addOnCameraIdleListener(listener)
        onDispose { readyMap.removeOnCameraIdleListener(listener) }
    }

    Box(modifier.clip(RoundedCornerShape(20.dp)).background(PathAsanLightGray)) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        Text(
            if (!isOnline) "OFFLINE" else "ONLINE",
            color = if (!isOnline) PathAsanRed else PathAsanBlack,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.TopStart)
                .padding(12.dp)
                .background(PathAsanWhite, RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 6.dp),
        )

        if (showMyLocationButton) {
            IconButton(
                onClick = onMyLocationRequested,
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(PathAsanWhite, RoundedCornerShape(12.dp)),
            ) {
                Icon(
                    Icons.Outlined.MyLocation,
                    contentDescription = if (language == AppLanguage.FILIPINO) {
                        "Aking lokasyon"
                    } else {
                        "My location"
                    },
                    tint = PathAsanRed,
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(10.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            IconButton(
                onClick = { map?.animateCamera(CameraUpdateFactory.zoomIn(), 180) },
                modifier = Modifier.background(PathAsanWhite, RoundedCornerShape(12.dp)),
            ) {
                Icon(
                    Icons.Outlined.Add,
                    if (language == AppLanguage.FILIPINO) "Palakihin ang mapa" else "Zoom in",
                    tint = PathAsanBlack,
                )
            }
            IconButton(
                onClick = { map?.animateCamera(CameraUpdateFactory.zoomOut(), 180) },
                modifier = Modifier.background(PathAsanWhite, RoundedCornerShape(12.dp)),
            ) {
                Icon(
                    Icons.Outlined.Remove,
                    if (language == AppLanguage.FILIPINO) "Paliitin ang mapa" else "Zoom out",
                    tint = PathAsanBlack,
                )
            }
        }

        if (!styleLoaded) {
            Box(
                Modifier.fillMaxSize().background(Color(0xDDF4F4F4)),
                contentAlignment = Alignment.Center,
            ) {
                if (mapError == null) {
                    CircularProgressIndicator(color = PathAsanRed)
                } else {
                    Text(
                        if (language == AppLanguage.FILIPINO)
                            "Hindi ma-load ang mapa. Kumonekta sa internet o i-download muna ang Baguio CBD map."
                        else "The map could not load. Connect to the internet or download the Baguio CBD map first.",
                        color = PathAsanMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MapViewLifecycle(mapView: MapView, lifecycle: Lifecycle) {
    DisposableEffect(lifecycle, mapView) {
        var started = false
        var resumed = false
        var destroyed = false

        fun start() {
            if (!started) {
                mapView.onStart()
                started = true
            }
        }

        fun resume() {
            if (!resumed) {
                mapView.onResume()
                resumed = true
            }
        }

        fun pause() {
            if (resumed) {
                mapView.onPause()
                resumed = false
            }
        }

        fun stop() {
            if (started) {
                mapView.onStop()
                started = false
            }
        }

        fun destroy() {
            if (!destroyed) {
                mapView.onDestroy()
                destroyed = true
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_RESUME -> resume()
                Lifecycle.Event.ON_PAUSE -> pause()
                Lifecycle.Event.ON_STOP -> stop()
                Lifecycle.Event.ON_DESTROY -> destroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) resume()
        onDispose {
            lifecycle.removeObserver(observer)
            pause()
            stop()
            destroy()
        }
    }
}

private fun createPinIcon(
    context: android.content.Context,
    selected: Boolean,
    device: Boolean,
): MapIcon {
    val density = context.resources.displayMetrics.density
    val size = ((if (selected) 42 else 34) * density).toInt().coerceAtLeast(34)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = when {
        device -> android.graphics.Color.rgb(38, 132, 255)
        selected -> android.graphics.Color.rgb(21, 21, 21)
        else -> android.graphics.Color.rgb(225, 6, 22)
    }
    canvas.drawCircle(size / 2f, size / 2f, size * .42f, paint)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size * .17f, paint)
    return IconFactory.getInstance(context).fromBitmap(bitmap)
}
