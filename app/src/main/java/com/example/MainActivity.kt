package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.SessionLog
import com.example.model.ProgressionEngine
import com.example.model.Protocol
import com.example.ui.ActiveSessionScreen
import com.example.ui.MainAppContainer
import com.example.ui.theme.EyeRestTheme
import com.example.ui.theme.ObsidianBg
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val sessionLogDao = database.sessionLogDao()

        setContent {
            EyeRestTheme {
                val coroutineScope = rememberCoroutineScope()
                var activeProtocol by remember { mutableStateOf<Protocol?>(null) }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianBg),
                    color = ObsidianBg
                ) {
                    AnimatedContent(
                        targetState = activeProtocol,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { protocol ->
                        if (protocol != null) {
                            ActiveSessionScreen(
                                protocol = protocol,
                                onClose = { activeProtocol = null },
                                onSessionCompleted = { protocolId, duration ->
                                    coroutineScope.launch {
                                        sessionLogDao.insertLog(
                                            SessionLog(
                                                protocolId = protocolId,
                                                protocolTitle = protocol.title,
                                                durationSeconds = duration
                                            )
                                        )
                                        ProgressionEngine.recordDrillCompletion(
                                            categoryId = "TRACKING",
                                            accuracy = 0.90f,
                                            sessionLogDao = sessionLogDao
                                        )
                                    }
                                    activeProtocol = null
                                }
                            )
                        } else {
                            MainAppContainer(
                                sessionLogDao = sessionLogDao,
                                onStartProtocol = { selected ->
                                    activeProtocol = selected
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
