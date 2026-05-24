package com.example.neatnest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

// receives battery change broadcasts for real-time updates
class BatteryReceiver : BroadcastReceiver() {

    var onBatteryChanged: ((level: Int, temp: Float, charging: Boolean) -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_CHANGED) return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val pct = (level * 100) / scale
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        onBatteryChanged?.invoke(pct, temp, charging)
        Log.d("BatteryReceiver", "Battery: $pct%, ${temp}°C, charging=$charging")
    }
}
