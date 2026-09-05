package dev.phrolova.navigator.domain.usecase

import dev.phrolova.navigator.domain.BackupParser
import dev.phrolova.navigator.domain.ImportPayload
import dev.phrolova.navigator.domain.RecordRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ImportBackup(
    private val repository: RecordRepository,
    private val parser: BackupParser,
) {
    suspend operator fun invoke(jsonText: String): Int {
        val payload = parser.parse(jsonText)
        repository.replaceAll(payload.records, payload.trackingStartDate)
        return payload.records.size
    }
}

class ExportBackup(
    private val repository: RecordRepository,
    private val parser: BackupParser,
) {
    suspend operator fun invoke(now: LocalDateTime = LocalDateTime.now()): String {
        val payload = ImportPayload(
            trackingStartDate = repository.getTrackingStartDate(),
            records = repository.getAll(),
        )
        return parser.exportNative(payload, now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
}
