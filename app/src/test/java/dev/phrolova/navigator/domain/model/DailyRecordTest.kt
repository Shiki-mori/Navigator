package dev.phrolova.navigator.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DailyRecordTest {
    private val day = LocalDate.of(2024, 8, 1)

    @Test
    fun masturbationOnlyUsesMasturbationCount() {
        val record = DailyRecord(date = day, masturbationCount = 3)
        assertEquals(3, record.relapseCount)
    }

    @Test
    fun masturbationWithPornUsesMasturbationCountOnly() {
        val record = DailyRecord(date = day, masturbationCount = 2, viewedPorn = true)
        assertEquals(2, record.relapseCount)
    }

    @Test
    fun pornOnlyCountsAsOne() {
        val record = DailyRecord(date = day, viewedPorn = true)
        assertEquals(1, record.relapseCount)
    }

    @Test
    fun cleanDayIsZero() {
        val record = DailyRecord(date = day, intercourseCount = 1, nocturnalEmission = true)
        assertEquals(0, record.relapseCount)
    }
}
