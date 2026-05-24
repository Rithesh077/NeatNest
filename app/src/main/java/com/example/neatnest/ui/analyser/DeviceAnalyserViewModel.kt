package com.example.neatnest.ui.analyser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neatnest.data.model.DeviceSnapshot
import com.example.neatnest.data.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// reactive ViewModel for device analyser
class DeviceAnalyserViewModel(private val repo: DeviceRepository) : ViewModel() {

    private val _currentStats = MutableStateFlow<DeviceSnapshot?>(null)
    val currentStats: StateFlow<DeviceSnapshot?> = _currentStats.asStateFlow()

    private val _history = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    val history: StateFlow<List<DeviceSnapshot>> = _history.asStateFlow()

    private val _networkInfo = MutableStateFlow<DeviceRepository.NetworkInfo?>(null)
    val networkInfo: StateFlow<DeviceRepository.NetworkInfo?> = _networkInfo.asStateFlow()

    private val _runningApps = MutableStateFlow<List<android.app.ActivityManager.RunningAppProcessInfo>>(emptyList())
    val runningApps: StateFlow<List<android.app.ActivityManager.RunningAppProcessInfo>> = _runningApps.asStateFlow()

    // time range for charts (default 1 hour)
    private var chartRangeMs = 60 * 60 * 1000L

    init {
        refresh()
        loadHistory()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val (ramTotal, ramAvail) = repo.getRamInfo()
            val (storageTotal, storageAvail) = repo.getStorageInfo()
            val (batteryPct, batteryTemp, charging) = repo.getBatteryInfo()
            val cpu = repo.getCpuUsage()

            _currentStats.value = DeviceSnapshot(
                ramTotalMb = ramTotal,
                ramAvailableMb = ramAvail,
                storageTotalMb = storageTotal,
                storageAvailableMb = storageAvail,
                batteryLevel = batteryPct,
                batteryTemperature = batteryTemp,
                isCharging = charging,
                cpuUsagePercent = cpu
            )

            _runningApps.value = repo.getRunningApps()
            _networkInfo.value = repo.getNetworkInfo()
        }
    }

    fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val since = System.currentTimeMillis() - chartRangeMs
            repo.getSnapshotsSince(since).collect { snapshots ->
                _history.value = snapshots
            }
        }
    }

    fun setChartRange(rangeMs: Long) {
        chartRangeMs = rangeMs
        loadHistory()
    }

    fun updateBattery(level: Int, temp: Float, charging: Boolean) {
        val current = _currentStats.value ?: return
        _currentStats.value = current.copy(
            batteryLevel = level,
            batteryTemperature = temp,
            isCharging = charging
        )
    }

    suspend fun takeSnapshot(): DeviceSnapshot {
        return repo.takeSnapshot()
    }
}
