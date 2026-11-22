package com.pathasan.app.data

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import java.nio.charset.StandardCharsets

const val OPEN_FREE_MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/bright"

object BaguioCbdOfflineArea {
    const val id = "baguio-cbd-v2"
    const val name = "Baguio CBD"
    const val north = 16.421
    const val south = 16.392
    const val east = 120.612
    const val west = 120.584
    const val minZoom = 12.0
    const val maxZoom = 16.0

    val bounds: LatLngBounds
        get() = LatLngBounds.Builder()
            .include(LatLng(north, east))
            .include(LatLng(south, west))
            .build()
}

enum class OfflineDownloadStatus {
    CHECKING,
    NOT_DOWNLOADED,
    DOWNLOADING,
    PAUSED,
    COMPLETE,
    REMOVING,
    ERROR,
}

data class OfflineMapState(
    val status: OfflineDownloadStatus = OfflineDownloadStatus.CHECKING,
    val progressPercent: Int = 0,
    val completedBytes: Long = 0,
    val completedResources: Long = 0,
    val requiredResources: Long = 0,
    val availableStorageBytes: Long = 0,
    val errorMessage: String? = null,
) {
    val isDownloaded: Boolean get() = status == OfflineDownloadStatus.COMPLETE
}

class OfflineMapRepository(context: Context) {
    private val appContext = context.applicationContext
    private val manager = OfflineManager.getInstance(appContext)
    private val _state = MutableStateFlow(
        OfflineMapState(availableStorageBytes = availableStorage())
    )
    val state: StateFlow<OfflineMapState> = _state.asStateFlow()
    private var region: OfflineRegion? = null
    private var cancelRequested = false

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(
            status = OfflineDownloadStatus.CHECKING,
            availableStorageBytes = availableStorage(),
            errorMessage = null,
        )
        manager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                region = offlineRegions.orEmpty().firstOrNull(::isBaguioRegion)
                val existing = region
                if (existing == null) {
                    _state.value = OfflineMapState(
                        status = OfflineDownloadStatus.NOT_DOWNLOADED,
                        availableStorageBytes = availableStorage(),
                    )
                } else {
                    observe(existing)
                    existing.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                        override fun onStatus(status: OfflineRegionStatus?) {
                            if (status != null) updateFromStatus(status)
                        }

                        override fun onError(error: String?) {
                            fail(error ?: "Unable to read the offline map status.")
                        }
                    })
                }
            }

            override fun onError(error: String) = fail(error)
        })
    }

    fun download() {
        cancelRequested = false
        val existing = region
        if (existing != null) {
            if (_state.value.isDownloaded) return
            observe(existing)
            _state.value = _state.value.copy(status = OfflineDownloadStatus.DOWNLOADING, errorMessage = null)
            existing.setDownloadState(OfflineRegion.STATE_ACTIVE)
            return
        }

        if (_state.value.status == OfflineDownloadStatus.DOWNLOADING) return
        _state.value = _state.value.copy(status = OfflineDownloadStatus.DOWNLOADING, errorMessage = null)
        val definition = OfflineTilePyramidRegionDefinition(
            OPEN_FREE_MAP_STYLE_URL,
            BaguioCbdOfflineArea.bounds,
            BaguioCbdOfflineArea.minZoom,
            BaguioCbdOfflineArea.maxZoom,
            appContext.resources.displayMetrics.density.coerceAtMost(2f),
        )
        val metadata = """{"id":"${BaguioCbdOfflineArea.id}","name":"${BaguioCbdOfflineArea.name}"}"""
            .toByteArray(StandardCharsets.UTF_8)
        manager.createOfflineRegion(
            definition,
            metadata,
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    region = offlineRegion
                    if (cancelRequested) {
                        deleteRegion(offlineRegion)
                    } else {
                        observe(offlineRegion)
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                    }
                }

                override fun onError(error: String) = fail(error)
            },
        )
    }

    fun cancel() {
        cancelRequested = true
        val existing = region
        if (existing == null) {
            _state.value = _state.value.copy(status = OfflineDownloadStatus.REMOVING)
        } else {
            deleteRegion(existing)
        }
    }

    fun remove() {
        val existing = region ?: return
        cancelRequested = false
        deleteRegion(existing)
    }

    private fun deleteRegion(existing: OfflineRegion) {
        existing.setDownloadState(OfflineRegion.STATE_INACTIVE)
        _state.value = _state.value.copy(status = OfflineDownloadStatus.REMOVING, errorMessage = null)
        existing.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
            override fun onDelete() {
                region = null
                cancelRequested = false
                _state.value = OfflineMapState(
                    status = OfflineDownloadStatus.NOT_DOWNLOADED,
                    availableStorageBytes = availableStorage(),
                )
            }

            override fun onError(error: String) = fail(error)
        })
    }

    private fun observe(offlineRegion: OfflineRegion) {
        offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
            override fun onStatusChanged(status: OfflineRegionStatus) {
                updateFromStatus(status)
                if (status.requiredResourceCount > 0 && status.isComplete) {
                    offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                }
            }

            override fun onError(error: OfflineRegionError) {
                offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                fail(error.message)
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                fail("The map provider's offline tile limit was reached.")
            }
        })
    }

    private fun updateFromStatus(status: OfflineRegionStatus) {
        val required = status.requiredResourceCount
        val isComplete = required > 0 && status.isComplete
        val progress = if (required > 0) {
            ((status.completedResourceCount * 100L) / required).toInt().coerceIn(0, 100)
        } else {
            0
        }
        val stateStatus = when {
            isComplete -> OfflineDownloadStatus.COMPLETE
            status.downloadState == OfflineRegion.STATE_ACTIVE -> OfflineDownloadStatus.DOWNLOADING
            else -> OfflineDownloadStatus.PAUSED
        }
        _state.value = OfflineMapState(
            status = stateStatus,
            progressPercent = if (isComplete) 100 else progress,
            completedBytes = status.completedResourceSize,
            completedResources = status.completedResourceCount,
            requiredResources = required,
            availableStorageBytes = availableStorage(),
        )
    }

    private fun isBaguioRegion(candidate: OfflineRegion): Boolean =
        String(candidate.metadata, StandardCharsets.UTF_8).contains(BaguioCbdOfflineArea.id)

    private fun fail(message: String) {
        _state.value = _state.value.copy(
            status = OfflineDownloadStatus.ERROR,
            availableStorageBytes = availableStorage(),
            errorMessage = message,
        )
    }

    private fun availableStorage(): Long {
        val stats = StatFs(appContext.filesDir.absolutePath)
        return stats.availableBytes
    }
}
