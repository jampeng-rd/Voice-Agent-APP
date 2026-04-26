package com.jam.voiceagent.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jam.voiceagent.data.local.SessionStore
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.network.ApiClient
import com.jam.voiceagent.data.repository.AuthRepository
import com.jam.voiceagent.data.repository.ChatRepository
import com.jam.voiceagent.ui.screens.AssistantHomeScreen
import com.jam.voiceagent.ui.screens.auth.LoginScreen
import com.jam.voiceagent.ui.screens.auth.RegisterScreen
import com.jam.voiceagent.ui.screens.chat.ChatPlaceholderScreen
import kotlinx.coroutines.launch

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val tokenStore = remember(context) { TokenStore(context) }
    val sessionStore = remember(context) { SessionStore(context) }
    val authRepository = remember(context) {
        AuthRepository(
            authApi = ApiClient.authApi,
            tokenStore = tokenStore,
            sessionStore = sessionStore
        )
    }
    val chatRepository = remember(context) {
        ChatRepository(
            chatApi = ApiClient.chatApi,
            tokenStore = tokenStore,
            sessionStore = sessionStore
        )
    }

    var route by rememberSaveable { mutableStateOf(AppRoute.Home) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var isChatBusy by remember { mutableStateOf(false) }
    var latestAssistantReply by remember { mutableStateOf("") }
    var guestStartupErrorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authRepository) {
        isLoggedIn = authRepository.hasRegisteredToken()
        if (!isLoggedIn) {
            val result = authRepository.ensureGuestToken()
            guestStartupErrorMessage = if (result.isSuccess) {
                ""
            } else {
                result.errorMessage ?: "目前無法建立訪客連線，請稍後再試。"
            }
        } else {
            guestStartupErrorMessage = ""
        }
    }

    val goHome: () -> Unit = {
        if (!isChatBusy) {
            route = AppRoute.Home
        }
    }
    val goChat: () -> Unit = {
        if (!isChatBusy) {
            route = if (isLoggedIn) AppRoute.Chat else AppRoute.Login
        }
    }
    val userAction: () -> Unit = {
        if (!isChatBusy) {
            if (isLoggedIn) {
                scope.launch {
                    val logoutResult = authRepository.logoutRegisteredAndCreateGuest()
                    chatRepository.clearSession()
                    isLoggedIn = false
                    guestStartupErrorMessage = if (logoutResult.isSuccess) {
                        ""
                    } else {
                        logoutResult.errorMessage ?: "目前無法建立訪客連線，請稍後再試。"
                    }
                    route = AppRoute.Home
                }
            } else {
                route = AppRoute.Login
            }
        }
    }
    HomeNavigationBarImmersiveEffect(enabled = route == AppRoute.Home)

    when (route) {
        AppRoute.Home -> {
            AssistantHomeScreen(
                isLoggedIn = isLoggedIn,
                onNavigateHome = goHome,
                onNavigateChat = goChat,
                onUserAction = userAction,
                chatRepository = chatRepository,
                isChatBusy = isChatBusy,
                latestAssistantReply = latestAssistantReply,
                startupErrorMessage = guestStartupErrorMessage,
                onChatBusyChange = { isChatBusy = it },
                onAssistantReplyChange = { latestAssistantReply = it },
                onStartupErrorConsumed = {
                    guestStartupErrorMessage = ""
                }
            )
        }

        AppRoute.Login -> {
            LoginScreen(
                isLoggedIn = isLoggedIn,
                onHomeClick = goHome,
                onChatClick = goChat,
                onUserClick = userAction,
                authRepository = authRepository,
                onGoRegister = { route = AppRoute.Register },
                onLoginSuccess = {
                    isLoggedIn = true
                    guestStartupErrorMessage = ""
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
                authRepository = authRepository,
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
                    authRepository = authRepository,
                    onGoRegister = { route = AppRoute.Register },
                    onLoginSuccess = {
                        isLoggedIn = true
                        guestStartupErrorMessage = ""
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
