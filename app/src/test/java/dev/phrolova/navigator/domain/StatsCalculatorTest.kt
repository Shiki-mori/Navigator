package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.BadHabit
import dev.phrolova.navigator.domain.model.DailyRecord
import dev.phrolova.navigator.domain.model.StatsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class StatsCalculatorTest {
    private val calculator = StatsCalculator()

    @Test
    fun allTimeCountsAndAverageInterval() {
        val today = LocalDate.of(2024, 8, 20)
        val records = listOf(
            DailyRecord(date = LocalDate.of(2024, 8, 10), masturbationCount = 2, viewedPorn = true),
            DailyRecord(date = LocalDate.of(2024, 8, 13), viewedPorn = true),
            DailyRecord(date = LocalDate.of(2024, 8, 19), masturbationCount = 1, badHabit = BadHabit.STAY_UP_LATE),
            DailyRecord(date = LocalDate.of(2024, 8, 20), intercourseCount = 1, nocturnalEmission = true),
        )
        val stats = calculator.calculate(
            records = records,
            today = today,
            period = StatsPeriod.ALL,
            trackingStartDate = LocalDate.of(2024, 8, 10),
        )
        assertEquals(4, stats.recordedDays)
        assertEquals(1, stats.cleanDays)
        assertEquals(3, stats.relapseDays)
        assertEquals(4, stats.relapseCount)
        assertEquals(3, stats.masturbationTotal)
        assertEquals(2, stats.pornDays)
        assertEquals(1, stats.intercourseTotal)
        assertEquals(1, stats.nocturnalEmissionDays)
        assertEquals(1, stats.stayUpLateDays)
        assertEquals(0, stats.allNighterDays)
        assertEquals(4.5, stats.averageRelapseIntervalDays!!, 0.001)
    }

    @Test
    fun monthPeriodExcludesPreviousMonth() {
        val today = LocalDate.of(2024, 8, 5)
        val records = listOf(
            DailyRecord(date = LocalDate.of(2024, 7, 30), masturbationCount = 1),
            DailyRecord(date = LocalDate.of(2024, 8, 1), viewedPorn = true),
            DailyRecord(date = LocalDate.of(2024, 8, 5)),
        )
        val stats = calculator.calculate(records, today, StatsPeriod.MONTH, null)
        assertEquals(LocalDate.of(2024, 8, 1), stats.from)
        assertEquals(2, stats.recordedDays)
        assertEquals(1, stats.relapseDays)
        assertEquals(1, stats.relapseCount)
        assertNull(stats.averageRelapseIntervalDays)
    }

    @Test
    fun selectedPastMonthDoesNotUseTodayMonth() {
        val today = LocalDate.of(2026, 9, 5)
        val records = listOf(
            DailyRecord(date = LocalDate.of(2024, 8, 1), masturbationCount = 1),
            DailyRecord(date = LocalDate.of(2026, 9, 5), viewedPorn = true),
        )
        val stats = calculator.calculate(
            records = records,
            today = today,
            period = StatsPeriod.MONTH,
            trackingStartDate = null,
            month = YearMonth.of(2024, 8),
        )
        assertEquals(LocalDate.of(2024, 8, 1), stats.from)
        assertEquals(LocalDate.of(2024, 8, 31), stats.to)
        assertEquals(1, stats.recordedDays)
        assertEquals(1, stats.relapseDays)
        assertEquals(1, stats.relapseCount)
    }
}
