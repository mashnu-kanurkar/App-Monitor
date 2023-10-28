package com.redwater.appmonitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.AppRoomModel

@Dao
interface AppPrefsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appModel: AppRoomModel)

    @Query("SELECT * FROM ${Constants.appPrefsTable}")
    fun getAllRecords(): List<AppRoomModel>

    @Query("SELECT * FROM ${Constants.appPrefsTable} WHERE ${Constants.AppPrefsColumns.packageName} LIKE :packageName")
    fun getAppPrefsFor(packageName: String): AppRoomModel?

    @Query("DELETE FROM ${Constants.appPrefsTable} WHERE ${Constants.AppPrefsColumns.packageName} LIKE :packageName")
    fun deleteAppPrefs(packageName: String)
}