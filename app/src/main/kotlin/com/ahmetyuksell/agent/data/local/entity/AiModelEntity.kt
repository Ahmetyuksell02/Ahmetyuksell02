package com.ahmetyuksell.agent.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_models")
data class AiModelEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    val provider: String,
    @ColumnInfo(name = "context_length") val contextLength: Int,
    @ColumnInfo(name = "supports_vision") val supportsVision: Boolean = false,
    @ColumnInfo(name = "supports_function_calling") val supportsFunctionCalling: Boolean = false,
    @ColumnInfo(name = "pricing_input_per_1k") val pricingInputPer1k: Double = 0.0,
    @ColumnInfo(name = "pricing_output_per_1k") val pricingOutputPer1k: Double = 0.0,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "display_order") val displayOrder: Int = 0
)
