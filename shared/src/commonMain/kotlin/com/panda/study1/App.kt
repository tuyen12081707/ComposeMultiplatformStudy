package com.panda.study1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import composemultiplatformstudy.shared.generated.resources.Res
import composemultiplatformstudy.shared.generated.resources.compose_multiplatform

@Composable
fun App(batteryManager : BatteryManager) {
    MaterialTheme {
       Box(modifier = Modifier.fillMaxSize().background(Color.White),
           contentAlignment = Alignment.Center
       ){
           Text("Hello word battery : ${batteryManager.getBatteryLevel()}")
       }
    }
}