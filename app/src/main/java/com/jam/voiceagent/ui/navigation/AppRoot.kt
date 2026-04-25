package com.jam.voiceagent.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.jam.voiceagent.ui.screens.AssistantHomeScreen
import com.jam.voiceagent.ui.screens.auth.LoginScreen
import com.jam.voiceagent.ui.screens.auth.RegisterScreen
import com.jam.voiceagent.ui.screens.chat.ChatPlaceholderScreen

@Composable
fun AppRoot() {
    var route by rememberSaveable { mutableStateOf(AppRoute.Home) }
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    val goHome: () -> Unit = { route = AppRoute.Home }
    val goChat: () -> Unit = {
        route = if (isLoggedIn) AppRoute.Chat else AppRoute.Login
    }
    val userAction: () -> Unit = {
        if (isLoggedIn) {
            isLoggedIn = false
            route = AppRoute.Home
        } else {
            route = AppRoute.Login
        }
    }

    when (route) {
        AppRoute.Home -> {
            AssistantHomeScreen(
                isLoggedIn = isLoggedIn,
                onNavigateHome = goHome,
                onNavigateChat = goChat,
                onUserAction = userAction
            )
        }

        AppRoute.Login -> {
            LoginScreen(
                isLoggedIn = isLoggedIn,
                onHomeClick = goHome,
                onChatClick = goChat,
                onUserClick = userAction,
                onGoRegister = { route = AppRoute.Register },
                onLoginSuccess = {
                    isLoggedIn = true
                    route = AppRoute.Chat
                }
            )
        }

        AppRoute.Register -> {
            RegisterScreen(
                isLoggedIn = isLoggedIn,
                onHomeClick = goHome,
                onChatClick = goChat,
                onUserClick = userAction,
                onBackLogin = { route = AppRoute.Login },
                onRegisterSuccess = { route = AppRoute.Login }
            )
        }

        AppRoute.Chat -> {
            if (!isLoggedIn) {
                LoginScreen(
                    isLoggedIn = isLoggedIn,
                    onHomeClick = goHome,
                    onChatClick = goChat,
                    onUserClick = userAction,
                    onGoRegister = { route = AppRoute.Register },
                    onLoginSuccess = {
                        isLoggedIn = true
                        route = AppRoute.Chat
                    }
                )
            } else {
                ChatPlaceholderScreen(
                    isLoggedIn = isLoggedIn,
                    onHomeClick = goHome,
                    onChatClick = goChat,
                    onUserClick = userAction
                )
            }
        }
    }
}
