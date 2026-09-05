package dev.phrolova.navigator.domain.usecase

import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.StatsCalculator
import dev.phrolova.navigator.domain.model.Stats
import dev.phrolova.navigator.domain.model.StatsPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveStats(
    private val repository: RecordRepository,
    private val statsCalculator: StatsCalculator,
    private val clock: Clock,
) {
    operator fun invoke(period: StatsPeriod): Flow<Stats> {
        val today = clock.today()
        return repository.observeAll().combine(repository.observeTrackingStartDate()) { records, start ->
            statsCalculator.calculate(records, today, period, start)
        }
    }
}
