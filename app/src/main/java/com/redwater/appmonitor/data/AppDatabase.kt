package com.redwater.appmonitor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.dao.AppPrefsDao
import com.redwater.appmonitor.data.dao.BlogDao
import com.redwater.appmonitor.data.dao.OverlayDataDao
import com.redwater.appmonitor.data.dao.QuotesDao
import com.redwater.appmonitor.data.model.AppRoomModel
import com.redwater.appmonitor.data.model.Blog
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.data.model.Quote


@Database(entities = [AppRoomModel::class, OverlayPayload::class, Quote::class, Blog::class],
    version = 2,
    exportSchema = true,
    )
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAppPrefsDao(): AppPrefsDao

    abstract fun getOverlayDataDao(): OverlayDataDao
    abstract fun quotesDao(): QuotesDao

    abstract fun blogDao(): BlogDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(applicationContext: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    Constants.appDatabase)
                    .fallbackToDestructiveMigration()
                    //.addMigrations(migration1to2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

