package com.jam.voiceagent.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputBar(
    modifier: Modifier = Modifier,
    borderColor: Color,
    textColor: Color,
    placeholderColor: Color,
    iconColor: Color
) {
    val text = remember { mutableStateOf("") }
    val compactTextStyle = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp
    )

    OutlinedTextField(
        value = text.value,
        onValueChange = { text.value = it },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .height(52.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        placeholder = {
            Text(
                text = "輸入文字",
                color = placeholderColor,
                style = compactTextStyle
            )
        },
        singleLine = true,
        textStyle = compactTextStyle,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(
                onClick = {},
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.NorthEast,
                    contentDescription = "送出輔助文字",
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}
