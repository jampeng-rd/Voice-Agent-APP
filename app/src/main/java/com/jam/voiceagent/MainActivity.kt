package com.jam.voiceagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jam.voiceagent.ui.screens.AssistantHomeScreen
import com.jam.voiceagent.ui.theme.VoiceAgentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceAgentTheme {
                AssistantHomeScreen()
            }
        }
    }
}
