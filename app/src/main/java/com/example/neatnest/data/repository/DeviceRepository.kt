package com.example.neatnest.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.neatnest.data.local.DeviceSnapshotDao
import com.example.neatnest.data.model.DeviceSnapshot
import kotlinx.coroutines.flow.Flow
import java.io.RandomAccessFile

// wraps system APIs + DAO for device stats
class DeviceRepository(
    private val context: Context,
    private val dao: DeviceSnapshotDao
) {

    // --- Live System Stats ---

    fun getRamInfo(): Pair<Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val totalMb = mi.totalMem / (1024 * 1024)
        val availMb = mi.availMem / (1024 * 1024)
        return Pair(totalMb, availMb)
    }

    fun getStorageInfo(): Pair<Long, Long> {
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalMb = (stat.blockSizeLong * stat.blockCountLong) / (1024 * 1024)
        val availMb = (stat.blockSizeLong * stat.availableBlocksLong) / (1024 * 1024)
        return Pair(totalMb, availMb)
    }

    fun getBatteryInfo(): Triple<Int, Float, Boolean> {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = (level * 100) / scale
        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        return Triple(pct, temp, charging)
    }

    fun getCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val line1 = reader.readLine()
            reader.close()
            Thread.sleep(300)
            val reader2 = RandomAccessFile("/proc/stat", "r")
            val line2 = reader2.readLine()
            reader2.close()

            val toks1 = line1.split("\\s+".toRegex())
            val toks2 = line2.split("\\s+".toRegex())

            val idle1 = toks1[4].toLong()
            val idle2 = toks2[4].toLong()
            val total1 = toks1.drop(1).take(7).sumOf { it.toLong() }
            val total2 = toks2.drop(1).take(7).sumOf { it.toLong() }

            val totalDiff = total2 - total1
            val idleDiff = idle2 - idle1
            if (totalDiff > 0) ((totalDiff - idleDiff).toFloat() / totalDiff * 100f) else 0f
        } catch (e: Exception) {
            0f
        }
    }

    fun getRunningApps(): List<ActivityManager.RunningAppProcessInfo> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses ?: emptyList()
    }

    data class NetworkInfo(
        val type: String,
        val isConnected: Boolean,
        val wifiSsid: String?,
        val ipAddress: String?,
        val linkSpeed: Int?
    )

    fun getNetworkInfo(): NetworkInfo {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }

        val isConnected = caps != null
        val type = when {
            caps == null -> "Disconnected"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }

        var ssid: String? = null
        var ip: String? = null
        var speed: Int? = null

        if (type == "WiFi") {
            try {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val info = wm.connectionInfo
                ssid = info.ssid?.replace("\"", "") ?: "Unknown"
                speed = info.linkSpeed
                val ipInt = info.ipAddress
                ip = "${ipInt and 0xFF}.${(ipInt shr 8) and 0xFF}.${(ipInt shr 16) and 0xFF}.${(ipInt shr 24) and 0xFF}"
            } catch (_: Exception) {}
        }

        return NetworkInfo(type, isConnected, ssid, ip, speed)
    }

    // --- Take a full snapshot and save to DB ---

    suspend fun takeSnapshot(): DeviceSnapshot {
        val (ramTotal, ramAvail) = getRamInfo()
        val (storageTotal, storageAvail) = getStorageInfo()
        val (batteryPct, batteryTemp, charging) = getBatteryInfo()
        val cpu = getCpuUsage()

        val snapshot = DeviceSnapshot(
            ramTotalMb = ramTotal,
            ramAvailableMb = ramAvail,
            storageTotalMb = storageTotal,
            storageAvailableMb = storageAvail,
            batteryLevel = batteryPct,
            batteryTemperature = batteryTemp,
            isCharging = charging,
            cpuUsagePercent = cpu
        )
        dao.insert(snapshot)
        return snapshot
    }

    // --- DB Queries ---

    fun getSnapshotsSince(since: Long): Flow<List<DeviceSnapshot>> = dao.getSnapshotsSince(since)
    fun getRecent(limit: Int): Flow<List<DeviceSnapshot>> = dao.getRecent(limit)
    suspend fun getCount(): Int = dao.getCount()
    suspend fun deleteOlderThan(before: Long) = dao.deleteOlderThan(before)
}
