package com.example.neatnest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.neatnest.data.model.DeviceSnapshot
import com.example.neatnest.data.model.ProcessedFile
import com.example.neatnest.data.model.ProcessedNotification
import com.example.neatnest.data.model.TrackedFolder

// room database with singleton access
@Database(
    entities = [ProcessedFile::class, ProcessedNotification::class, TrackedFolder::class, DeviceSnapshot::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun processedFileDao(): ProcessedFileDao
    abstract fun processedNotificationDao(): ProcessedNotificationDao
    abstract fun trackedFolderDao(): TrackedFolderDao
    abstract fun deviceSnapshotDao(): DeviceSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v4→v5: add engineUsed and category columns
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE processed_files ADD COLUMN engineUsed TEXT NOT NULL DEFAULT 'legacy'")
                db.execSQL("ALTER TABLE processed_files ADD COLUMN category TEXT NOT NULL DEFAULT ''")
            }
        }

        // v5→v6: add device_snapshots table
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS device_snapshots (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        ramTotalMb INTEGER NOT NULL,
                        ramAvailableMb INTEGER NOT NULL,
                        storageTotalMb INTEGER NOT NULL,
                        storageAvailableMb INTEGER NOT NULL,
                        batteryLevel INTEGER NOT NULL,
                        batteryTemperature REAL NOT NULL,
                        isCharging INTEGER NOT NULL DEFAULT 0,
                        cpuUsagePercent REAL NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // koin entry point
        fun build(context: Context): AppDatabase = getDatabase(context)

        // singleton accessor
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neatnest_database"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
