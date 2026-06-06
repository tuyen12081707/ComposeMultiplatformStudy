package com.panda.study1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

/**
 * Tự động dò tìm đường dẫn thực thi của ADB trên mọi hệ điều hành (Mac, Win, Linux)
 */
fun getAdbPath(): String {
    val osName = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")
    val isWindows = osName.contains("win")

    val adbName = if (isWindows) "adb.exe" else "adb"

    // 1. Thử tìm trong biến môi trường (Nếu máy có set)
    val sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (sdkRoot != null) {
        val adbFile = File("$sdkRoot/platform-tools/$adbName")
        if (adbFile.exists()) return adbFile.absolutePath
    }

    // 2. Thử tìm ở các đường dẫn mặc định của Android Studio
    val defaultSdkPath = when {
        isWindows -> "$userHome\\AppData\\Local\\Android\\Sdk\\platform-tools\\$adbName"
        osName.contains("mac") -> "$userHome/Library/Android/sdk/platform-tools/$adbName"
        else -> "$userHome/Android/Sdk/platform-tools/$adbName" // Ubuntu/Linux
    }

    val defaultFile = File(defaultSdkPath)
    if (defaultFile.exists()) {
        return defaultFile.absolutePath
    }

    // 3. Đường cùng: Trả về "adb" hy vọng máy có sẵn trong PATH toàn cục
    return adbName
}

/**
 * Runs `adb install-multiple -i com.android.vending <apkPaths...>` and streams
 * stdout/stderr lines back as a cold [Flow].
 *
 * The process is launched on [Dispatchers.IO] so callers do not need to switch
 * dispatchers themselves.
 *
 * @param apkFiles  List of .apk [File]s to install.
 * @return          A [Flow] emitting each output line from the ADB process.
 */
fun installApksViaAdb(apkFiles: List<File>): Flow<String> = flow {
    if (apkFiles.isEmpty()) {
        emit("ERROR: No .apk files found to install.")
        return@flow
    }

    // Lấy chính xác vị trí file ADB bằng hàm vừa tạo
    val adbExecutable = getAdbPath()

    val command = buildList {
        add(adbExecutable) // Thay thế chữ "adb" bằng biến này
        add("install-multiple")
        add("-i")
        add("com.android.vending")
        apkFiles.forEach { add(it.absolutePath) }
    }

    emit("$ ${command.joinToString(" ")}")

    val process = ProcessBuilder(command)
        .redirectErrorStream(true)   // merge stderr into stdout
        .start()

    process.inputStream.bufferedReader().use { reader ->
        var line = reader.readLine()
        while (line != null) {
            emit(line)
            line = reader.readLine()
        }
    }

    val exitCode = process.waitFor()
    if (exitCode == 0) {
        emit("✅ Installation completed successfully (exit code: $exitCode)")
    } else {
        emit("❌ ADB process exited with code: $exitCode")
    }
}.flowOn(Dispatchers.IO)