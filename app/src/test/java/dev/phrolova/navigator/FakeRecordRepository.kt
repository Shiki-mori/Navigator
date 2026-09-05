package dev.phrolova.navigator

import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.model.DailyRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class FakeRecordRepository : RecordRepository {
    private val records = MutableStateFlow<List<DailyRecord>>(emptyList())
    private val start = MutableStateFlow<LocalDate?>(null)

    override fun observeAll(): Flow<List<DailyRecord>> = records
    override fun observeTrackingStartDate(): Flow<LocalDate?> = start
    override suspend fun getAll(): List<DailyRecord> = records.value
    override suspend fun get(date: LocalDate): DailyRecord? = records.value.find { it.date == date }
    override suspend fun upsert(record: DailyRecord) {
        records.value = records.value.filterNot { it.date == record.date } + record
    }
    override suspend fun replaceAll(records: List<DailyRecord>, trackingStartDate: LocalDate?) {
        this.records.value = records
        start.value = trackingStartDate
    }
    override suspend fun getTrackingStartDate(): LocalDate? = start.value
    override suspend fun setTrackingStartDate(date: LocalDate) {
        start.value = date
    }
}
