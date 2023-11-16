package com.redwater.appmonitor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.model.AppRoomModel
import com.redwater.appmonitor.data.model.OverlayPayload


@Database(entities = [AppRoomModel::class, OverlayPayload::class], version = 1, exportSchema = true)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAppPrefsDao(): AppPrefsDao

    abstract fun getOverlayDataDao(): OverlayDataDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(applicationContext: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, Constants.appDatabase
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}