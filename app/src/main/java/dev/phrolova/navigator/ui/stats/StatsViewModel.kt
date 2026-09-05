package dev.phrolova.navigator.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.phrolova.navigator.AppContainer
import dev.phrolova.navigator.domain.ChartCalculator
import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.StatsCalculator
import dev.phrolova.navigator.domain.model.ChartMetric
import dev.phrolova.navigator.domain.model.Stats
import dev.phrolova.navigator.domain.model.StatsPeriod
import dev.phrolova.navigator.domain.model.TimeSeriesChart
import dev.phrolova.navigator.ui.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

data class StatsQuery(
    val period: StatsPeriod = StatsPeriod.MONTH,
    val month: YearMonth,
    val year: Int,
    val metrics: Set<ChartMetric> = setOf(ChartMetric.RELAPSE),
)

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.MONTH,
    val month: YearMonth = YearMonth.now(),
    val year: Int = LocalDate.now().year,
    val today: LocalDate = LocalDate.MIN,
    val selectedMetrics: Set<ChartMetric> = setOf(ChartMetric.RELAPSE),
    val stats: Stats? = null,
    val chart: TimeSeriesChart? = null,
    val canGoNext: Boolean = false,
)

class StatsViewModel(
    repository: RecordRepository,
    statsCalculator: StatsCalculator,
    chartCalculator: ChartCalculator,
    clock: Clock,
) : ViewModel() {
    private val today = clock.today()
    private val query = MutableStateFlow(
        StatsQuery(month = YearMonth.from(today), year = today.year),
    )

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeAll(),
        repository.observeTrackingStartDate(),
        query,
    ) { records, start, q ->
        val stats = statsCalculator.calculate(
            records = records,
            today = today,
            period = q.period,
            trackingStartDate = start,
            month = q.month,
            year = q.year,
        )
        val chart = when (q.period) {
            StatsPeriod.MONTH -> chartCalculator.month(records, q.month, today, q.metrics)
            StatsPeriod.YEAR -> chartCalculator.year(records, q.year, today, q.metrics)
            StatsPeriod.ALL -> null
        }
        StatsUiState(
            period = q.period,
            month = q.month,
            year = q.year,
            today = today,
            selectedMetrics = q.metrics,
            stats = stats,
            chart = chart,
            canGoNext = canGoNext(q, today),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StatsUiState(month = YearMonth.from(today), year = today.year, today = today),
    )

    fun setPeriod(period: StatsPeriod) {
        query.update { current ->
            when (period) {
                StatsPeriod.MONTH -> {
                    val month = if (current.period == StatsPeriod.MONTH) {
                        current.month
                    } else if (current.year == today.year) {
                        YearMonth.from(today)
                    } else {
                        YearMonth.of(current.year, 1)
                    }
                    current.copy(period = period, month = month, year = month.year)
                }
                StatsPeriod.YEAR -> current.copy(period = period, year = current.month.year)
                StatsPeriod.ALL -> current.copy(period = period)
            }
        }
    }

    fun previous() {
        query.update { current ->
            when (current.period) {
                StatsPeriod.MONTH -> {
                    val month = current.month.minusMonths(1)
                    current.copy(month = month, year = month.year)
                }
                StatsPeriod.YEAR -> current.copy(year = current.year - 1)
                StatsPeriod.ALL -> current
            }
        }
    }

    fun next() {
        query.update { current ->
            if (!canGoNext(current, today)) return@update current
            when (current.period) {
                StatsPeriod.MONTH -> {
                    val month = current.month.plusMonths(1)
                    current.copy(month = month, year = month.year)
                }
                StatsPeriod.YEAR -> current.copy(year = current.year + 1)
                StatsPeriod.ALL -> current
            }
        }
    }

    fun toggleMetric(metric: ChartMetric) {
        query.update { current ->
            val next = if (metric in current.metrics) {
                current.metrics - metric
            } else {
                current.metrics + metric
            }
            current.copy(metrics = next)
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            StatsViewModel(
                repository = container.repository,
                statsCalculator = container.statsCalculator,
                chartCalculator = container.chartCalculator,
                clock = container.clock,
            )
        }

        private fun canGoNext(query: StatsQuery, today: LocalDate): Boolean {
            return when (query.period) {
                StatsPeriod.MONTH -> query.month < YearMonth.from(today)
                StatsPeriod.YEAR -> query.year < today.year
                StatsPeriod.ALL -> false
            }
        }
    }
}
