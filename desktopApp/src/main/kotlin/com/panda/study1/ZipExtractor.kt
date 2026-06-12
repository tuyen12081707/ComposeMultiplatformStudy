package com.panda.study1

import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File

/**
 * Extracts a ZIP-based archive (.xapk / .apks) into a unique temporary directory.
 * Uses Apache Commons Compress to bypass Android's invalid "DEFLATED EXT descriptor" zipalign quirks.
 */
fun extractXapk(sourceFile: File, onProgress: (entryName: String) -> Unit = {}): File {
    val tmpDir = File(System.getProperty("java.io.tmpdir"), "xapk_installer_${System.currentTimeMillis()}")
    tmpDir.mkdirs()

    // Khởi tạo ZipFile của Apache (phiên bản 1.26+ xài builder)
    ZipFile.builder().setFile(sourceFile).get().use { zip ->
        val entries = zip.entries
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val outFile = File(tmpDir, entry.name)

            // Bảo mật: Ngăn chặn lỗi Zip Slip
            if (!outFile.canonicalPath.startsWith(tmpDir.canonicalPath)) {
                continue
            }

            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                onProgress(entry.name)

                zip.getInputStream(entry).use { input ->
                    outFile.outputStream().buffered().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    return tmpDir
}

/**
 * Recursively deletes a directory and all its contents.
 */
fun deleteDirectory(dir: File) {
    if (dir.isDirectory) {
        dir.listFiles()?.forEach { deleteDirectory(it) }
    }
    dir.delete()
}