package com.jam.voiceagent.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jam.voiceagent.ui.avatar.AvatarState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmotionButtons(
    selected: AvatarState,
    states: List<AvatarState>,
    onSelect: (AvatarState) -> Unit,
    selectedContainer: Color,
    selectedLabel: Color,
    container: Color,
    label: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (compact) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            states.forEach { state ->
                val isSelected = selected == state
                AssistChip(
                    onClick = { onSelect(state) },
                    label = {
                        Text(
                            text = state.displayName,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) selectedContainer else container,
                        labelColor = if (isSelected) selectedLabel else label
                    )
                )
            }
        }
        return
    }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5
    ) {
        states.forEach { state ->
            val isSelected = selected == state
            AssistChip(
                onClick = { onSelect(state) },
                label = {
                    Text(
                        text = state.displayName,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(50),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) selectedContainer else container,
                    labelColor = if (isSelected) selectedLabel else label
                )
            )
        }
    }
}
