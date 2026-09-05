package dev.phrolova.navigator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.phrolova.navigator.domain.model.BadHabit

data class CheckInFields(
    val masturbationCount: Int = 0,
    val intercourseCount: Int = 0,
    val viewedPorn: Boolean = false,
    val nocturnalEmission: Boolean = false,
    val badHabit: BadHabit? = null,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInForm(
    fields: CheckInFields,
    onChange: (CheckInFields) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CountStepper(
            label = "自慰",
            value = fields.masturbationCount,
            onChange = { onChange(fields.copy(masturbationCount = it)) },
        )
        Spacer(Modifier.height(8.dp))
        CountStepper(
            label = "房事",
            value = fields.intercourseCount,
            onChange = { onChange(fields.copy(intercourseCount = it)) },
        )
        Spacer(Modifier.height(12.dp))
        ToggleRow(
            label = "看黄",
            checked = fields.viewedPorn,
            onChecked = { onChange(fields.copy(viewedPorn = it)) },
        )
        ToggleRow(
            label = "遗精",
            checked = fields.nocturnalEmission,
            onChecked = { onChange(fields.copy(nocturnalEmission = it)) },
        )
        Spacer(Modifier.height(8.dp))
        Text("坏习惯", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = fields.badHabit == null,
                onClick = { onChange(fields.copy(badHabit = null)) },
                label = { Text("无") },
            )
            FilterChip(
                selected = fields.badHabit == BadHabit.STAY_UP_LATE,
                onClick = { onChange(fields.copy(badHabit = BadHabit.STAY_UP_LATE)) },
                label = { Text("熬夜") },
            )
            FilterChip(
                selected = fields.badHabit == BadHabit.ALL_NIGHTER,
                onClick = { onChange(fields.copy(badHabit = BadHabit.ALL_NIGHTER)) },
                label = { Text("通宵") },
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSave,
            enabled = saveEnabled,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("保存")
        }
    }
}

@Composable
private fun CountStepper(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    max: Int = 20,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        OutlinedIconButton(onClick = { onChange((value - 1).coerceAtLeast(0)) }) {
            Icon(Icons.Outlined.Remove, contentDescription = "减少$label")
        }
        Text(
            text = value.toString(),
            modifier = Modifier.width(36.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedIconButton(onClick = { onChange((value + 1).coerceAtMost(max)) }) {
            Icon(Icons.Outlined.Add, contentDescription = "增加$label")
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
