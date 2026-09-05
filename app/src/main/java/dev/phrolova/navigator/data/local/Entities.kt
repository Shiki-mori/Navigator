package dev.phrolova.navigator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey val dateEpochDay: Long,
    val masturbationCount: Int,
    val intercourseCount: Int,
    val viewedPorn: Boolean,
    val nocturnalEmission: Boolean,
    val badHabit: String?,
)

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val id: Int = 1,
    val trackingStartEpochDay: Long?,
)
