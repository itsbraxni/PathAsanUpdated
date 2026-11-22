package com.pathasan.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface AlertRefreshResult {
    data class Success(val alerts: List<DisasterAlert>) : AlertRefreshResult
    data object LiveSourceUnavailable : AlertRefreshResult
    data class Error(val message: String) : AlertRefreshResult
}

interface AlertSource {
    suspend fun fetchAlerts(): AlertRefreshResult
}

class UnconfiguredAlertSource : AlertSource {
    override suspend fun fetchAlerts(): AlertRefreshResult = AlertRefreshResult.LiveSourceUnavailable
}

class AlertRepository(
    private val dao: AlertDao,
    private val source: AlertSource,
) {
    val cachedAlerts: Flow<List<DisasterAlert>> = dao.observeAll().map { entities ->
        entities.map { entity ->
            DisasterAlert(
                id = entity.id,
                title = entity.title,
                hazardType = entity.hazardType,
                severity = AlertSeverity.valueOf(entity.severity),
                affectedArea = entity.affectedArea,
                authority = entity.authority,
                issuedAtEpochMillis = entity.issuedAtEpochMillis,
                updatedAtEpochMillis = entity.updatedAtEpochMillis,
                summary = entity.summary,
                sourceUrl = entity.sourceUrl,
                fetchedAtEpochMillis = entity.fetchedAtEpochMillis,
            )
        }
    }

    suspend fun refresh(): AlertRefreshResult {
        val result = source.fetchAlerts()
        if (result is AlertRefreshResult.Success) {
            dao.upsertAll(result.alerts.map { alert ->
                AlertEntity(
                    id = alert.id,
                    title = alert.title,
                    hazardType = alert.hazardType,
                    severity = alert.severity.name,
                    affectedArea = alert.affectedArea,
                    authority = alert.authority,
                    issuedAtEpochMillis = alert.issuedAtEpochMillis,
                    updatedAtEpochMillis = alert.updatedAtEpochMillis,
                    summary = alert.summary,
                    sourceUrl = alert.sourceUrl,
                    fetchedAtEpochMillis = alert.fetchedAtEpochMillis,
                )
            })
        }
        return result
    }
}
