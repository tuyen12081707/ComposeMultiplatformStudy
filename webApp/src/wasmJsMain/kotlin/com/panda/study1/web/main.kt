package com.panda.study1.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.getElementById("root") ?: document.body!!
    ComposeViewport(body) {
        App()
    }
}
