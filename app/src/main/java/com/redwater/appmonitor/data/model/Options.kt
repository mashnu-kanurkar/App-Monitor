package com.redwater.appmonitor.data.model

import com.google.gson.annotations.SerializedName

data class TextOption(
    @SerializedName("text") val text: String,
    @SerializedName("color") val textColor: String? = null,
    @SerializedName("is_answer") val isAnswer: Boolean)

fun TextOption.toJsonString(): String{
    return "{\"text\":\"$text\",\"color\":\"$textColor\",\"is_answer\":$isAnswer}"
}

data class ImageOption(
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("is_answer") val isAnswer: Boolean)