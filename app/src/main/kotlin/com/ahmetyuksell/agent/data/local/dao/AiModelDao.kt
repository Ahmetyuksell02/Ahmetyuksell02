package com.ahmetyuksell.agent.data.local.dao

import androidx.room.*
import com.ahmetyuksell.agent.data.local.entity.AiModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiModelDao {

    @Query("SELECT * FROM ai_models WHERE is_enabled = 1 ORDER BY display_order ASC, display_name ASC")
    fun getEnabledModels(): Flow<List<AiModelEntity>>

    @Query("SELECT * FROM ai_models ORDER BY display_order ASC, display_name ASC")
    fun getAllModels(): Flow<List<AiModelEntity>>

    @Query("SELECT * FROM ai_models WHERE id = :id LIMIT 1")
    suspend fun getModel(id: String): AiModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModel(entity: AiModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModels(entities: List<AiModelEntity>)

    @Query("UPDATE ai_models SET is_enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE ai_models SET display_order = :order WHERE id = :id")
    suspend fun setOrder(id: String, order: Int)
}
