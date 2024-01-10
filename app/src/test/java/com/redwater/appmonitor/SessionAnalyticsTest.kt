package com.redwater.appmonitor

import com.redwater.appmonitor.data.model.Session
import com.redwater.appmonitor.data.model.SessionData
import com.redwater.appmonitor.data.model.hourlyDistributionInMillis
import org.junit.Assert
import org.junit.Test

class SessionAnalyticsTest {
    @Test
    fun sessionToHourlyDistTest(){
        val sessionList = listOf(
            //SessionData(sessionId=1704653842204, sessionLength=225979, lastSessionEndTS=1704654068253),
            SessionData(sessionId=(1704654097394+86400000), sessionLength=1646965, lastSessionEndTS=1704655744362+86400000),
            SessionData(sessionId=1704676827024+86400000, sessionLength=1415852, lastSessionEndTS=1704678243012+86400000),
            SessionData(sessionId=1704679022436+86400000, sessionLength=136050, lastSessionEndTS=1704679158649+86400000),
            SessionData(sessionId=1704679404196+86400000, sessionLength=711548, lastSessionEndTS=1704680115744+86400000),
        )
        var session: Session = Session(start = 1704653842204+86400000, end = 1704654068253+86400000)
        sessionList.map {
            session.addSession(it.sessionId, it.lastSessionEndTS)
        }
        println("session $session")
        val dist = session.hourlyDistributionInMillis()
        println("dist: $dist")
        val expectedDist = mapOf<Short, Long>("0".toShort() to 1873017, "6".toShort() to 572976, "7".toShort() to 1690773)
        Assert.assertSame(expectedDist, dist)
    }
}