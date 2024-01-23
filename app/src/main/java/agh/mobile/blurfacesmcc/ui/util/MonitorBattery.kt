package agh.mobile.blurfacesmcc.ui.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

class MonitorBattery(
    private val context: Context
) {

    private val batteryStatus: Intent? =
        IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

    private val batteryManager
        get() = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager


    val batteryPct: Float
        get() = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            Log.d("xdd", "Btrly lvl: $level")
            Log.d("xdd", "Btry lvl: $scale")

            level * 100 / scale.toFloat()
        } ?: 0.0f

    val batteryLevel
        get() = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    val batteryCapacity
        get() = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

}