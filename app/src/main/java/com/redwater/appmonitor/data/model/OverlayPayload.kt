package com.redwater.appmonitor.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.redwater.appmonitor.Constants

@Entity(tableName = Constants.overlayDataTable)
data class OverlayPayload(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.OverlayDataColumns.id)
    @SerializedName("id") val id: Int = -1,

    @ColumnInfo(name = Constants.OverlayDataColumns.type)
    @SerializedName("type") val type: String = "puzzle",

    @ColumnInfo(name = Constants.OverlayDataColumns.subType)
    @SerializedName("sub_type") val subType: String = "text",

    @ColumnInfo(name = Constants.OverlayDataColumns.data)
    @SerializedName("data") val data: String = Constants.defPuzzleText,

    @ColumnInfo(name = Constants.OverlayDataColumns.difficultyLevel)
    @SerializedName("difficulty_level") val difficultyLevel: Short = 1,

    @ColumnInfo(name = Constants.OverlayDataColumns.isUsed)
    @SerializedName("is_used") val isUsed: Boolean = false
)
