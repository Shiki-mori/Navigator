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
