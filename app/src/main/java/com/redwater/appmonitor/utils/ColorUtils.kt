package com.redwater.appmonitor.utils

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun hexStringToColorValue(hex: String): Color {
    val hexValue = hex.removePrefix("#")
    if (hexValue.length == 6 || hexValue.length == 8) {
        val value = hexValue.toLong(16)
        val red = (value shr 16 and 0xFF).toInt()
        val green = (value shr 8 and 0xFF).toInt()
        val blue = (value and 0xFF).toInt()
        val alpha = if (hexValue.length == 8) (value shr 24 and 0xFF).toInt() else 255
        return if (alpha == 255) Color(red = red, green = green, blue = blue) else Color(red, green, blue, alpha)
    } else {
        throw IllegalArgumentException("Invalid hexadecimal color string")
    }
}

fun getRandomColorList(size: Int): List<Int>{
    val random = Random
    val colorList = mutableListOf<Int>()
    for (i in 0..size){
        val color = android.graphics.Color.argb(255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256))
        colorList.add(color)
    }
    return colorList
}

fun getColorsForPieChart():List<Color>{
    return listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)
}