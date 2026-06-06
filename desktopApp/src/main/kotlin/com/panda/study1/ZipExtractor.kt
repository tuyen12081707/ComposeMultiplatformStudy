package com.panda.study1

import java.io.File
import java.util.zip.ZipInputStream

/**
 * Extracts a ZIP-based archive (.xapk / .apks) into a unique temporary directory.
 *
 * @param sourceFile  The .xapk or .apks file to extract.
 * @param onProgress  Optional callback invoked with each entry name as it is extracted.
 * @return            The [File] pointing to the created temp directory containing the extracted contents.
 * @throws Exception  If extraction fails for any reason.
 */
fun extractXapk(sourceFile: File, onProgress: (entryName: String) -> Unit = {}): File {
    val tmpDir = File(System.getProperty("java.io.tmpdir"), "xapk_installer_${System.currentTimeMillis()}")
    tmpDir.mkdirs()

    ZipInputStream(sourceFile.inputStream().buffered()).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            val outFile = File(tmpDir, entry.name)
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                onProgress(entry.name)
                outFile.outputStream().buffered().use { out ->
                    zis.copyTo(out)
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
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
