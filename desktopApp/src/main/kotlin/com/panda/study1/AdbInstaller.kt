package com.panda.study1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

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

    val command = buildList {
        add("adb")
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
