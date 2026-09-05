package dev.phrolova.navigator.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.phrolova.navigator.ui.components.CheckInForm
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMessage: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.message) {
        val text = state.message ?: return@LaunchedEffect
        onMessage(text)
        viewModel.consumeMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("当前连续", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${state.currentStreak}",
            style = MaterialTheme.typography.displayLarge,
            color = if (state.isRelapseToday) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Text("天", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = longestStreakLabel(state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        val todayLabel = if (state.today.year > 0) {
            state.today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            ""
        }
        Text(
            text = when {
                state.isRelapseToday -> "$todayLabel  今日已破戒"
                state.hasTodayRecord -> "$todayLabel  今日已打卡"
                else -> "$todayLabel  今日尚未打卡"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(20.dp))
        CheckInForm(
            fields = state.fields,
            onChange = viewModel::updateFields,
            onSave = viewModel::save,
            saveEnabled = state.today.year > 0,
        )
    }
}

private fun longestStreakLabel(state: HomeUiState): String {
    val start = state.longestStreakStart
    val end = state.longestStreakEnd
    val summary = "最长 ${state.longestStreak} 天"
    if (state.longestStreak <= 0 || start == null || end == null) {
        return summary
    }
    return "$summary ${start}至${end}"
}
