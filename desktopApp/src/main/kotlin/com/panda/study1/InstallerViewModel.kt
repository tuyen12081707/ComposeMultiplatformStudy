package com.panda.study1

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Coordinates the full install pipeline:
 * 1. Determine input type (folder vs .apk vs .xapk/.apks)
 * 2. Extract archive if needed
 * 3. Run ADB install-multiple (or single install)
 * 4. Clean up temp directory
 * 5. Expose reactive [state] and [logs] to the UI.
 */
class InstallerViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private val _state = MutableStateFlow<InstallerState>(InstallerState.Idle)
    val state: StateFlow<InstallerState> = _state.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private var currentJob: Job? = null

    fun handleDrop(path: String) {
        // Ignore drops while busy
        if (_state.value !is InstallerState.Idle &&
            _state.value !is InstallerState.Success &&
            _state.value !is InstallerState.Error
        ) return

        currentJob?.cancel()
        currentJob = scope.launch {
            _logs.value = emptyList()
            runInstallPipeline(path)
        }
    }

    // Hàm làm sạch đường dẫn, xử lý đặc sản "file://" của macOS
    private fun cleanMacOsPath(rawPath: String): String {
        var cleanPath = runCatching {
            URLDecoder.decode(rawPath.trim(), StandardCharsets.UTF_8.name())
        }.getOrDefault(rawPath.trim())

        if (cleanPath.startsWith("file://")) {
            cleanPath = cleanPath.removePrefix("file://")
        } else if (cleanPath.startsWith("file:")) {
            cleanPath = cleanPath.removePrefix("file:")
        }
        return cleanPath
    }

    private suspend fun runInstallPipeline(path: String) {
        val decodedPath = cleanMacOsPath(path)
        val inputFile = File(decodedPath)

        if (!inputFile.exists()) {
            appendLog("ERROR: Path does not exist: $decodedPath")
            _state.value = InstallerState.Error("File not found: ${inputFile.name}")
            return
        }

        var tempDir: File? = null
        val targetApkFiles = mutableListOf<File>()

        try {
            // ── Step 1: Phân loại File đầu vào ──────────────────────────────
            when {
                inputFile.isDirectory -> {
                    appendLog("📂 Dropped folder: ${inputFile.absolutePath}")
                    val found = inputFile.walkTopDown()
                        .filter { it.isFile && it.extension.lowercase() == "apk" }
                        .toList()
                    targetApkFiles.addAll(found)
                }

                inputFile.extension.lowercase() == "apk" -> {
                    appendLog("📄 Dropped single APK: ${inputFile.name}")
                    // Nếu là 1 file apk lẻ, không cần giải nén hay quét thư mục
                    targetApkFiles.add(inputFile)
                }

                inputFile.extension.lowercase() in listOf("xapk", "apks", "zip") -> {
                    _state.value = InstallerState.Extracting(inputFile.name)
                    appendLog("📦 Extracting archive: ${inputFile.name}")

                    val dir = withContext(Dispatchers.IO) {
                        extractXapk(inputFile) { entryName ->
                            appendLog("  ↳ $entryName")
                        }
                    }
                    tempDir = dir
                    appendLog("✅ Extraction complete → ${dir.absolutePath}")

                    // Quét tìm các file apk trong thư mục vừa giải nén
                    val found = dir.walkTopDown()
                        .filter { it.isFile && it.extension.lowercase() == "apk" }
                        .toList()
                    targetApkFiles.addAll(found)
                }

                else -> {
                    appendLog("ERROR: Unsupported file type: .${inputFile.extension}")
                    _state.value = InstallerState.Error("Unsupported file type: .${inputFile.extension}")
                    return
                }
            }

            // ── Step 2: Kiểm tra lại danh sách APK ────────────────────────────────
            if (targetApkFiles.isEmpty()) {
                appendLog("ERROR: No .apk files found to install.")
                _state.value = InstallerState.Error("No .apk files found")
                return
            }

            appendLog("\n🔍 Ready to install ${targetApkFiles.size} APK file(s):")
            targetApkFiles.forEach { appendLog("  • ${it.name}") }

            // ── Step 3: Install via ADB ──────────────────────────────────────────
            _state.value = InstallerState.Installing(targetApkFiles.size)
            appendLog("\n🚀 Starting ADB installation...")

            var success = false
            // Giữ nguyên hàm installApksViaAdb của bạn (hỗ trợ cả install lẻ hoặc install-multiple)
            installApksViaAdb(targetApkFiles).collect { line ->
                appendLog(line)
                if (line.startsWith("✅") || line.contains("Success", ignoreCase = true)) {
                    success = true
                }
            }

            _state.value = if (success) {
                InstallerState.Success("Installed ${targetApkFiles.size} APK(s) successfully")
            } else {
                InstallerState.Error("ADB installation failed – check logs above")
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            appendLog("EXCEPTION: ${e.message}")
            _state.value = InstallerState.Error(e.message ?: "Unknown error")
        } finally {
            // ── Step 4: Cleanup ──────────────────────────────────────────────────
            tempDir?.let { dir ->
                withContext(Dispatchers.IO) {
                    appendLog("\n🗑️  Cleaning up temp directory...")
                    deleteDirectory(dir)
                }
            }
        }
    }

    fun reset() {
        currentJob?.cancel()
        _state.value = InstallerState.Idle
        _logs.value = emptyList()
    }

    private fun appendLog(line: String) {
        _logs.update { it + line }
    }
}