package dev.phrolova.navigator.data

import dev.phrolova.navigator.data.local.AppMetaEntity
import dev.phrolova.navigator.data.local.DailyRecordEntity
import dev.phrolova.navigator.data.local.RecordDao
import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.model.BadHabit
import dev.phrolova.navigator.domain.model.DailyRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomRecordRepository(
    private val dao: RecordDao,
) : RecordRepository {
    override fun observeAll(): Flow<List<DailyRecord>> {
        return dao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAll(): List<DailyRecord> {
        return dao.getAll().map { it.toDomain() }
    }

    override suspend fun get(date: LocalDate): DailyRecord? {
        return dao.get(date.toEpochDay())?.toDomain()
    }

    override suspend fun upsert(record: DailyRecord) {
        dao.upsert(record.toEntity())
    }

    override suspend fun replaceAll(records: List<DailyRecord>, trackingStartDate: LocalDate?) {
        dao.replaceAll(
            records = records.map { it.toEntity() },
            meta = AppMetaEntity(trackingStartEpochDay = trackingStartDate?.toEpochDay()),
        )
    }

    override suspend fun getTrackingStartDate(): LocalDate? {
        return dao.getMeta()?.trackingStartEpochDay?.let(LocalDate::ofEpochDay)
    }

    override suspend fun setTrackingStartDate(date: LocalDate) {
        dao.upsertMeta(AppMetaEntity(trackingStartEpochDay = date.toEpochDay()))
    }

    override fun observeTrackingStartDate(): Flow<LocalDate?> {
        return dao.observeMeta().map { meta -> meta?.trackingStartEpochDay?.let(LocalDate::ofEpochDay) }
    }
}

private fun DailyRecordEntity.toDomain(): DailyRecord {
    return DailyRecord(
        date = LocalDate.ofEpochDay(dateEpochDay),
        masturbationCount = masturbationCount,
        intercourseCount = intercourseCount,
        viewedPorn = viewedPorn,
        nocturnalEmission = nocturnalEmission,
        badHabit = BadHabit.fromStored(badHabit),
    )
}

private fun DailyRecord.toEntity(): DailyRecordEntity {
    return DailyRecordEntity(
        dateEpochDay = date.toEpochDay(),
        masturbationCount = masturbationCount,
        intercourseCount = intercourseCount,
        viewedPorn = viewedPorn,
        nocturnalEmission = nocturnalEmission,
        badHabit = badHabit?.name,
    )
}
