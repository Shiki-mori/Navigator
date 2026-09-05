package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.DailyRecord
import dev.phrolova.navigator.domain.model.Streak
import java.time.LocalDate

class StreakCalculator {
    fun calculate(
        records: List<DailyRecord>,
        today: LocalDate,
        trackingStartDate: LocalDate?,
    ): Streak {
        val byDate = records.associateBy { it.date }
        val earliestBound = earliestBound(records, trackingStartDate)
        val current = currentStreak(byDate, today, earliestBound)
        val longest = longestStreak(byDate, today, earliestBound)
        return Streak(
            current = current,
            longest = longest.length,
            longestStart = longest.start,
            longestEnd = longest.end,
        )
    }

    private fun earliestBound(
        records: List<DailyRecord>,
        trackingStartDate: LocalDate?,
    ): LocalDate? {
        val firstRecord = records.minOfOrNull { it.date }
        return when {
            trackingStartDate != null && firstRecord != null -> minOf(trackingStartDate, firstRecord)
            else -> trackingStartDate ?: firstRecord
        }
    }

    private fun currentStreak(
        byDate: Map<LocalDate, DailyRecord>,
        today: LocalDate,
        earliestBound: LocalDate?,
    ): Int {
        if (earliestBound == null) return 0
        val todayRecord = byDate[today]
        if (todayRecord?.isRelapse == true) return 0
        val end = if (todayRecord == null) today.minusDays(1) else today
        if (end.isBefore(earliestBound)) return 0
        var count = 0
        var cursor = end
        while (!cursor.isBefore(earliestBound)) {
            val record = byDate[cursor]
            if (record == null || record.isRelapse) break
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    private fun longestStreak(
        byDate: Map<LocalDate, DailyRecord>,
        today: LocalDate,
        earliestBound: LocalDate?,
    ): LongestRun {
        val start = earliestBound ?: return LongestRun.EMPTY
        var run = 0
        var best = 0
        var bestStart: LocalDate? = null
        var bestEnd: LocalDate? = null
        var cursor = start
        while (!cursor.isAfter(today)) {
            val record = byDate[cursor]
            if (record != null && record.isClean) {
                run++
                if (run >= best) {
                    best = run
                    bestEnd = cursor
                    bestStart = cursor.minusDays(run - 1L)
                }
            } else {
                run = 0
            }
            cursor = cursor.plusDays(1)
        }
        return LongestRun(length = best, start = bestStart, end = bestEnd)
    }

    private data class LongestRun(
        val length: Int,
        val start: LocalDate?,
        val end: LocalDate?,
    ) {
        companion object {
            val EMPTY = LongestRun(length = 0, start = null, end = null)
        }
    }
}
