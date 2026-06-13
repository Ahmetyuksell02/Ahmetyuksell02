package com.ahmetyuksell.agent.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ahmetyuksell.agent.domain.model.AiModel

@Composable
fun ModelSelector(
    selectedModel: AiModel?,
    models: List<AiModel>,
    onModelSelected: (AiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = selectedModel?.displayName ?: "Select Model",
                style = MaterialTheme.typography.labelMedium
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = model.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onModelSelected(model)
                        expanded = false
                    },
                    leadingIcon = {
                        if (model.id == selectedModel?.id) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Selected"
                            )
                        }
                    }
                )
            }
        }
    }
}
