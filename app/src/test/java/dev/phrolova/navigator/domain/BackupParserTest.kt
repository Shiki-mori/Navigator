package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.BadHabit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BackupParserTest {
    private val parser = BackupParser()

    @Test
    fun parseLegacyIgnoresYyYwAndMapsFields() {
        val json = javaClass.classLoader!!
            .getResource("legacy_sample.json")!!
            .readText()
        val payload = parser.parse(json)
        assertEquals(LocalDate.of(2024, 8, 16), payload.trackingStartDate)
        assertEquals(6, payload.records.size)

        val first = payload.records.first { it.date == LocalDate.of(2024, 8, 14) }
        assertEquals(0, first.masturbationCount)
        assertTrue(first.nocturnalEmission)
        assertFalse(first.viewedPorn)
        assertNull(first.badHabit)

        val relapse = payload.records.first { it.date == LocalDate.of(2024, 8, 18) }
        assertEquals(2, relapse.masturbationCount)
        assertTrue(relapse.viewedPorn)
        assertTrue(relapse.isRelapse)

        val stayUp = payload.records.first { it.date == LocalDate.of(2024, 9, 3) }
        assertEquals(BadHabit.STAY_UP_LATE, stayUp.badHabit)
        assertTrue(stayUp.isClean)

        val allNighter = payload.records.first { it.date == LocalDate.of(2024, 9, 4) }
        assertEquals(BadHabit.ALL_NIGHTER, allNighter.badHabit)
    }

    @Test
    fun parseLegacyWithoutYyYw() {
        val json = """
            {"version":2,"first_start":20240816,"records":[
              {"calendar":20240816,"zw":0,"fs":1,"kh":0,"yj":0}
            ]}
        """.trimIndent()
        val payload = parser.parse(json)
        assertEquals(1, payload.records.size)
        assertEquals(1, payload.records[0].intercourseCount)
        assertFalse(payload.records[0].isRelapse)
    }

    @Test
    fun khNonZeroMeansViewedPorn() {
        val json = """{"records":[{"calendar":20240101,"zw":0,"fs":0,"kh":1,"yj":0}]}"""
        val payload = parser.parse(json)
        assertTrue(payload.records[0].viewedPorn)
        assertTrue(payload.records[0].isRelapse)
    }

    @Test
    fun nativeRoundTrip() {
        val original = parser.parse(
            """
            {"format":"navigator.backup","version":1,"exportedAt":"2026-09-05T16:00:00",
             "trackingStartDate":"2024-08-16",
             "records":[{"date":"2024-09-03","masturbationCount":0,"intercourseCount":0,
             "viewedPorn":false,"nocturnalEmission":false,"badHabit":"STAY_UP_LATE"}]}
            """.trimIndent(),
        )
        val exported = parser.exportNative(original, "2026-09-05T17:00:00")
        val again = parser.parse(exported)
        assertEquals(original.trackingStartDate, again.trackingStartDate)
        assertEquals(original.records, again.records)
        assertTrue(exported.contains("navigator.backup"))
        assertFalse(exported.contains("\"zw\""))
    }

    @Test
    fun duplicateDatesKeepLast() {
        val json = """
            {"records":[
              {"calendar":20240101,"zw":1,"fs":0,"kh":0,"yj":0},
              {"calendar":20240101,"zw":0,"fs":0,"kh":0,"yj":0}
            ]}
        """.trimIndent()
        val payload = parser.parse(json)
        assertEquals(1, payload.records.size)
        assertEquals(0, payload.records[0].masturbationCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyBackupRejected() {
        parser.parse("   ")
    }
}
