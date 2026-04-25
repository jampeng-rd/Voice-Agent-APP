package com.jam.voiceagent.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jam.voiceagent.data.repository.AuthRepository
import com.jam.voiceagent.ui.components.TopRightQuickMenu
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    isLoggedIn: Boolean,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onUserClick: () -> Unit,
    authRepository: AuthRepository,
    onGoRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorText by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        TopRightQuickMenu(
            isLoggedIn = isLoggedIn,
            onHomeClick = onHomeClick,
            onChatClick = onChatClick,
            onUserClick = onUserClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "登入",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "請使用已註冊帳號登入。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                enabled = !isLoading
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密碼") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isLoading
            )

            if (errorText.isNotBlank()) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (isLoading) return@Button
                    if (email.isBlank() || password.isBlank()) {
                        errorText = "請輸入 Email 與密碼"
                        return@Button
                    }

                    isLoading = true
                    errorText = ""
                    scope.launch {
                        val result = authRepository.login(
                            email = email.trim(),
                            password = password
                        )
                        isLoading = false
                        if (result.isSuccess) {
                            onLoginSuccess()
                        } else {
                            errorText = result.errorMessage ?: "登入失敗，請確認帳號密碼。"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically)
                    )
                } else {
                    Text("登入")
                }
            }

            OutlinedButton(
                onClick = onGoRegister,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("前往註冊")
            }
        }
    }
}
