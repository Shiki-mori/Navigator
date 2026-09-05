package dev.phrolova.navigator.domain

import java.time.LocalDate

fun interface Clock {
    fun today(): LocalDate
}

class SystemClock : Clock {
    override fun today(): LocalDate = LocalDate.now()
}
