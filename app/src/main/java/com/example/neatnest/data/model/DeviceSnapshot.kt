package com.example.neatnest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// device stats snapshot stored periodically for historical analysis
@Entity(tableName = "device_snapshots")
data class DeviceSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val ramTotalMb: Long,
    val ramAvailableMb: Long,
    val storageTotalMb: Long,
    val storageAvailableMb: Long,
    val batteryLevel: Int,
    val batteryTemperature: Float,
    val isCharging: Boolean = false,
    val cpuUsagePercent: Float = 0f
)
