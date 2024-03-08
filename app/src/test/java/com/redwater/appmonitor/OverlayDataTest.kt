package com.redwater.appmonitor

import com.redwater.appmonitor.overlayview.DefaultMathPuzzle
import org.junit.Assert
import org.junit.Test

class OverlayDataTest {
    @Test
    fun generateRandomQuestionTest(){
        val def = DefaultMathPuzzle()
        val s = def.generateQuestion()
        println(s)
        Assert.assertEquals(4, 2 + 2)
    }
}