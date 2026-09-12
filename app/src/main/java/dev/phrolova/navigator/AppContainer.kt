package dev.phrolova.navigator

import android.content.Context
import dev.phrolova.navigator.data.RoomRecordRepository
import dev.phrolova.navigator.data.local.NavigatorDatabase
import dev.phrolova.navigator.domain.BackupParser
import dev.phrolova.navigator.domain.ChartCalculator
import dev.phrolova.navigator.domain.Clock
import dev.phrolova.navigator.domain.RecordRepository
import dev.phrolova.navigator.domain.StatsCalculator
import dev.phrolova.navigator.domain.StreakCalculator
import dev.phrolova.navigator.domain.SystemClock
import dev.phrolova.navigator.domain.usecase.AutoCheckIn
import dev.phrolova.navigator.domain.usecase.ExportBackup
import dev.phrolova.navigator.domain.usecase.GetRecord
import dev.phrolova.navigator.domain.usecase.ImportBackup
import dev.phrolova.navigator.domain.usecase.ObserveHomeState
import dev.phrolova.navigator.domain.usecase.ObserveMonth
import dev.phrolova.navigator.domain.usecase.ObserveStats
import dev.phrolova.navigator.domain.usecase.SaveCheckIn

class AppContainer(context: Context) {
    private val database = NavigatorDatabase.create(context)
    val repository: RecordRepository = RoomRecordRepository(database.recordDao())
    val clock: Clock = SystemClock()
    val parser = BackupParser()
    val streakCalculator = StreakCalculator()
    val statsCalculator = StatsCalculator()
    val chartCalculator = ChartCalculator()
    val saveCheckIn = SaveCheckIn(repository, clock)
    val autoCheckIn = AutoCheckIn(repository, saveCheckIn, clock)
    val getRecord = GetRecord(repository)
    val observeHomeState = ObserveHomeState(repository, streakCalculator, clock)
    val observeMonth = ObserveMonth(repository)
    val observeStats = ObserveStats(repository, statsCalculator, clock)
    val importBackup = ImportBackup(repository, parser)
    val exportBackup = ExportBackup(repository, parser)
    val contentResolver = context.applicationContext.contentResolver
}
