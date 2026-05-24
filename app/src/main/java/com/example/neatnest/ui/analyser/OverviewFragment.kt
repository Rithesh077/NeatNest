package com.example.neatnest.ui.analyser

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.neatnest.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

// overview tab: RAM, Storage, Battery, CPU summary cards
class OverviewFragment : Fragment() {

    private val viewModel: DeviceAnalyserViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvRam = view.findViewById<TextView>(R.id.tvRamUsage)
        val pbRam = view.findViewById<ProgressBar>(R.id.pbRam)
        val tvStorage = view.findViewById<TextView>(R.id.tvStorageUsage)
        val pbStorage = view.findViewById<ProgressBar>(R.id.pbStorage)
        val tvBattery = view.findViewById<TextView>(R.id.tvBattery)
        val tvBatteryDetails = view.findViewById<TextView>(R.id.tvBatteryDetails)
        val tvCpu = view.findViewById<TextView>(R.id.tvCpuUsage)
        val pbCpu = view.findViewById<ProgressBar>(R.id.pbCpu)
        val tvDeviceInfo = view.findViewById<TextView>(R.id.tvDeviceInfo)

        // device info (static)
        tvDeviceInfo.text = buildString {
            append("Model: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
            append("Board: ${Build.BOARD}\n")
            append("Hardware: ${Build.HARDWARE}")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentStats.collect { stats ->
                    if (stats == null) return@collect

                    val ramUsed = stats.ramTotalMb - stats.ramAvailableMb
                    val ramPct = if (stats.ramTotalMb > 0) (ramUsed * 100 / stats.ramTotalMb).toInt() else 0
                    tvRam.text = "${ramUsed} / ${stats.ramTotalMb} MB"
                    pbRam.progress = ramPct

                    val storageUsedGb = (stats.storageTotalMb - stats.storageAvailableMb) / 1024f
                    val storageTotalGb = stats.storageTotalMb / 1024f
                    val storagePct = if (stats.storageTotalMb > 0) ((stats.storageTotalMb - stats.storageAvailableMb) * 100 / stats.storageTotalMb).toInt() else 0
                    tvStorage.text = "%.1f / %.1f GB".format(storageUsedGb, storageTotalGb)
                    pbStorage.progress = storagePct

                    tvBattery.text = "${stats.batteryLevel}%"
                    val chargingText = if (stats.isCharging) "Charging" else "Not charging"
                    tvBatteryDetails.text = "%.1f°C • %s".format(stats.batteryTemperature, chargingText)

                    tvCpu.text = "%.1f%%".format(stats.cpuUsagePercent)
                    pbCpu.progress = stats.cpuUsagePercent.toInt().coerceIn(0, 100)
                }
            }
        }
    }
}
