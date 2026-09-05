package dev.phrolova.navigator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyRecordEntity::class, AppMetaEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NavigatorDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        fun create(context: Context): NavigatorDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                NavigatorDatabase::class.java,
                "navigator.db",
            ).build()
        }
    }
}
