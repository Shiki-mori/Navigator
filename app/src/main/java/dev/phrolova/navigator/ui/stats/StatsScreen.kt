package dev.phrolova.navigator.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.phrolova.navigator.domain.model.ChartMetric
import dev.phrolova.navigator.domain.model.StatsPeriod
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dark = isSystemInDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.period == StatsPeriod.MONTH,
                onClick = { viewModel.setPeriod(StatsPeriod.MONTH) },
                label = { Text("月视图") },
            )
            FilterChip(
                selected = state.period == StatsPeriod.YEAR,
                onClick = { viewModel.setPeriod(StatsPeriod.YEAR) },
                label = { Text("年视图") },
            )
            FilterChip(
                selected = state.period == StatsPeriod.ALL,
                onClick = { viewModel.setPeriod(StatsPeriod.ALL) },
                label = { Text("全部") },
            )
        }

        if (state.period != StatsPeriod.ALL) {
            PeriodNavigator(
                title = periodTitle(state),
                canGoNext = state.canGoNext,
                onPrevious = viewModel::previous,
                onNext = viewModel::next,
            )
            Text("显示条目", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ChartMetric.entries.forEach { metric ->
                    val selected = metric in state.selectedMetrics
                    val color = metric.chartColor(dark)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleMetric(metric) },
                        label = { Text(metric.label) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.18f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val chart = state.chart
                    if (chart == null || state.selectedMetrics.isEmpty()) {
                        Text(
                            "请选择要显示的条目",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                        )
                    } else {
                        Text(
                            if (state.period == StatsPeriod.MONTH) "按日" else "按月合计",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        MultiLineChart(
                            xLabels = chart.xLabels,
                            lines = chart.series.map { series ->
                                ChartLine(
                                    name = series.metric.label,
                                    color = series.metric.chartColor(dark),
                                    values = series.values,
                                )
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }

        val stats = state.stats
        if (stats == null) {
            Text("暂无数据")
        } else {
            Text(
                text = "${stats.from} 至 ${stats.to}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatCard("记录天数", stats.recordedDays.toString())
            StatCard("干净天数", stats.cleanDays.toString())
            StatCard("破戒天数", stats.relapseDays.toString())
            StatCard("自慰次数", stats.masturbationTotal.toString())
            StatCard("看黄天数", stats.pornDays.toString())
            StatCard("房事次数", stats.intercourseTotal.toString())
            StatCard("遗精天数", stats.nocturnalEmissionDays.toString())
            StatCard("熬夜天数", stats.stayUpLateDays.toString())
            StatCard("通宵天数", stats.allNighterDays.toString())
            StatCard(
                "破戒平均间隔",
                stats.averageRelapseIntervalDays?.let { String.format(Locale.CHINA, "%.1f 天", it) } ?: "—",
            )
        }
    }
}

@Composable
private fun PeriodNavigator(
    title: String,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一段")
        }
        Text(title, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一段")
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun periodTitle(state: StatsUiState): String {
    return when (state.period) {
        StatsPeriod.MONTH -> state.month.format(DateTimeFormatter.ofPattern("yyyy年M月"))
        StatsPeriod.YEAR -> "${state.year}年"
        StatsPeriod.ALL -> "全部"
    }
}

private fun ChartMetric.chartColor(dark: Boolean): Color {
    return when (this) {
        ChartMetric.RELAPSE -> if (dark) Color(0xFFFFB4AB) else Color(0xFFB42318)
        ChartMetric.MASTURBATION -> if (dark) Color(0xFF7AD1C2) else Color(0xFF0F6B5C)
        ChartMetric.PORN -> if (dark) Color(0xFFFFB77C) else Color(0xFFC2410C)
        ChartMetric.INTERCOURSE -> if (dark) Color(0xFFB4C7DB) else Color(0xFF3F5B76)
        ChartMetric.EMISSION -> if (dark) Color(0xFFD0BCFF) else Color(0xFF6D28D9)
        ChartMetric.STAY_UP_LATE -> if (dark) Color(0xFFFFD08A) else Color(0xFFB45309)
        ChartMetric.ALL_NIGHTER -> if (dark) Color(0xFFC4C7C5) else Color(0xFF4B5563)
    }
}
