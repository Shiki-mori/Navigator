package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.BadHabit
import dev.phrolova.navigator.domain.model.ChartMetric
import dev.phrolova.navigator.domain.model.DailyRecord
import dev.phrolova.navigator.domain.model.MetricSeries
import dev.phrolova.navigator.domain.model.TimeSeriesChart
import java.time.LocalDate
import java.time.YearMonth

class ChartCalculator {
    fun month(
        records: List<DailyRecord>,
        month: YearMonth,
        today: LocalDate,
        metrics: Set<ChartMetric>,
    ): TimeSeriesChart {
        val lastDay = lastDayInMonth(month, today)
        val byDate = records.associateBy { it.date }
        val days = (1..lastDay).toList()
        return TimeSeriesChart(
            xLabels = days.map { it.toString() },
            series = seriesFor(metrics) { metric ->
                days.map { day ->
                    valueOf(byDate[month.atDay(day)], metric)
                }
            },
        )
    }

    fun year(
        records: List<DailyRecord>,
        year: Int,
        today: LocalDate,
        metrics: Set<ChartMetric>,
    ): TimeSeriesChart {
        val lastMonth = lastMonthInYear(year, today)
        val months = (1..lastMonth).toList()
        return TimeSeriesChart(
            xLabels = months.map { "${it}月" },
            series = seriesFor(metrics) { metric ->
                months.map { monthNumber ->
                    val yearMonth = YearMonth.of(year, monthNumber)
                    val from = yearMonth.atDay(1)
                    val to = minOf(yearMonth.atEndOfMonth(), today)
                    records
                        .filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
                        .sumOf { valueOf(it, metric).toDouble() }
                        .toFloat()
                }
            },
        )
    }

    private fun seriesFor(
        metrics: Set<ChartMetric>,
        values: (ChartMetric) -> List<Float>,
    ): List<MetricSeries> {
        return ChartMetric.entries
            .filter { it in metrics }
            .map { MetricSeries(metric = it, values = values(it)) }
    }

    private fun lastDayInMonth(month: YearMonth, today: LocalDate): Int {
        return if (YearMonth.from(today) == month) {
            today.dayOfMonth
        } else {
            month.lengthOfMonth()
        }
    }

    private fun lastMonthInYear(year: Int, today: LocalDate): Int {
        return if (today.year == year) today.monthValue else 12
    }

    companion object {
        fun valueOf(record: DailyRecord?, metric: ChartMetric): Float {
            if (record == null) return 0f
            return when (metric) {
                ChartMetric.RELAPSE -> if (record.isRelapse) 1f else 0f
                ChartMetric.MASTURBATION -> record.masturbationCount.toFloat()
                ChartMetric.PORN -> if (record.viewedPorn) 1f else 0f
                ChartMetric.INTERCOURSE -> record.intercourseCount.toFloat()
                ChartMetric.EMISSION -> if (record.nocturnalEmission) 1f else 0f
                ChartMetric.STAY_UP_LATE -> if (record.badHabit == BadHabit.STAY_UP_LATE) 1f else 0f
                ChartMetric.ALL_NIGHTER -> if (record.badHabit == BadHabit.ALL_NIGHTER) 1f else 0f
            }
        }
    }
}
