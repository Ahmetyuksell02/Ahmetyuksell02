package com.ahmetyuksell.agent.data.local.dao

import androidx.room.*
import com.ahmetyuksell.agent.data.local.entity.AgentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {

    @Query("SELECT * FROM agents WHERE is_active = 1 ORDER BY name ASC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE id = :id LIMIT 1")
    suspend fun getAgent(id: String): AgentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(entity: AgentEntity)

    @Update
    suspend fun updateAgent(entity: AgentEntity)

    @Query("UPDATE agents SET is_active = 0 WHERE id = :id")
    suspend fun deleteAgent(id: String)
}
