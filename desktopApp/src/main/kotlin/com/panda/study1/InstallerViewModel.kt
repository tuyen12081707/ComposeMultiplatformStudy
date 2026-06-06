package com.panda.study1

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * Coordinates the full install pipeline:
 *   1. Determine input type (folder vs .xapk/.apks)
 *   2. Extract archive if needed
 *   3. Run ADB install-multiple
 *   4. Clean up temp directory
 *   5. Expose reactive [state] and [logs] to the UI.
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

    private suspend fun runInstallPipeline(path: String) {
        val inputFile = File(path.trim())

        if (!inputFile.exists()) {
            appendLog("ERROR: Path does not exist: $path")
            _state.value = InstallerState.Error("File not found: ${inputFile.name}")
            return
        }

        var tempDir: File? = null

        try {
            // ── Step 1: Determine source directory ──────────────────────────────
            val sourceDir: File = when {
                inputFile.isDirectory -> {
                    appendLog("📂 Dropped folder: ${inputFile.absolutePath}")
                    inputFile
                }
                inputFile.extension.lowercase() in listOf("xapk", "apks") -> {
                    _state.value = InstallerState.Extracting(inputFile.name)
                    appendLog("📦 Extracting archive: ${inputFile.name}")

                    val dir = withContext(Dispatchers.IO) {
                        extractXapk(inputFile) { entryName ->
                            appendLog("  ↳ $entryName")
                        }
                    }
                    tempDir = dir
                    appendLog("✅ Extraction complete → ${dir.absolutePath}")
                    dir
                }
                else -> {
                    appendLog("ERROR: Unsupported file type: .${inputFile.extension}")
                    _state.value = InstallerState.Error("Unsupported file type: .${inputFile.extension}")
                    return
                }
            }

            // ── Step 2: Collect .apk files ──────────────────────────────────────
            val apkFiles = sourceDir.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "apk" }
                .toList()

            if (apkFiles.isEmpty()) {
                appendLog("ERROR: No .apk files found in ${sourceDir.absolutePath}")
                _state.value = InstallerState.Error("No .apk files found")
                return
            }

            appendLog("\n🔍 Found ${apkFiles.size} APK file(s):")
            apkFiles.forEach { appendLog("  • ${it.name}") }

            // ── Step 3: Install via ADB ──────────────────────────────────────────
            _state.value = InstallerState.Installing(apkFiles.size)
            appendLog("\n🚀 Starting ADB installation...")

            var success = false
            installApksViaAdb(apkFiles).collect { line ->
                appendLog(line)
                if (line.startsWith("✅")) success = true
            }

            _state.value = if (success) {
                InstallerState.Success("Installed ${apkFiles.size} APK(s) successfully")
            } else {
                InstallerState.Error("ADB installation failed – check logs above")
            }

        } catch (e: CancellationException) {
            throw e  // always re-throw cancellation
        } catch (e: Exception) {
            appendLog("EXCEPTION: ${e.message}")
            _state.value = InstallerState.Error(e.message ?: "Unknown error")
        } finally {
            // ── Step 4: Cleanup ──────────────────────────────────────────────────
            tempDir?.let { dir ->
                withContext(Dispatchers.IO) {
                    appendLog("\n🗑️  Cleaning up temp directory: ${dir.absolutePath}")
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
