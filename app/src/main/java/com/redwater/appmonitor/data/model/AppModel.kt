package com.redwater.appmonitor.data.model

import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.redwater.appmonitor.Constants

@Entity(tableName = Constants.appPrefsTable)
data class AppRoomModel(
    @PrimaryKey
    @ColumnInfo(name = Constants.AppPrefsColumns.packageName)
    val packageName: String = "",

    @ColumnInfo(name = Constants.AppPrefsColumns.name)
    val name: String = "Unknown",

    @ColumnInfo(name = Constants.AppPrefsColumns.isSelected)
    val isSelected: Boolean = false,

    @ColumnInfo(name = Constants.AppPrefsColumns.usageTime)
    var usageTime: Long = 0,

    @ColumnInfo(name = Constants.AppPrefsColumns.thresholdTime)
    val thresholdTime: Short = Short.MAX_VALUE,

)

data class AppModel(
    val packageName: String = "",
    val name: String = "Unknown",
    val isSelected: Boolean = false,
    var usageTime: Long = 0,
    val thresholdTime: Short = Short.MAX_VALUE,
    val icon: Drawable? = null
)

fun AppModel.toAppRoomModel(): AppRoomModel{
    return AppRoomModel(packageName = this.packageName,
        name = this.name,
        isSelected = this.isSelected,
        usageTime  = this.usageTime,
        thresholdTime = this.thresholdTime
    )
}

fun AppRoomModel.toAppModel(): AppModel{
    return AppModel(
        packageName = this.packageName,
        name = this.name,
        isSelected = this.isSelected,
        usageTime  = this.usageTime,
        thresholdTime = this.thresholdTime,
        icon = null
    )
}
