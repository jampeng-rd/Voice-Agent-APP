package com.jam.voiceagent.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VoicePrimaryButton(
    onClick: () -> Unit,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier.size(74.dp)) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "語音主按鈕",
                tint = iconColor,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
