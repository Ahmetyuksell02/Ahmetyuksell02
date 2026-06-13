package com.ahmetyuksell.agent.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ToolDto(
    val type: String = "function",
    val function: FunctionDefinitionDto
)

@Serializable
data class FunctionDefinitionDto(
    val name: String,
    val description: String,
    val parameters: JsonObject
)
