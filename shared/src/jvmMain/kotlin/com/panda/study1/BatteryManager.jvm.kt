package com.panda.study1

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

actual class BatteryManager {
    actual fun getBatteryLevel(): Int {
        val osName = System.getProperty("os.name").lowercase()

        return try {
            when {
                osName.contains("win") -> getWindowsBattery()
                osName.contains("mac") -> getMacBattery()
                osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> getLinuxBattery()
                else -> -1 // Không nhận diện được OS
            }
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // Báo lỗi (Trả về -1 nghĩa là không lấy được)
        }
    }

    private fun getWindowsBattery(): Int {
        // Chạy lệnh CMD của Windows để lấy % pin
        val process = Runtime.getRuntime().exec("wmic path Win32_Battery get EstimatedChargeRemaining")
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        reader.readLine() // Bỏ qua dòng tiêu đề đầu tiên
        val batteryStr = reader.readLine()?.trim()

        return batteryStr?.toIntOrNull() ?: -1
    }

    private fun getMacBattery(): Int {
        // Chạy lệnh Terminal của macOS
        val process = Runtime.getRuntime().exec("pmset -g batt")
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        reader.readLine() // Bỏ qua dòng thông báo nguồn (AC/Battery)
        val line = reader.readLine() ?: return -1

        // Dữ liệu trả về có dạng: -InternalBattery-0 100%; discharging...
        // Dùng Regex để tách lấy đúng con số trước dấu %
        val regex = "(\\d+)%".toRegex()
        val matchResult = regex.find(line)

        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: -1
    }

    private fun getLinuxBattery(): Int {
        // Trên Linux, thông tin pin được lưu thẳng trong file hệ thống
        val file = File("/sys/class/power_supply/BAT0/capacity")
        if (file.exists()) {
            return file.readText().trim().toIntOrNull() ?: -1
        }

        // Đề phòng trường hợp pin nằm ở BAT1
        val file2 = File("/sys/class/power_supply/BAT1/capacity")
        if (file2.exists()) {
            return file2.readText().trim().toIntOrNull() ?: -1
        }

        return -1
    }
}