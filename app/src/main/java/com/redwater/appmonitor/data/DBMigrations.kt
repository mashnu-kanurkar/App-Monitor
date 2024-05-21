package com.redwater.appmonitor.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration script to update the app_prefs table
// Please ensure to increment the version number accordingly

val migration1to2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create a temporary table with the expected structure
        database.execSQL("CREATE TABLE IF NOT EXISTS app_prefs_temp (" +
                "package TEXT NOT NULL DEFAULT 'undefined'," +
                "name TEXT NOT NULL DEFAULT 'undefined'," +
                "is_selected INTEGER NOT NULL DEFAULT 0," +
                "thr_time INTEGER," +
                "delay INTEGER NOT NULL DEFAULT 0," +
                "dnd_start_time TEXT," +
                "dnd_end_time TEXT," +
                //"usage_time INTEGER NOT NULL DEFAULT 0," + // new column
                "PRIMARY KEY(package, name)" +
                ")")

        // Copy data from the old table to the temporary table
        database.execSQL("INSERT INTO app_prefs_temp " +
                "(package, name, is_selected, thr_time, delay, dnd_start_time, dnd_end_time) " +
                "SELECT package, name, is_selected, thr_time, delay, null, null FROM app_prefs")

        // Drop the old table
        database.execSQL("DROP TABLE IF EXISTS app_prefs")

        // Rename the temporary table to the original table name
        database.execSQL("ALTER TABLE app_prefs_temp RENAME TO app_prefs")
    }
}
