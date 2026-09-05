package dev.phrolova.navigator.domain.model

import java.time.LocalDate

data class Streak(
    val current: Int,
    val longest: Int,
    val longestStart: LocalDate? = null,
    val longestEnd: LocalDate? = null,
)
