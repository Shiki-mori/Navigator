package dev.phrolova.navigator.domain.usecase

import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.StreakCalculator
import dev.phrolova.navigator.domain.model.DailyRecord
import dev.phrolova.navigator.domain.model.DayStatus
import dev.phrolova.navigator.domain.model.Streak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

data class HomeSnapshot(
    val today: LocalDate,
    val todayRecord: DailyRecord?,
    val streak: Streak,
)

class ObserveHomeState(
    private val repository: RecordRepository,
    private val streakCalculator: StreakCalculator,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<HomeSnapshot> {
        val today = clock.today()
        return combine(
            repository.observeAll(),
            repository.observeTrackingStartDate(),
        ) { records, start ->
            HomeSnapshot(
                today = today,
                todayRecord = records.find { it.date == today },
                streak = streakCalculator.calculate(records, today, start),
            )
        }
    }
}

class ObserveMonth(
    private val repository: RecordRepository,
) {
    operator fun invoke(month: YearMonth): Flow<Map<LocalDate, DayStatus>> {
        return all().map { statuses ->
            val days = month.lengthOfMonth()
            buildMap {
                for (day in 1..days) {
                    val date = month.atDay(day)
                    put(date, statuses[date] ?: DayStatus.UNRECORDED)
                }
            }
        }
    }

    fun all(): Flow<Map<LocalDate, DayStatus>> {
        return repository.observeAll().map { records ->
            records.associate { record ->
                record.date to if (record.isRelapse) DayStatus.RELAPSE else DayStatus.CLEAN
            }
        }
    }
}
