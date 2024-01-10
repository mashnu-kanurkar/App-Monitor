package com.redwater.appmonitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.model.AppRoomModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPrefsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appModel: AppRoomModel)

    @Query("SELECT * FROM ${Constants.appPrefsTable} WHERE ${Constants.AppPrefsColumns.isSelected} = 1")
    fun getAllSelectedRecords(): List<AppRoomModel>

    @Query("SELECT * FROM ${Constants.appPrefsTable} WHERE ${Constants.AppPrefsColumns.isSelected} = 1")
    fun getAllSelectedRecordsFlow(): Flow<List<AppRoomModel>>

    @Query("SELECT * FROM ${Constants.appPrefsTable}")
    fun getAllRecords(): List<AppRoomModel>

    @Query("SELECT * FROM ${Constants.appPrefsTable}")
    fun getAllRecordsFlow(): Flow<List<AppRoomModel>>

    @Query("SELECT * FROM ${Constants.appPrefsTable} WHERE ${Constants.AppPrefsColumns.packageName} LIKE :packageName")
    fun getAppPrefsFor(packageName: String): Flow<AppRoomModel?>

    @Query("DELETE FROM ${Constants.appPrefsTable} WHERE ${Constants.AppPrefsColumns.packageName} LIKE :packageName")
    fun deleteAppPrefs(packageName: String)

    @Query("UPDATE ${Constants.appPrefsTable} SET ${Constants.AppPrefsColumns.isSelected} = 0, ${Constants.AppPrefsColumns.thresholdTime} = null WHERE ${Constants.AppPrefsColumns.packageName} LIKE :packageName")
    fun unselectPrefsFor(packageName: String)

    @Query("UPDATE ${Constants.appPrefsTable} SET ${Constants.AppPrefsColumns.delay} = :delayInMin WHERE ${Constants.AppPrefsColumns.packageName} LIKE :packageName")
    fun updateDelay(packageName: String, delayInMin: Short)
}