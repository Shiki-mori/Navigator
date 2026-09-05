package dev.phrolova.navigator.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.phrolova.navigator.AppContainer
import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.model.DayStatus
import dev.phrolova.navigator.domain.usecase.ObserveMonth
import dev.phrolova.navigator.ui.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth,
    val today: LocalDate,
    val statuses: Map<LocalDate, DayStatus>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    observeMonth: ObserveMonth,
    clock: Clock,
) : ViewModel() {
    private val today = clock.today()
    private val month = MutableStateFlow(YearMonth.from(today))

    val uiState: StateFlow<CalendarUiState> = month
        .flatMapLatest { current ->
            observeMonth(current).map { statuses ->
                CalendarUiState(month = current, today = today, statuses = statuses)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CalendarUiState(month.value, today, emptyMap()),
        )

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            CalendarViewModel(container.observeMonth, container.clock)
        }
    }
}
