package com.aiagent.mobile.core.domain.repository

import com.aiagent.mobile.core.common.Resource
import com.aiagent.mobile.core.domain.model.AIModel

interface IModelRepository {
    /** Returns cached models or fetches from OpenRouter if cache is empty/stale. */
    suspend fun getModels(): Resource<List<AIModel>>

    /** Force-fetches fresh models from the OpenRouter API. */
    suspend fun refreshModels(): Resource<List<AIModel>>
}
