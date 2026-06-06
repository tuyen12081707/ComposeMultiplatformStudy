package com.panda.study1

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "XAPK Installer",
        state = WindowState(size = DpSize(680.dp, 540.dp)),
        resizable = true
    ) {
        XapkInstallerApp()
    }
}