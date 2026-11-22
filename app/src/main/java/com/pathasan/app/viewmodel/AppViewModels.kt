package com.pathasan.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pathasan.app.PathAsanApplication
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.AppPreferences
import com.pathasan.app.data.AlertRefreshResult
import com.pathasan.app.data.AlertRepository
import com.pathasan.app.data.AuthRepository
import com.pathasan.app.data.DisasterAlert
import com.pathasan.app.data.EvacuationDestination
import com.pathasan.app.data.GeoapifyRepository
import com.pathasan.app.data.OfflineMapRepository
import com.pathasan.app.data.PreferencesRepository
import com.pathasan.app.data.TextSize
import com.pathasan.app.data.UserSession
import com.pathasan.app.data.WeatherRepository
import com.pathasan.app.data.WeatherResult
import com.pathasan.app.data.WeatherSnapshot
import com.pathasan.app.data.WalkingRoute
import com.pathasan.app.location.LocationClient
import com.pathasan.app.location.LocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class AppUiState(
    val preferences: AppPreferences = AppPreferences(),
    val user: UserSession? = null,
    val restored: Boolean = false,
)

class AppViewModel(
    application: Application,
    private val preferences: PreferencesRepository,
    private val auth: AuthRepository,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.preferences.collect { prefs ->
                val user = prefs.sessionUserId?.let { auth.restoreSession(it) }
                _uiState.value = AppUiState(prefs, user, restored = true)
            }
        }
    }

    fun completeAuthentication(user: UserSession) {
        _uiState.value = _uiState.value.copy(user = user)
    }

    fun setLanguage(value: AppLanguage) = viewModelScope.launch { preferences.setLanguage(value) }
    fun setTextSize(value: TextSize) = viewModelScope.launch { preferences.setTextSize(value) }
    fun setNotifications(value: Boolean) = viewModelScope.launch { preferences.setNotifications(value) }
    fun logout(onComplete: () -> Unit) = viewModelScope.launch {
        auth.logout()
        _uiState.value = _uiState.value.copy(user = null)
        onComplete()
    }

    companion object {
        fun factory(app: PathAsanApplication) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AppViewModel(app, app.preferencesRepository, app.authRepository) as T
        }
    }
}

data class AuthUiState(val loading: Boolean = false, val error: String? = null)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun signUp(name: String, email: String, password: String, done: (UserSession) -> Unit) =
        submit { repository.signUp(name, email, password) }(done)

    fun login(email: String, password: String, done: (UserSession) -> Unit) =
        submit { repository.login(email, password) }(done)

    private fun submit(block: suspend () -> Result<UserSession>): ((UserSession) -> Unit) -> Unit = { done ->
        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)
            block().fold(
                onSuccess = {
                    _uiState.value = AuthUiState()
                    done(it)
                },
                onFailure = { _uiState.value = AuthUiState(error = it.message ?: "Unable to continue.") },
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    companion object {
        fun factory(repository: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
        }
    }
}

class OfflineMapsViewModel(private val repository: OfflineMapRepository) : ViewModel() {
    val state = repository.state

    fun download() = repository.download()
    fun cancel() = repository.cancel()
    fun remove() = repository.remove()
    fun refresh() = repository.refresh()

    companion object {
        fun factory(repository: OfflineMapRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OfflineMapsViewModel(repository) as T
        }
    }
}

enum class AlertFeedStatus { LOADING, READY, LIVE_UNAVAILABLE, ERROR }

data class DisasterAlertsUiState(
    val status: AlertFeedStatus = AlertFeedStatus.LOADING,
    val alerts: List<DisasterAlert> = emptyList(),
    val errorMessage: String? = null,
) {
    val isStale: Boolean
        get() = alerts.isNotEmpty() && status != AlertFeedStatus.READY
}

class DisasterAlertsViewModel(private val repository: AlertRepository) : ViewModel() {
    private val _state = MutableStateFlow(DisasterAlertsUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.cachedAlerts.collect { alerts ->
                _state.value = _state.value.copy(alerts = alerts)
            }
        }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(status = AlertFeedStatus.LOADING, errorMessage = null)
        when (val result = repository.refresh()) {
            is AlertRefreshResult.Success -> {
                _state.value = _state.value.copy(status = AlertFeedStatus.READY, errorMessage = null)
            }
            AlertRefreshResult.LiveSourceUnavailable -> {
                _state.value = _state.value.copy(status = AlertFeedStatus.LIVE_UNAVAILABLE)
            }
            is AlertRefreshResult.Error -> {
                _state.value = _state.value.copy(
                    status = AlertFeedStatus.ERROR,
                    errorMessage = result.message,
                )
            }
        }
    }

    companion object {
        fun factory(repository: AlertRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DisasterAlertsViewModel(repository) as T
        }
    }
}

data class WeatherUiState(
    val loading: Boolean = true,
    val weather: WeatherSnapshot? = null,
    val isStale: Boolean = false,
    val errorMessage: String? = null,
)

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _state = MutableStateFlow(WeatherUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, errorMessage = null)
        _state.value = when (val result = repository.fetchBaguioWeather()) {
            is WeatherResult.Success -> WeatherUiState(
                loading = false,
                weather = result.weather,
                isStale = result.isStale,
                errorMessage = result.message,
            )
            is WeatherResult.Error -> _state.value.copy(
                loading = false,
                errorMessage = result.message,
            )
        }
    }

    companion object {
        fun factory(repository: WeatherRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                WeatherViewModel(repository) as T
        }
    }
}

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val client = LocationClient(application)
    private val _state = MutableStateFlow<LocationState>(LocationState.PermissionRequired)
    val state = _state.asStateFlow()
    private var updatesJob: Job? = null
    private var timeoutJob: Job? = null

    fun permissionDenied() {
        stopUpdates()
        _state.value = LocationState.PermissionRequired
    }

    fun refresh(hasPermission: Boolean) {
        if (!hasPermission) {
            permissionDenied()
            return
        }
        if (!client.servicesEnabled()) {
            stopUpdates()
            _state.value = LocationState.ServicesDisabled
            return
        }
        if (updatesJob?.isActive == true) return
        _state.value = LocationState.Loading
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(15_000)
            if (_state.value == LocationState.Loading) {
                _state.value = LocationState.Error(
                    "Location timed out. Move to an open area and try again."
                )
            }
        }
        updatesJob = viewModelScope.launch {
            client.locationUpdates()
                .catch {
                    _state.value = LocationState.Error(
                        "Device location is unavailable. Check Android location settings."
                    )
                }
                .collect { location ->
                    timeoutJob?.cancel()
                    _state.value = LocationState.Available(
                        location.latitude,
                        location.longitude,
                        location.accuracy,
                    )
                }
        }
    }

    fun retry(hasPermission: Boolean) {
        stopUpdates()
        refresh(hasPermission)
    }

    private fun stopUpdates() {
        updatesJob?.cancel()
        timeoutJob?.cancel()
        updatesJob = null
        timeoutJob = null
    }

    override fun onCleared() {
        stopUpdates()
        super.onCleared()
    }
}

data class EvacuationRouteUiState(
    val selectedDestinationId: String? = null,
    val loading: Boolean = false,
    val route: WalkingRoute? = null,
    val errorMessage: String? = null,
)

class EvacuationRouteViewModel(
    private val repository: GeoapifyRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EvacuationRouteUiState())
    val state = _state.asStateFlow()
    private var routeJob: Job? = null

    fun select(destination: EvacuationDestination) {
        if (_state.value.selectedDestinationId == destination.id) return
        routeJob?.cancel()
        _state.value = EvacuationRouteUiState(selectedDestinationId = destination.id)
    }

    fun requestRoute(
        start: LocationState.Available,
        destination: EvacuationDestination,
        isOnline: Boolean,
    ) {
        routeJob?.cancel()
        if (!isOnline) {
            _state.value = _state.value.copy(
                loading = false,
                route = null,
                errorMessage = "Walking directions require an internet connection.",
            )
            return
        }
        routeJob = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, route = null, errorMessage = null)
            when (val result = repository.walkingRoute(
                start.latitude,
                start.longitude,
                destination,
            )) {
                is com.pathasan.app.data.RouteResult.Success -> {
                    _state.value = _state.value.copy(
                        loading = false,
                        route = result.route,
                        errorMessage = null,
                    )
                }
                is com.pathasan.app.data.RouteResult.Error -> {
                    _state.value = _state.value.copy(
                        loading = false,
                        route = null,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        routeJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(repository: GeoapifyRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EvacuationRouteViewModel(repository) as T
        }
    }
}
