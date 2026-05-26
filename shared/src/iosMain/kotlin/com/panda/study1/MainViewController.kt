package com.panda.study1

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    App(
        batteryManager = remember { BatteryManager() }
    )
}