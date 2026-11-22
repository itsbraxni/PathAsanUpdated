package com.pathasan.app.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS alerts (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                hazardType TEXT NOT NULL,
                severity TEXT NOT NULL,
                affectedArea TEXT NOT NULL,
                authority TEXT NOT NULL,
                issuedAtEpochMillis INTEGER NOT NULL,
                updatedAtEpochMillis INTEGER,
                summary TEXT NOT NULL,
                sourceUrl TEXT NOT NULL,
                fetchedAtEpochMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
