package com.panda.study1.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Footer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A10))
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            thickness = 1.dp
        )
        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text  = "🐻‍❄️",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "Ice Bear Studio",
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text      = "© 2025 Ice Bear Studio. All rights reserved.",
            style     = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.outline
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text      = "Built with ❤ using Compose Multiplatform",
            style     = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center
        )
    }
}
