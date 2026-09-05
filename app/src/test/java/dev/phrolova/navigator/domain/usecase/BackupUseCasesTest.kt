package dev.phrolova.navigator.domain.usecase

import dev.phrolova.navigator.FakeRecordRepository
import dev.phrolova.navigator.domain.BackupParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BackupUseCasesTest {
    @Test
    fun importThenExportRoundTrip() = runBlocking {
        val repo = FakeRecordRepository()
        val parser = BackupParser()
        val importer = ImportBackup(repo, parser)
        val exporter = ExportBackup(repo, parser)
        val source = javaClass.classLoader!!.getResource("legacy_sample.json")!!.readText()
        assertEquals(6, importer(source))
        val exported = exporter()
        assertFalse(exported.contains("\"yy\""))
        val again = parser.parse(exported)
        assertEquals(repo.getTrackingStartDate(), again.trackingStartDate)
        assertEquals(repo.getAll(), again.records)
        assertEquals(6, importer(exported))
        assertEquals(again.records, repo.getAll())
    }
}
