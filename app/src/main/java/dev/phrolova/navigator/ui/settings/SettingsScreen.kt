package dev.phrolova.navigator.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onMessage: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(viewModel::importFrom)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let(viewModel::exportTo)
    }

    LaunchedEffect(state.message) {
        val text = state.message ?: return@LaunchedEffect
        onMessage(text)
        viewModel.consumeMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("数据", style = MaterialTheme.typography.titleLarge)
        Text("已记录 ${state.recordCount} 天")
        Text("开始追踪：${state.trackingStartDate.toDisplay()}")
        Button(
            onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("导入备份")
        }
        OutlinedButton(
            onClick = {
                val name = "navigator-backup-${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.json"
                exportLauncher.launch(name)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("导出备份")
        }
        Text("规则", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        Text(
            "当日自慰次数 ≥ 1 或看黄，视为破戒，连续日归零。房事、遗精、熬夜、通宵不断连续日。开始追踪日之前的缺日记为未记录。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "导入会全量替换现有记录。请使用清洗后的 JSON，或仍可导入带 yy/yw 的旧备份（这两项会被忽略）。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun LocalDate?.toDisplay(): String = this?.toString() ?: "尚未设置"
