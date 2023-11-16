package com.redwater.appmonitor.games

import kotlin.random.Random

class NimCountingGame(
    private val maxStepNum: Int = 2,
    private val targetNum: Int = 20

) {
    fun getSystemNextGuess(lastSum: Int ): Int{
        val div = 1 + maxStepNum
        return if (lastSum % div == targetNum % div){
            Random.nextInt(1, div)
        }else{
            val guess = div - (lastSum % div) + (targetNum % div)
            guess - div
        }
    }
}