package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.BadHabit
import dev.phrolova.navigator.domain.model.DailyRecord
import dev.phrolova.navigator.domain.model.Stats
import dev.phrolova.navigator.domain.model.StatsPeriod
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class StatsCalculator {
    fun calculate(
        records: List<DailyRecord>,
        today: LocalDate,
        period: StatsPeriod,
        trackingStartDate: LocalDate?,
        month: YearMonth = YearMonth.from(today),
        year: Int = today.year,
    ): Stats {
        val (from, to) = bounds(today, period, records, trackingStartDate, month, year)
        val inPeriod = records.filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
        val relapseDates = inPeriod.filter { it.isRelapse }.map { it.date }.sorted()
        val averageInterval = if (relapseDates.size >= 2) {
            val gaps = relapseDates.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toDouble() }
            gaps.average()
        } else {
            null
        }
        return Stats(
            from = from,
            to = to,
            recordedDays = inPeriod.size,
            cleanDays = inPeriod.count { it.isClean },
            relapseDays = relapseDates.size,
            relapseCount = inPeriod.sumOf { it.relapseCount },
            masturbationTotal = inPeriod.sumOf { it.masturbationCount },
            intercourseTotal = inPeriod.sumOf { it.intercourseCount },
            pornDays = inPeriod.count { it.viewedPorn },
            nocturnalEmissionDays = inPeriod.count { it.nocturnalEmission },
            stayUpLateDays = inPeriod.count { it.badHabit == BadHabit.STAY_UP_LATE },
            allNighterDays = inPeriod.count { it.badHabit == BadHabit.ALL_NIGHTER },
            averageRelapseIntervalDays = averageInterval,
        )
    }

    private fun bounds(
        today: LocalDate,
        period: StatsPeriod,
        records: List<DailyRecord>,
        trackingStartDate: LocalDate?,
        month: YearMonth,
        year: Int,
    ): Pair<LocalDate, LocalDate> {
        val earliest = listOfNotNull(trackingStartDate, records.minOfOrNull { it.date }).minOrNull()
            ?: today
        return when (period) {
            StatsPeriod.MONTH -> month.atDay(1) to minOf(month.atEndOfMonth(), today)
            StatsPeriod.YEAR -> LocalDate.of(year, 1, 1) to minOf(LocalDate.of(year, 12, 31), today)
            StatsPeriod.ALL -> earliest to today
        }
    }
}
