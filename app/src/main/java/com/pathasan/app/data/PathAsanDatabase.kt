package com.pathasan.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
)

@Entity(tableName = "map_downloads")
data class MapDownloadEntity(
    @PrimaryKey val packageId: String,
    val downloadedAt: Long,
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hazardType: String,
    val severity: String,
    val affectedArea: String,
    val authority: String,
    val issuedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long?,
    val summary: String,
    val sourceUrl: String,
    val fetchedAtEpochMillis: Long,
)

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Query("SELECT * FROM accounts WHERE lower(email) = lower(:email) LIMIT 1")
    suspend fun findByEmail(email: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AccountEntity?
}

@Dao
interface MapDownloadDao {
    @Query("SELECT * FROM map_downloads")
    fun observeAll(): Flow<List<MapDownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(download: MapDownloadEntity)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY issuedAtEpochMillis DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(alerts: List<AlertEntity>)
}

@Database(
    entities = [AccountEntity::class, MapDownloadEntity::class, AlertEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class PathAsanDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun mapDownloadDao(): MapDownloadDao
    abstract fun alertDao(): AlertDao
}
