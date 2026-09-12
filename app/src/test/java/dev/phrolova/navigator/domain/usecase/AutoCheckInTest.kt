package dev.phrolova.navigator.domain.usecase

import dev.phrolova.navigator.FakeRecordRepository
import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.model.DailyRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AutoCheckInTest {
    private val today = LocalDate.of(2026, 9, 12)
    private val clock = Clock { today }

    @Test
    fun skipsEntirelyWhenTodayAlreadyRecorded() = runBlocking {
        val repo = FakeRecordRepository()
        repo.setTrackingStartDate(today.minusDays(10))
        repo.upsert(DailyRecord(date = today, masturbationCount = 1))
        val filled = AutoCheckIn(repo, SaveCheckIn(repo, clock), clock)()
        assertTrue(filled.isEmpty())
        assertEquals(1, repo.getAll().size)
        assertEquals(1, repo.get(today)?.masturbationCount)
        assertNull(repo.get(today.minusDays(1)))
    }

    @Test
    fun fillsSixEmptyDaysAndSetsTrackingStart() = runBlocking {
        val repo = FakeRecordRepository()
        val filled = AutoCheckIn(repo, SaveCheckIn(repo, clock), clock)()
        val expected = (5 downTo 0).map { today.minusDays(it.toLong()) }
        assertEquals(expected, filled)
        assertEquals(today.minusDays(5), repo.getTrackingStartDate())
        expected.forEach { date ->
            val record = repo.get(date)!!
            assertEquals(0, record.masturbationCount)
            assertEquals(0, record.intercourseCount)
            assertEquals(false, record.viewedPorn)
            assertEquals(false, record.nocturnalEmission)
            assertNull(record.badHabit)
        }
    }

    @Test
    fun onlyFillsGapsAndDoesNotOverwrite() = runBlocking {
        val repo = FakeRecordRepository()
        repo.setTrackingStartDate(today.minusDays(10))
        val existing = DailyRecord(date = today.minusDays(3), masturbationCount = 2, viewedPorn = true)
        repo.upsert(existing)
        val filled = AutoCheckIn(repo, SaveCheckIn(repo, clock), clock)()
        assertEquals(
            listOf(
                today.minusDays(5),
                today.minusDays(4),
                today.minusDays(2),
                today.minusDays(1),
                today,
            ),
            filled,
        )
        assertEquals(existing, repo.get(today.minusDays(3)))
    }

    @Test
    fun doesNotFillBeforeTrackingStart() = runBlocking {
        val repo = FakeRecordRepository()
        repo.setTrackingStartDate(today.minusDays(2))
        val filled = AutoCheckIn(repo, SaveCheckIn(repo, clock), clock)()
        assertEquals(
            listOf(today.minusDays(2), today.minusDays(1), today),
            filled,
        )
        assertNull(repo.get(today.minusDays(5)))
        assertNull(repo.get(today.minusDays(4)))
        assertNull(repo.get(today.minusDays(3)))
        assertEquals(today.minusDays(2), repo.getTrackingStartDate())
    }
}
