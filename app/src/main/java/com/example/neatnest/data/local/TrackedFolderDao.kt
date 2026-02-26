package com.example.neatnest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neatnest.data.model.TrackedFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedFolderDao {
    @Query("SELECT * FROM tracked_folders")
    fun getAllTrackedFolders(): Flow<List<TrackedFolder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: TrackedFolder)

    @Delete
    suspend fun removeFolder(folder: TrackedFolder)
}

