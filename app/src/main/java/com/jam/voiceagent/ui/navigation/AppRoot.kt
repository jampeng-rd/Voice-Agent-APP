package com.jam.voiceagent.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    HomeNavigationBarImmersiveEffect(enabled = route == AppRoute.Home)

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

@Composable
private fun HomeNavigationBarImmersiveEffect(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled, view) {
        val activity = view.context.findActivity()
        val window = activity?.window
        val controller =
            if (window != null) WindowCompat.getInsetsController(window, view) else null

        if (enabled) {
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller?.show(WindowInsetsCompat.Type.navigationBars())
        }

        onDispose {
            if (enabled) {
                controller?.show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
