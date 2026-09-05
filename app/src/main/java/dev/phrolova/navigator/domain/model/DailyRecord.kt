package dev.phrolova.navigator.domain.model

import java.time.LocalDate

data class DailyRecord(
    val date: LocalDate,
    val masturbationCount: Int = 0,
    val intercourseCount: Int = 0,
    val viewedPorn: Boolean = false,
    val nocturnalEmission: Boolean = false,
    val badHabit: BadHabit? = null,
) {
    val isRelapse: Boolean
        get() = masturbationCount >= 1 || viewedPorn

    val isClean: Boolean
        get() = !isRelapse

    val relapseCount: Int
        get() = when {
            masturbationCount >= 1 -> masturbationCount
            viewedPorn -> 1
            else -> 0
        }
}
