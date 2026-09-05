package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.BadHabit
import dev.phrolova.navigator.domain.model.ChartMetric
import dev.phrolova.navigator.domain.model.DailyRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ChartCalculatorTest {
    private val calculator = ChartCalculator()

    @Test
    fun monthUsesOnePointPerDayThroughToday() {
        val month = YearMonth.of(2024, 8)
        val records = listOf(
            DailyRecord(date = LocalDate.of(2024, 8, 1), masturbationCount = 2),
            DailyRecord(date = LocalDate.of(2024, 8, 2), masturbationCount = 3, viewedPorn = true),
            DailyRecord(date = LocalDate.of(2024, 8, 3), viewedPorn = true),
        )
        val chart = calculator.month(
            records = records,
            month = month,
            today = LocalDate.of(2024, 8, 5),
            metrics = setOf(ChartMetric.RELAPSE, ChartMetric.MASTURBATION, ChartMetric.PORN),
        )
        assertEquals(listOf("1", "2", "3", "4", "5"), chart.xLabels)
        assertEquals(
            listOf(2f, 3f, 1f, 0f, 0f),
            chart.series.first { it.metric == ChartMetric.RELAPSE }.values,
        )
        assertEquals(
            listOf(2f, 3f, 0f, 0f, 0f),
            chart.series.first { it.metric == ChartMetric.MASTURBATION }.values,
        )
        assertEquals(
            listOf(0f, 1f, 1f, 0f, 0f),
            chart.series.first { it.metric == ChartMetric.PORN }.values,
        )
    }

    @Test
    fun pastMonthIncludesFullMonth() {
        val chart = calculator.month(
            records = emptyList(),
            month = YearMonth.of(2024, 2),
            today = LocalDate.of(2024, 8, 5),
            metrics = setOf(ChartMetric.RELAPSE),
        )
        assertEquals(29, chart.xLabels.size)
    }

    @Test
    fun yearAggregatesSelectedMetricsByMonth() {
        val records = listOf(
            DailyRecord(date = LocalDate.of(2024, 1, 2), masturbationCount = 1),
            DailyRecord(date = LocalDate.of(2024, 1, 5), masturbationCount = 2, badHabit = BadHabit.STAY_UP_LATE),
            DailyRecord(date = LocalDate.of(2024, 3, 1), viewedPorn = true),
        )
        val chart = calculator.year(
            records = records,
            year = 2024,
            today = LocalDate.of(2024, 3, 15),
            metrics = setOf(ChartMetric.RELAPSE, ChartMetric.MASTURBATION, ChartMetric.STAY_UP_LATE),
        )
        assertEquals(listOf("1月", "2月", "3月"), chart.xLabels)
        assertEquals(
            listOf(3f, 0f, 1f),
            chart.series.first { it.metric == ChartMetric.RELAPSE }.values,
        )
        assertEquals(
            listOf(3f, 0f, 0f),
            chart.series.first { it.metric == ChartMetric.MASTURBATION }.values,
        )
        assertEquals(
            listOf(1f, 0f, 0f),
            chart.series.first { it.metric == ChartMetric.STAY_UP_LATE }.values,
        )
    }

    @Test
    fun unrecordedDaysAreZeroAndDoNotCountAsRelapse() {
        val chart = calculator.month(
            records = listOf(DailyRecord(date = LocalDate.of(2024, 8, 16))),
            month = YearMonth.of(2024, 8),
            today = LocalDate.of(2024, 8, 18),
            metrics = setOf(ChartMetric.RELAPSE),
        )
        assertEquals(18, chart.xLabels.size)
        assertEquals(List(18) { 0f }, chart.series.single().values)
    }
}
