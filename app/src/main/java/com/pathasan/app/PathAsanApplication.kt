package com.pathasan.app

import android.app.Application
import androidx.room.Room
import com.pathasan.app.data.AuthRepository
import com.pathasan.app.data.AlertRepository
import com.pathasan.app.data.MIGRATION_1_2
import com.pathasan.app.data.GeoapifyRepository
import com.pathasan.app.data.OfflineMapRepository
import com.pathasan.app.data.PathAsanDatabase
import com.pathasan.app.data.PreferencesRepository
import com.pathasan.app.data.UnconfiguredAlertSource
import com.pathasan.app.data.WeatherRepository
import org.maplibre.android.MapLibre

class PathAsanApplication : Application() {
    lateinit var database: PathAsanDatabase
    lateinit var preferencesRepository: PreferencesRepository
    lateinit var authRepository: AuthRepository
    lateinit var offlineMapRepository: OfflineMapRepository
    lateinit var alertRepository: AlertRepository
    lateinit var weatherRepository: WeatherRepository
    lateinit var geoapifyRepository: GeoapifyRepository

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        database = Room.databaseBuilder(this, PathAsanDatabase::class.java, "pathasan.db")
            .addMigrations(MIGRATION_1_2)
            .build()
        preferencesRepository = PreferencesRepository(this)
        authRepository = AuthRepository(database.accountDao(), preferencesRepository)
        offlineMapRepository = OfflineMapRepository(this)
        alertRepository = AlertRepository(database.alertDao(), UnconfiguredAlertSource())
        weatherRepository = WeatherRepository(this)
        geoapifyRepository = GeoapifyRepository()
    }
}
