package dev.phrolova.navigator.domain.usecase

import dev.phrolova.navigator.FakeRecordRepository
import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.model.DailyRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SaveCheckInTest {
    private val today = LocalDate.of(2024, 8, 20)

    @Test
    fun savesAndSetsTrackingStartOnFirstRecord() = runBlocking {
        val repo = FakeRecordRepository()
        val useCase = SaveCheckIn(repo, Clock { today })
        useCase(DailyRecord(date = today, masturbationCount = 1))
        assertEquals(today, repo.getTrackingStartDate())
        assertEquals(1, repo.get(today)?.masturbationCount)
    }

    @Test
    fun doesNotOverwriteExistingTrackingStart() = runBlocking {
        val repo = FakeRecordRepository()
        val start = LocalDate.of(2024, 8, 16)
        repo.setTrackingStartDate(start)
        SaveCheckIn(repo, Clock { today })(DailyRecord(date = today))
        assertEquals(start, repo.getTrackingStartDate())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFutureDate(): Unit = runBlocking {
        val repo = FakeRecordRepository()
        SaveCheckIn(repo, Clock { today })(DailyRecord(date = today.plusDays(1)))
        Unit
    }
}
