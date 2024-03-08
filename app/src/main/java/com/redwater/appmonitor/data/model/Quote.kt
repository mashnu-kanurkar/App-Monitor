package com.redwater.appmonitor.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.redwater.appmonitor.Constants

@Entity(tableName = Constants.quoteTable)
data class Quote(
    @ColumnInfo(name = Constants.QuotesColumns.id)
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id") val id: Int=-1,

    @ColumnInfo(name = Constants.QuotesColumns.text)
    @SerializedName("text") val text: String="",

    @ColumnInfo(name = Constants.QuotesColumns.author)
    @SerializedName("author") val author: String="",

    @ColumnInfo(name = Constants.QuotesColumns.isUsed)
    @SerializedName("isUsed") val isUsed: Boolean = false
    )
