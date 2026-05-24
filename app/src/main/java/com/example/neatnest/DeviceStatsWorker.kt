package com.example.neatnest

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.neatnest.data.local.AppDatabase
import com.example.neatnest.data.repository.DeviceRepository

// periodic worker: saves a device snapshot every 15 minutes
class DeviceStatsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repo = DeviceRepository(applicationContext, db.deviceSnapshotDao())
            val snapshot = repo.takeSnapshot()
            Log.d("DeviceStatsWorker", "Snapshot saved: RAM ${snapshot.ramAvailableMb}/${snapshot.ramTotalMb}MB, Battery ${snapshot.batteryLevel}%")

            // cleanup: keep only last 7 days of data
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            repo.deleteOlderThan(sevenDaysAgo)

            Result.success()
        } catch (e: Exception) {
            Log.e("DeviceStatsWorker", "Failed to save snapshot", e)
            Result.retry()
        }
    }
}
