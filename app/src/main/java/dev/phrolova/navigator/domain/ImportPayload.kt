package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.DailyRecord
import java.time.LocalDate

data class ImportPayload(
    val trackingStartDate: LocalDate?,
    val records: List<DailyRecord>,
)
