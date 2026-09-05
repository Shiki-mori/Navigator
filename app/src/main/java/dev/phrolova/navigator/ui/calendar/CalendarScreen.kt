package dev.phrolova.navigator.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.phrolova.navigator.domain.model.DayStatus
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val PagerBaseMonth: YearMonth = YearMonth.of(2000, 1)
private const val PagerPageCount: Int = 80 * 12

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenDay: (LocalDate) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = monthToPage(YearMonth.from(state.today)),
        pageCount = { PagerPageCount },
    )
    val scope = rememberCoroutineScope()
    val displayedMonth = pageToMonth(pagerState.currentPage)

    LaunchedEffect(pagerState.settledPage) {
        viewModel.setMonth(pageToMonth(pagerState.settledPage))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                    }
                },
                enabled = pagerState.currentPage > 0,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一月")
            }
            Text(
                text = displayedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                style = MaterialTheme.typography.titleLarge,
            )
            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            (pagerState.currentPage + 1).coerceAtMost(PagerPageCount - 1),
                        )
                    }
                },
                enabled = pagerState.currentPage < PagerPageCount - 1,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一月")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) { page ->
            MonthGrid(
                month = pageToMonth(page),
                today = state.today,
                statuses = state.statuses,
                onOpenDay = onOpenDay,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Legend(MaterialTheme.colorScheme.primaryContainer, "干净")
            Legend(MaterialTheme.colorScheme.errorContainer, "破戒")
            Legend(MaterialTheme.colorScheme.surfaceVariant, "未记录")
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color)
                .padding(6.dp),
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    statuses: Map<LocalDate, DayStatus>,
    onOpenDay: (LocalDate) -> Unit,
) {
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - DayOfWeek.MONDAY.value
    val days = month.lengthOfMonth()
    Column(modifier = Modifier.fillMaxWidth()) {
        var dayIndex = 0
        repeat(6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) {
                    val dateIndex = dayIndex - offset + 1
                    dayIndex++
                    if (dateIndex in 1..days) {
                        val date = month.atDay(dateIndex)
                        val status = statuses[date] ?: DayStatus.UNRECORDED
                        val enabled = !date.isAfter(today)
                        DayCell(
                            day = dateIndex,
                            status = status,
                            isToday = date == today,
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            onClick = { if (enabled) onOpenDay(date) },
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    status: DayStatus,
    isToday: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val bg = when (status) {
        DayStatus.CLEAN -> MaterialTheme.colorScheme.primaryContainer
        DayStatus.RELAPSE -> MaterialTheme.colorScheme.errorContainer
        DayStatus.UNRECORDED -> Color.Transparent
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.toString(),
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        )
    }
}

internal fun monthToPage(month: YearMonth): Int {
    return ChronoUnit.MONTHS.between(PagerBaseMonth, month).toInt().coerceIn(0, PagerPageCount - 1)
}

internal fun pageToMonth(page: Int): YearMonth {
    return PagerBaseMonth.plusMonths(page.toLong())
}
