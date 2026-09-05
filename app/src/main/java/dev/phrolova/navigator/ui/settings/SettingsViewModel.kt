package dev.phrolova.navigator.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.phrolova.navigator.AppContainer
import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.usecase.ExportBackup
import dev.phrolova.navigator.domain.usecase.ImportBackup
import dev.phrolova.navigator.ui.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SettingsUiState(
    val recordCount: Int = 0,
    val trackingStartDate: LocalDate? = null,
    val message: String? = null,
)

class SettingsViewModel(
    private val repository: RecordRepository,
    private val importBackup: ImportBackup,
    private val exportBackup: ExportBackup,
    private val readUri: (Uri) -> String,
    private val writeUri: (Uri, String) -> Unit,
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observeAll(),
        repository.observeTrackingStartDate(),
        message,
    ) { records, start, msg ->
        SettingsUiState(recordCount = records.size, trackingStartDate = start, message = msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val text = readUri(uri)
                importBackup(text)
            }.onSuccess { count ->
                message.value = "已导入 $count 条"
            }.onFailure {
                message.value = "导入失败：${it.message}"
            }
        }
    }

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                writeUri(uri, exportBackup())
            }.onSuccess {
                message.value = "已导出"
            }.onFailure {
                message.value = "导出失败：${it.message}"
            }
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            SettingsViewModel(
                repository = container.repository,
                importBackup = container.importBackup,
                exportBackup = container.exportBackup,
                readUri = { uri ->
                    container.contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                        reader?.readText() ?: error("无法读取文件")
                    }
                },
                writeUri = { uri, text ->
                    container.contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
                        writer?.write(text) ?: error("无法写入文件")
                    }
                },
            )
        }
    }
}
