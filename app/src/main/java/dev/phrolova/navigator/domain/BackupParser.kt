package dev.phrolova.navigator.domain

import dev.phrolova.navigator.domain.model.BadHabit
import dev.phrolova.navigator.domain.model.DailyRecord
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BackupParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    },
) {
    fun parse(text: String): ImportPayload {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "备份文件为空" }
        val root = json.parseToJsonElement(trimmed).jsonObject
        val format = root["format"]?.jsonPrimitive?.content
        return if (format == NATIVE_FORMAT) {
            parseNative(trimmed)
        } else {
            parseLegacy(trimmed)
        }
    }

    fun exportNative(payload: ImportPayload, exportedAt: String): String {
        val dto = NativeBackupDto(
            format = NATIVE_FORMAT,
            version = 1,
            exportedAt = exportedAt,
            trackingStartDate = payload.trackingStartDate?.toString(),
            records = payload.records
                .sortedBy { it.date }
                .map { record ->
                    NativeRecordDto(
                        date = record.date.toString(),
                        masturbationCount = record.masturbationCount,
                        intercourseCount = record.intercourseCount,
                        viewedPorn = record.viewedPorn,
                        nocturnalEmission = record.nocturnalEmission,
                        badHabit = record.badHabit?.name,
                    )
                },
        )
        return json.encodeToString(NativeBackupDto.serializer(), dto)
    }

    private fun parseNative(text: String): ImportPayload {
        val dto = json.decodeFromString(NativeBackupDto.serializer(), text)
        val records = dto.records.map { item ->
            DailyRecord(
                date = LocalDate.parse(item.date),
                masturbationCount = item.masturbationCount.coerceAtLeast(0),
                intercourseCount = item.intercourseCount.coerceAtLeast(0),
                viewedPorn = item.viewedPorn,
                nocturnalEmission = item.nocturnalEmission,
                badHabit = BadHabit.fromStored(item.badHabit),
            )
        }
        return ImportPayload(
            trackingStartDate = dto.trackingStartDate?.let(LocalDate::parse),
            records = collapseByDate(records),
        )
    }

    private fun parseLegacy(text: String): ImportPayload {
        val dto = json.decodeFromString(LegacyBackupDto.serializer(), text)
        val records = dto.records.map { item ->
            DailyRecord(
                date = parseCompactDate(item.calendar),
                masturbationCount = item.zw.coerceAtLeast(0),
                intercourseCount = item.fs.coerceAtLeast(0),
                viewedPorn = item.kh != 0,
                nocturnalEmission = item.yj != 0,
                badHabit = BadHabit.fromStored(item.badhabit),
            )
        }
        return ImportPayload(
            trackingStartDate = dto.firstStart?.let(::parseCompactDate),
            records = collapseByDate(records),
        )
    }

    private fun collapseByDate(records: List<DailyRecord>): List<DailyRecord> {
        val byDate = LinkedHashMap<LocalDate, DailyRecord>()
        for (record in records) {
            byDate[record.date] = record
        }
        return byDate.values.sortedBy { it.date }
    }

    private fun parseCompactDate(value: Int): LocalDate {
        return LocalDate.parse(value.toString(), COMPACT)
    }

    companion object {
        const val NATIVE_FORMAT = "navigator.backup"
        private val COMPACT: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE
    }
}

@Serializable
private data class NativeBackupDto(
    val format: String = BackupParser.NATIVE_FORMAT,
    val version: Int = 1,
    val exportedAt: String,
    val trackingStartDate: String? = null,
    val records: List<NativeRecordDto> = emptyList(),
)

@Serializable
private data class NativeRecordDto(
    val date: String,
    val masturbationCount: Int = 0,
    val intercourseCount: Int = 0,
    val viewedPorn: Boolean = false,
    val nocturnalEmission: Boolean = false,
    val badHabit: String? = null,
)

@Serializable
private data class LegacyBackupDto(
    val version: Int? = null,
    @SerialName("backup_time") val backupTime: String? = null,
    @SerialName("first_start") val firstStart: Int? = null,
    val records: List<LegacyRecordDto> = emptyList(),
)

@Serializable
private data class LegacyRecordDto(
    val calendar: Int,
    val zw: Int = 0,
    val fs: Int = 0,
    val kh: Int = 0,
    val yj: Int = 0,
    val badhabit: String? = null,
)
