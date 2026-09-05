package dev.phrolova.navigator.ui.calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class CalendarPagerMappingTest {
    @Test
    fun baseMonthIsPageZero() {
        assertEquals(0, monthToPage(YearMonth.of(2000, 1)))
        assertEquals(YearMonth.of(2000, 1), pageToMonth(0))
    }

    @Test
    fun todayMonthRoundTrips() {
        val month = YearMonth.of(2026, 9)
        val page = monthToPage(month)
        assertEquals(month, pageToMonth(page))
        assertEquals(page + 1, monthToPage(month.plusMonths(1)))
        assertEquals(page - 1, monthToPage(month.minusMonths(1)))
    }
}
