package com.redwater.appmonitor.data.model

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redwater.appmonitor.Constants

//To be used on room database only
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

    @ColumnInfo(name = Constants.AppPrefsColumns.delay)
    val delay: Short = 0,
)

//To be used for processing business logic only
data class AppModel(
    val packageName: String = "",
    val name: String = "Unknown",
    val isSelected: Boolean = false,
    var usageTime: Long = 0,
    val thresholdTime: Short = Short.MAX_VALUE,
    val icon: ImageBitmap? = null,
    var launchCountToday: Int = 0,
    val usageDistribution: MutableMap<Short, Long> = mutableMapOf(),//in seconds
    val delay: Short = 0
)

fun AppModel.toAppRoomModel(): AppRoomModel{
    return AppRoomModel(packageName = this.packageName,
        name = this.name,
        isSelected = this.isSelected,
        usageTime  = this.usageTime,
        thresholdTime = this.thresholdTime,
        delay = this.delay
    )
}

fun AppRoomModel.toAppModel(): AppModel{
    return AppModel(
        packageName = this.packageName,
        name = this.name,
        isSelected = this.isSelected,
        usageTime  = this.usageTime,
        thresholdTime = this.thresholdTime,
        icon = null,
        delay = this.delay
    )
}

data class AppEvent(
    val packageName: String,
    val event: Int
)

