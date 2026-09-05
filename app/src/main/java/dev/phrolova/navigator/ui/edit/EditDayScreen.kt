package dev.phrolova.navigator.ui.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.phrolova.navigator.ui.components.CheckInForm

@Composable
fun EditDayScreen(
    viewModel: EditDayViewModel,
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
            .padding(16.dp),
    ) {
        Text(state.date.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            "补打或修改这一天",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        CheckInForm(
            fields = state.fields,
            onChange = viewModel::updateFields,
            onSave = viewModel::save,
            saveEnabled = state.loaded,
        )
    }
}
