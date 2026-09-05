package dev.phrolova.navigator.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.phrolova.navigator.AppContainer
import dev.phrolova.navigator.domain.model.DailyRecord
import dev.phrolova.navigator.domain.usecase.GetRecord
import dev.phrolova.navigator.domain.usecase.SaveCheckIn
import dev.phrolova.navigator.ui.components.CheckInFields
import dev.phrolova.navigator.ui.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EditDayUiState(
    val date: LocalDate,
    val fields: CheckInFields = CheckInFields(),
    val loaded: Boolean = false,
    val message: String? = null,
)

class EditDayViewModel(
    private val date: LocalDate,
    private val getRecord: GetRecord,
    private val saveCheckIn: SaveCheckIn,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditDayUiState(date = date))
    val uiState: StateFlow<EditDayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val record = getRecord(date)
            _uiState.update {
                it.copy(
                    loaded = true,
                    fields = CheckInFields(
                        masturbationCount = record?.masturbationCount ?: 0,
                        intercourseCount = record?.intercourseCount ?: 0,
                        viewedPorn = record?.viewedPorn ?: false,
                        nocturnalEmission = record?.nocturnalEmission ?: false,
                        badHabit = record?.badHabit,
                    ),
                )
            }
        }
    }

    fun updateFields(fields: CheckInFields) {
        _uiState.update { it.copy(fields = fields) }
    }

    fun save() {
        val current = _uiState.value
        viewModelScope.launch {
            runCatching {
                saveCheckIn(
                    DailyRecord(
                        date = current.date,
                        masturbationCount = current.fields.masturbationCount,
                        intercourseCount = current.fields.intercourseCount,
                        viewedPorn = current.fields.viewedPorn,
                        nocturnalEmission = current.fields.nocturnalEmission,
                        badHabit = current.fields.badHabit,
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(message = "已保存") }
            }.onFailure {
                _uiState.update { state -> state.copy(message = it.message ?: "保存失败") }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    companion object {
        fun factory(container: AppContainer, date: LocalDate) = viewModelFactory {
            EditDayViewModel(date, container.getRecord, container.saveCheckIn)
        }
    }
}
