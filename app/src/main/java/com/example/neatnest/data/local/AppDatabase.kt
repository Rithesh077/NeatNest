package com.example.neatnest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.neatnest.data.model.ProcessedFile
import com.example.neatnest.data.model.ProcessedNotification
import com.example.neatnest.data.model.TrackedFolder

// room database with singleton access
@Database(
    entities = [ProcessedFile::class, ProcessedNotification::class, TrackedFolder::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun processedFileDao(): ProcessedFileDao
    abstract fun processedNotificationDao(): ProcessedNotificationDao
    abstract fun trackedFolderDao(): TrackedFolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

