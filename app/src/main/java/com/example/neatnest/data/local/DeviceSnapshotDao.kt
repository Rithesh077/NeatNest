package com.example.neatnest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neatnest.data.model.DeviceSnapshot
import kotlinx.coroutines.flow.Flow

// queries for device stats snapshots
@Dao
interface DeviceSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: DeviceSnapshot)

    // most recent snapshot
    @Query("SELECT * FROM device_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): DeviceSnapshot?

    // snapshots in a time range (for charts)
    @Query("SELECT * FROM device_snapshots WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getSnapshotsSince(since: Long): Flow<List<DeviceSnapshot>>

    // recent N snapshots
    @Query("SELECT * FROM device_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DeviceSnapshot>>

    // cleanup: delete older than given timestamp
    @Query("DELETE FROM device_snapshots WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    // total count
    @Query("SELECT COUNT(*) FROM device_snapshots")
    suspend fun getCount(): Int

    // delete all
    @Query("DELETE FROM device_snapshots")
    suspend fun deleteAll()
}
