package com.ahmetyuksell.agent.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showApiKey by remember { mutableStateOf(false) }

    state.syncMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            viewModel.dismissSyncMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = {
            state.syncMessage?.let {
                Snackbar { Text(it) }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("API Configuration", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (state.hasApiKey) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("OpenRouter API Key: Set ✓", style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = viewModel::clearApiKey) {
                                    Text("Clear", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.apiKey,
                            onValueChange = viewModel::onApiKeyChanged,
                            label = { Text(if (state.hasApiKey) "Update API Key" else "Enter API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = viewModel::saveApiKey,
                            enabled = state.apiKey.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }

            item {
                Text("Models", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Available models: ${state.availableModels.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = viewModel::syncModels,
                                enabled = !state.isSyncing
                            ) {
                                if (state.isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Sync")
                                }
                            }
                        }

                        if (state.availableModels.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Default model:", style = MaterialTheme.typography.labelMedium)
                            state.availableModels.take(10).forEach { model ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(model.displayName, style = MaterialTheme.typography.bodySmall)
                                    RadioButton(
                                        selected = state.defaultModelId == model.id,
                                        onClick = { viewModel.setDefaultModel(model.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Observability", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(onClick = viewModel::refreshMetrics, modifier = Modifier.fillMaxWidth()) {
                            Text("Show Metrics")
                        }
                        if (state.metricsSummary.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.metricsSummary,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
