package com.panda.study1

import platform.UIKit.UIDevice

actual class BatteryManager {
    actual fun getBatteryLevel(): Int {
        UIDevice.currentDevice.batteryMonitoringEnabled = true

        val level = UIDevice.currentDevice.batteryLevel
        return (level * 100).toInt()
    }

}