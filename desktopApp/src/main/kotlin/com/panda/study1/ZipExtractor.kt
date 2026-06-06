package com.panda.study1

import java.io.File
import java.util.zip.ZipFile

/**
 * Extracts a ZIP-based archive (.xapk / .apks) into a unique temporary directory.
 * Uses ZipFile instead of ZipInputStream to avoid the "only DEFLATED entries can have EXT descriptor" error.
 */
fun extractXapk(sourceFile: File, onProgress: (entryName: String) -> Unit = {}): File {
    val tmpDir = File(System.getProperty("java.io.tmpdir"), "xapk_installer_${System.currentTimeMillis()}")
    tmpDir.mkdirs()

    // Sử dụng ZipFile để đọc cấu trúc chuẩn xác thay vì ZipInputStream
    ZipFile(sourceFile).use { zip ->
        val entries = zip.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val outFile = File(tmpDir, entry.name)

            // Bảo mật: Ngăn chặn lỗi Zip Slip (kẻ xấu nhét đường dẫn ../ để ghi đè file hệ thống)
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