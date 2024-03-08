package com.redwater.appmonitor.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.redwater.appmonitor.Constants

@Entity(tableName = Constants.blogTable)
data class Blog(
    @ColumnInfo(name = Constants.BlogColumns.id)
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id") val id: Int=-1,

    @ColumnInfo(name = Constants.BlogColumns.header)
    @SerializedName("header") val header: String="",

    @ColumnInfo(name = Constants.BlogColumns.paragraph)
    @SerializedName("paragraph") val paragraph: String="",

    @ColumnInfo(name = Constants.BlogColumns.imageUrl)
    @SerializedName("imageUrl") val imageUrl: String="",

    @ColumnInfo(name = Constants.BlogColumns.author)
    @SerializedName("author") val author: String="",

    @ColumnInfo(name = Constants.BlogColumns.blogUrl)
    @SerializedName("blogUrl") val blogUrl: String="",

    @ColumnInfo(name = Constants.BlogColumns.isUsed)
    @SerializedName("isUsed") val isUsed: Boolean = false
)