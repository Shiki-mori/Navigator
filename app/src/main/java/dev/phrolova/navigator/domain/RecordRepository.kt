package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.DailyRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RecordRepository {
    fun observeAll(): Flow<List<DailyRecord>>

    fun observeTrackingStartDate(): Flow<LocalDate?>

    suspend fun getAll(): List<DailyRecord>

    suspend fun get(date: LocalDate): DailyRecord?

    suspend fun upsert(record: DailyRecord)

    suspend fun replaceAll(records: List<DailyRecord>, trackingStartDate: LocalDate?)

    suspend fun getTrackingStartDate(): LocalDate?

    suspend fun setTrackingStartDate(date: LocalDate)
}
