package dev.phrolova.navigator.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.phrolova.navigator.AppContainer
import dev.phrolova.navigator.domain.model.DailyRecord
import dev.phrolova.navigator.domain.usecase.ObserveHomeState
import dev.phrolova.navigator.domain.usecase.SaveCheckIn
import dev.phrolova.navigator.ui.components.CheckInFields
import dev.phrolova.navigator.ui.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val today: LocalDate = LocalDate.MIN,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val longestStreakStart: LocalDate? = null,
    val longestStreakEnd: LocalDate? = null,
    val hasTodayRecord: Boolean = false,
    val isRelapseToday: Boolean = false,
    val fields: CheckInFields = CheckInFields(),
    val message: String? = null,
)

class HomeViewModel(
    private val observeHomeState: ObserveHomeState,
    private val saveCheckIn: SaveCheckIn,
) : ViewModel() {
    private val fields = MutableStateFlow(CheckInFields())
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        observeHomeState(),
        fields,
        message,
    ) { home, form, msg ->
        HomeUiState(
            today = home.today,
            currentStreak = home.streak.current,
            longestStreak = home.streak.longest,
            longestStreakStart = home.streak.longestStart,
            longestStreakEnd = home.streak.longestEnd,
            hasTodayRecord = home.todayRecord != null,
            isRelapseToday = home.todayRecord?.isRelapse == true,
            fields = form,
            message = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            observeHomeState()
                .map { it.todayRecord }
                .distinctUntilChanged()
                .collect { record -> fields.value = record.toFields() }
        }
    }

    fun updateFields(next: CheckInFields) {
        fields.value = next
    }

    fun save() {
        val current = uiState.value
        viewModelScope.launch {
            runCatching {
                saveCheckIn(
                    DailyRecord(
                        date = current.today,
                        masturbationCount = current.fields.masturbationCount,
                        intercourseCount = current.fields.intercourseCount,
                        viewedPorn = current.fields.viewedPorn,
                        nocturnalEmission = current.fields.nocturnalEmission,
                        badHabit = current.fields.badHabit,
                    ),
                )
            }.onSuccess {
                message.value = "已保存"
            }.onFailure {
                message.value = it.message ?: "保存失败"
            }
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            HomeViewModel(container.observeHomeState, container.saveCheckIn)
        }
    }
}

private fun DailyRecord?.toFields(): CheckInFields {
    if (this == null) return CheckInFields()
    return CheckInFields(
        masturbationCount = masturbationCount,
        intercourseCount = intercourseCount,
        viewedPorn = viewedPorn,
        nocturnalEmission = nocturnalEmission,
        badHabit = badHabit,
    )
}
