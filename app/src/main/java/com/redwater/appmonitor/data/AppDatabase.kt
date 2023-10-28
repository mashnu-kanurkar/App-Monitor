package com.redwater.appmonitor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.AppRoomModel


@Database(entities = [AppRoomModel::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAppPrefsDao(): AppPrefsDao

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