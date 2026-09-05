package dev.phrolova.navigator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class FullBackupImportTest {
    @Test
    fun parseCheckedInPersonalBackupIfPresent() {
        val file = listOf(
            File("backup.txt"),
            File("../backup.txt"),
            File("../../backup.txt"),
        ).firstOrNull { it.isFile } ?: return
        val payload = BackupParser().parse(file.readText())
        assertEquals(LocalDate.of(2024, 8, 16), payload.trackingStartDate)
        assertEquals(896, payload.records.size)
        val last = payload.records.maxBy { it.date }
        assertEquals(LocalDate.of(2026, 9, 5), last.date)
        assertTrue(last.isRelapse)
        assertTrue(payload.records.any { it.badHabit != null })
    }
}
