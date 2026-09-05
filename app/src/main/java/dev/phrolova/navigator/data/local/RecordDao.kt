package dev.phrolova.navigator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM daily_records ORDER BY dateEpochDay ASC")
    fun observeAll(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records ORDER BY dateEpochDay ASC")
    suspend fun getAll(): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun get(epochDay: Long): DailyRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<DailyRecordEntity>)

    @Query("DELETE FROM daily_records")
    suspend fun deleteAllRecords()

    @Query("SELECT * FROM app_meta WHERE id = 1 LIMIT 1")
    fun observeMeta(): Flow<AppMetaEntity?>

    @Query("SELECT * FROM app_meta WHERE id = 1 LIMIT 1")
    suspend fun getMeta(): AppMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(meta: AppMetaEntity)

    @Transaction
    suspend fun replaceAll(records: List<DailyRecordEntity>, meta: AppMetaEntity) {
        deleteAllRecords()
        upsertAll(records)
        upsertMeta(meta)
    }
}
