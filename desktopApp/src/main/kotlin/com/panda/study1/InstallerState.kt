package com.panda.study1

/**
 * Represents the current state of the XAPK installation process.
 */
sealed class InstallerState {
    object Idle : InstallerState()
    data class Extracting(val fileName: String) : InstallerState()
    data class Installing(val apkCount: Int) : InstallerState()
    data class Success(val message: String) : InstallerState()
    data class Error(val message: String) : InstallerState()
}
