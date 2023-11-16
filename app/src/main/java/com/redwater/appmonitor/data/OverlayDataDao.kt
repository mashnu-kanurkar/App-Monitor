package com.redwater.appmonitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.model.OverlayPayload

@Dao
interface OverlayDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(overlayPayload: OverlayPayload)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(overlayPayloadList: List<OverlayPayload>)

    @Query("UPDATE ${Constants.overlayDataTable} SET ${Constants.OverlayDataColumns.isUsed} = 1 WHERE ${Constants.OverlayDataColumns.id} =:id")
    fun markAsUsed(id: Int)

    @Query("SELECT * FROM ${Constants.overlayDataTable} WHERE ${Constants.OverlayDataColumns.id} LIKE :id")
    fun getOverlayPayload(id: Int): OverlayPayload

    @Query("SELECT * FROM ${Constants.overlayDataTable} WHERE NOT ${Constants.OverlayDataColumns.isUsed} AND (${Constants.OverlayDataColumns.difficultyLevel} BETWEEN :difficultyLevelMin AND :difficultyLevelMax) ORDER BY ${Constants.OverlayDataColumns.difficultyLevel} LIMIT 1")
    fun getGreaterDifficultyOverlayData(difficultyLevelMin: Short, difficultyLevelMax: Short): OverlayPayload?

    @Query("SELECT MAX(${Constants.OverlayDataColumns.id}) FROM ${Constants.overlayDataTable} WHERE ${Constants.OverlayDataColumns.type} =:type")
    fun getLastEntryID(type: String): Int

    @Query("SELECT CASE COUNT(*) WHEN 0 THEN 71 ELSE COUNT(*)*100/( SELECT COUNT(*) FROM ${Constants.overlayDataTable} WHERE ${Constants.OverlayDataColumns.type} =:type) END FROM ${Constants.overlayDataTable} WHERE ${Constants.OverlayDataColumns.isUsed} =1")
    fun getUsagePercentageOf(type: String): Int

}