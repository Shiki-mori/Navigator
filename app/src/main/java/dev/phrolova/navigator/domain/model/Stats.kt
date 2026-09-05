package dev.phrolova.navigator.domain.model

import java.time.LocalDate

data class Stats(
    val from: LocalDate,
    val to: LocalDate,
    val recordedDays: Int,
    val cleanDays: Int,
    val relapseDays: Int,
    val masturbationTotal: Int,
    val intercourseTotal: Int,
    val pornDays: Int,
    val nocturnalEmissionDays: Int,
    val stayUpLateDays: Int,
    val allNighterDays: Int,
    val averageRelapseIntervalDays: Double?,
)

enum class StatsPeriod {
    MONTH,
    YEAR,
    ALL,
}
