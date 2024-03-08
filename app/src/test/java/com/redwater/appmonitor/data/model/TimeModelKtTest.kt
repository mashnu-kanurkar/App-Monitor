package com.redwater.appmonitor.data.model

import org.junit.After
import org.junit.Before
import org.junit.Test

class TimeModelKtTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun toFormattedString() {
    }

    @Test
    fun toTimeModel() {
    }

    @Test
    fun createDNDKey() {
    }

    @Test
    fun getTimeModelFromKey() {
    }

    @Test
    fun getDNDTimeFrom() {
    }

    @Test
    fun toDNDMap() {
        val listOfApps = listOf(AppModel(packageName="com.instagram.android", name="Instagram", isSelected=false, usageTimeInMillis=4264157, thresholdTime=null, icon=null, launchCountToday=0, delay=0, session=null, dndStartTime="10:30 PM", dndEndTime="6:30 AM"),
            AppModel(packageName="com.samsung.android.app.contacts", name="Contacts", isSelected=false, usageTimeInMillis=0, thresholdTime=null, icon=null, launchCountToday=0, delay=0, session=null, dndStartTime="10:30 PM", dndEndTime="6:30 AM"),
            AppModel(packageName="com.axis.mobile", name="Axis Mobile", isSelected=false, usageTimeInMillis=0, thresholdTime=null, icon=null, launchCountToday=0, delay=0, session=null, dndStartTime="10:30 PM", dndEndTime="6:30 AM"))
        val toDND = mapOf("10:30 PM_6:30 AM" to mapOf(listOfApps[0].packageName to listOfApps[0],
            listOfApps[1].packageName to listOfApps[1],
            listOfApps[2].packageName to listOfApps[2]))


    }
}