package dev.phrolova.navigator.domain.usecase

import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.model.DailyRecord
import java.time.LocalDate

class SaveCheckIn(
    private val repository: RecordRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(record: DailyRecord) {
        require(!record.date.isAfter(clock.today())) { "不能为未来日期打卡" }
        require(record.masturbationCount >= 0 && record.intercourseCount >= 0) {
            "次数不能为负"
        }
        repository.upsert(record)
        if (repository.getTrackingStartDate() == null) {
            repository.setTrackingStartDate(record.date)
        }
    }
}

class GetRecord(
    private val repository: RecordRepository,
) {
    suspend operator fun invoke(date: LocalDate): DailyRecord? = repository.get(date)
}

class AutoCheckIn(
    private val repository: RecordRepository,
    private val saveCheckIn: SaveCheckIn,
    private val clock: Clock,
) {
    suspend operator fun invoke(): List<LocalDate> {
        val today = clock.today()
        if (repository.get(today) != null) return emptyList()
        val trackingStart = repository.getTrackingStartDate()
        val filled = mutableListOf<LocalDate>()
        var date = today.minusDays(LOOKBACK_DAYS)
        while (!date.isAfter(today)) {
            val beforeTracking = trackingStart != null && date.isBefore(trackingStart)
            if (!beforeTracking && repository.get(date) == null) {
                saveCheckIn(DailyRecord(date = date))
                filled += date
            }
            date = date.plusDays(1)
        }
        return filled
    }

    companion object {
        const val LOOKBACK_DAYS: Long = 5
    }
}
