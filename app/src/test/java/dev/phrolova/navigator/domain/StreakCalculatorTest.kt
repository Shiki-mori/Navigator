package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.DailyRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {
    private val calculator = StreakCalculator()
    private val start = LocalDate.of(2024, 8, 16)

    @Test
    fun todayUncheckedCountsThroughYesterday() {
        val today = LocalDate.of(2024, 8, 20)
        val records = listOf(
            clean(16),
            clean(17),
            clean(18),
            clean(19),
        )
        val streak = calculator.calculate(records, today, start)
        assertEquals(4, streak.current)
        assertEquals(4, streak.longest)
    }

    @Test
    fun todayCleanIncludesToday() {
        val today = LocalDate.of(2024, 8, 20)
        val records = listOf(clean(18), clean(19), clean(20))
        val streak = calculator.calculate(records, today, start)
        assertEquals(3, streak.current)
    }

    @Test
    fun todayRelapseCurrentIsZero() {
        val today = LocalDate.of(2024, 8, 20)
        val records = listOf(clean(18), clean(19), relapse(20))
        val streak = calculator.calculate(records, today, start)
        assertEquals(0, streak.current)
        assertEquals(2, streak.longest)
    }

    @Test
    fun missingDayBreaksCurrentAndLongest() {
        val today = LocalDate.of(2024, 8, 20)
        val records = listOf(clean(16), clean(18), clean(19), clean(20))
        val streak = calculator.calculate(records, today, start)
        assertEquals(3, streak.current)
        assertEquals(3, streak.longest)
    }

    @Test
    fun gapsBeforeTrackingStartDoNotInflateStreak() {
        val today = LocalDate.of(2024, 8, 18)
        val records = listOf(
            cleanOn(LocalDate.of(2022, 2, 25)),
            clean(16),
            clean(17),
            clean(18),
        )
        val streak = calculator.calculate(records, today, start)
        assertEquals(3, streak.current)
        assertEquals(3, streak.longest)
    }

    @Test
    fun intercourseAndEmissionDoNotBreakStreak() {
        val today = LocalDate.of(2024, 8, 18)
        val records = listOf(
            DailyRecord(date = date(16), intercourseCount = 1, nocturnalEmission = true),
            DailyRecord(date = date(17), badHabit = dev.phrolova.navigator.domain.model.BadHabit.ALL_NIGHTER),
            clean(18),
        )
        val streak = calculator.calculate(records, today, start)
        assertEquals(3, streak.current)
    }

    @Test
    fun pornAloneIsRelapse() {
        val today = LocalDate.of(2024, 8, 17)
        val records = listOf(
            clean(16),
            DailyRecord(date = date(17), viewedPorn = true),
        )
        val streak = calculator.calculate(records, today, start)
        assertEquals(0, streak.current)
        assertEquals(1, streak.longest)
    }

    private fun date(day: Int) = LocalDate.of(2024, 8, day)
    private fun clean(day: Int) = DailyRecord(date = date(day))
    private fun relapse(day: Int) = DailyRecord(date = date(day), masturbationCount = 1)
    private fun cleanOn(day: LocalDate) = DailyRecord(date = day)
}
