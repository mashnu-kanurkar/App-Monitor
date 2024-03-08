package com.redwater.appmonitor

import com.redwater.appmonitor.utils.TimeFormatUtility
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(value = Parameterized::class)
class TimeFormatUtilityTest(val timeToCheckStr: String, val expected: Boolean) {

    @Test
    fun isWithinDNDTimeTest_Boolean(){
        val startTimeStr = "09:00 PM"
        val endTimeStr = "06:00 AM"
        val timeFormatUtility = TimeFormatUtility()
        val isWithinDND = timeFormatUtility.isWithinDNDTime(timeToCheckStr, startTimeStr, endTimeStr)
        Assert.assertEquals(expected, isWithinDND)
    }

    companion object{
        @JvmStatic
        @Parameters(name = "{index}: {0} is within DND {1}")
        fun data(): List<Array<Any>>{
            return listOf(
                arrayOf("10:00 PM", true),
                arrayOf("11:00 PM", true),
                arrayOf("12:00 AM", true),
                arrayOf("01:00 AM", true),
                arrayOf("02:00 AM", true),
                arrayOf("03:00 AM", true),
                arrayOf("04:00 AM", true),
                arrayOf("05:00 AM", true),
                arrayOf("06:00 AM", true),
                arrayOf("07:00 AM", false),
                arrayOf("08:00 AM", false),
                arrayOf("09:00 AM", false),
                arrayOf("10:00 AM", false),
                arrayOf("11:00 AM", false),
                arrayOf("12:00 PM", false),
                arrayOf("01:00 PM", false),
                arrayOf("02:00 PM", false),
                arrayOf("03:00 PM", false),
                arrayOf("04:00 PM", false),
                arrayOf("05:00 PM", false),
                arrayOf("06:00 PM", false),
                arrayOf("07:00 PM", false),
                arrayOf("08:00 PM", false),
                arrayOf("09:00 PM", true),
            )
        }
    }
}


@RunWith(value = Parameterized::class)
class TimeFormatUtilityTest2(val timeToCheckStr: String, val expected: Boolean) {

    @Test
    fun isWithinDNDTimeTest_Boolean(){
        val startTimeStr = "09:00 AM"
        val endTimeStr = "06:00 PM"
        val timeFormatUtility = TimeFormatUtility()
        val isWithinDND = timeFormatUtility.isWithinDNDTime(timeToCheckStr, startTimeStr, endTimeStr)
        Assert.assertEquals(expected, isWithinDND)
    }

    @Test
    fun isWithinDND_expected_false(){
        val startTimeStr = "09:00 AM"
        val endTimeStr = "06:00 PM"
        val timeFormatUtility = TimeFormatUtility()
        val isWithinDND = timeFormatUtility.isWithinDNDTime("06:00 AM", startTimeStr, endTimeStr)
        Assert.assertEquals(false, isWithinDND)
    }

    companion object{
        @JvmStatic
        @Parameters(name = "{index}: {0} is within DND {1}")
        fun data(): List<Array<Any>>{
            return listOf(
                arrayOf("10:00 PM", false),
                arrayOf("11:00 PM", false),
                arrayOf("12:00 AM", false),
                arrayOf("01:00 AM", false),
                arrayOf("02:00 AM", false),
                arrayOf("03:00 AM", false),
                arrayOf("04:00 AM", false),
                arrayOf("05:00 AM", false),
                arrayOf("06:00 AM", true),
                arrayOf("07:00 AM", false),
                arrayOf("08:00 AM", false),
                arrayOf("09:00 AM", true),
                arrayOf("10:00 AM", true),
                arrayOf("11:00 AM", true),
                arrayOf("12:00 PM", true),
                arrayOf("01:00 PM", true),
                arrayOf("02:00 PM", true),
                arrayOf("03:00 PM", true),
                arrayOf("04:00 PM", true),
                arrayOf("05:00 PM", true),
                arrayOf("06:00 PM", true),
                arrayOf("07:00 PM", false),
                arrayOf("08:00 PM", false),
                arrayOf("09:00 PM", true),
            )
        }
    }
}