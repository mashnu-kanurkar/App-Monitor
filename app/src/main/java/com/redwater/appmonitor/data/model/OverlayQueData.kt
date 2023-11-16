package com.redwater.appmonitor.data.model

import java.lang.StringBuilder

data class OverlayQueData(
    val que: String,
    val time: Int,
    val optionsList: List<TextOption>
)

fun OverlayQueData.toJsonString():String{
    val optionBuilder = StringBuilder()
    optionsList.forEachIndexed { index, option->
        optionBuilder.append(option.toJsonString())
        if (index < optionsList.size-1){
            optionBuilder.append(",")
        }
    }
    return "{\"que\":\"$que\",\"time\":\"$time\",\"options\":[$optionBuilder]}"
}


