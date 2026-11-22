package com.pathasan.app.navigation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pathasan.app.PathAsanApplication
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.ui.screens.DisasterAlertsScreen
import com.pathasan.app.ui.screens.EmergencyGuideScreen
import com.pathasan.app.ui.screens.EmergencyContactsScreen
import com.pathasan.app.ui.screens.EvacuationRoutesScreen
import com.pathasan.app.ui.screens.ForgotPasswordScreen
import com.pathasan.app.ui.screens.HomeScreen
import com.pathasan.app.ui.screens.LoginScreen
import com.pathasan.app.ui.screens.OfflineMapsScreen
import com.pathasan.app.ui.screens.SettingsScreen
import com.pathasan.app.ui.screens.SignUpScreen
import com.pathasan.app.ui.screens.WelcomeScreen
import com.pathasan.app.viewmodel.AppUiState
import com.pathasan.app.viewmodel.AppViewModel
import com.pathasan.app.viewmodel.AuthViewModel
import com.pathasan.app.viewmodel.DisasterAlertsViewModel
import com.pathasan.app.viewmodel.LocationViewModel
import com.pathasan.app.viewmodel.EvacuationRouteViewModel
import com.pathasan.app.viewmodel.OfflineMapsViewModel
import com.pathasan.app.viewmodel.WeatherViewModel

object Routes {
    const val Welcome = "welcome"
    const val SignUp = "sign_up"
    const val Login = "login"
    const val Forgot = "forgot"
    const val Home = "home"
    const val EvacuationRoutes = "routes"
    const val OfflineMaps = "offline_maps"
    const val Alerts = "alerts"
    const val Guide = "guide"
    const val Contacts = "contacts"
    const val Settings = "settings"
}

@Composable
fun AppNavigation(
    appState: AppUiState,
    appViewModel: AppViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val app = LocalContext.current.applicationContext as PathAsanApplication
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(app.authRepository))
    val offlineMapsViewModel: OfflineMapsViewModel =
        viewModel(factory = OfflineMapsViewModel.factory(app.offlineMapRepository))
    val locationViewModel: LocationViewModel = viewModel()
    val evacuationRouteViewModel: EvacuationRouteViewModel =
        viewModel(factory = EvacuationRouteViewModel.factory(app.geoapifyRepository))
    val alertsViewModel: DisasterAlertsViewModel =
        viewModel(factory = DisasterAlertsViewModel.factory(app.alertRepository))
    val weatherViewModel: WeatherViewModel =
        viewModel(factory = WeatherViewModel.factory(app.weatherRepository))
    val alertsState by alertsViewModel.state.collectAsStateWithLifecycle()
    val start = if (appState.user == null) Routes.Welcome else Routes.Home

    fun toHome() {
        navController.navigate(Routes.Home) {
            popUpTo(Routes.Welcome) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun bottomNavigate(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.Home) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun setLanguage(language: AppLanguage) {
        appViewModel.setLanguage(language)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.Welcome) {
            WelcomeScreen(
                language = appState.preferences.language,
                onLanguageChange = ::setLanguage,
                onSignUp = { navController.navigate(Routes.SignUp) },
                onLogin = { navController.navigate(Routes.Login) },
            )
        }
        composable(Routes.SignUp) {
            SignUpScreen(
                language = appState.preferences.language,
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(Routes.Login) },
                onSuccess = {
                    appViewModel.completeAuthentication(it)
                    toHome()
                },
            )
        }
        composable(Routes.Login) {
            LoginScreen(
                language = appState.preferences.language,
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSignUp = { navController.navigate(Routes.SignUp) },
                onForgot = { navController.navigate(Routes.Forgot) },
                onSuccess = {
                    appViewModel.completeAuthentication(it)
                    toHome()
                },
            )
        }
        composable(Routes.Forgot) {
            ForgotPasswordScreen(
                language = appState.preferences.language,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                language = appState.preferences.language,
                user = appState.user,
                alertsState = alertsState,
                onNavigate = ::bottomNavigate,
                onLogout = {
                    appViewModel.logout {
                        navController.navigate(Routes.Welcome) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Routes.EvacuationRoutes) {
            EvacuationRoutesScreen(
                appState.preferences.language,
                locationViewModel,
                evacuationRouteViewModel,
                offlineMapsViewModel,
                onBack = { navController.popBackStack() },
                onNavigate = ::bottomNavigate,
            )
        }
        composable(Routes.OfflineMaps) {
            OfflineMapsScreen(
                appState.preferences.language,
                offlineMapsViewModel,
                onBack = { navController.popBackStack() },
                onNavigate = ::bottomNavigate,
            )
        }
        composable(Routes.Alerts) {
            DisasterAlertsScreen(
                appState.preferences.language,
                alertsViewModel,
                weatherViewModel,
                onBack = { navController.popBackStack() },
                onNavigate = ::bottomNavigate,
            )
        }
        composable(Routes.Guide) {
            EmergencyGuideScreen(
                appState.preferences.language,
                onBack = { navController.popBackStack() },
                onNavigate = ::bottomNavigate,
            )
        }
        composable(Routes.Contacts) {
            EmergencyContactsScreen(
                appState.preferences.language,
                onBack = { navController.popBackStack() },
                onNavigate = ::bottomNavigate,
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                preferences = appState.preferences,
                onLanguageChange = ::setLanguage,
                onTextSizeChange = appViewModel::setTextSize,
                onNotificationsChange = appViewModel::setNotifications,
                onLogout = {
                    appViewModel.logout {
                        navController.navigate(Routes.Welcome) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
