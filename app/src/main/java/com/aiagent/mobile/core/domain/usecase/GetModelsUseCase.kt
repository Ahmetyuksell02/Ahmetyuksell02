package com.aiagent.mobile.core.domain.usecase

import com.aiagent.mobile.core.common.Resource
import com.aiagent.mobile.core.domain.model.AIModel
import com.aiagent.mobile.core.domain.repository.IModelRepository
import javax.inject.Inject

class GetModelsUseCase @Inject constructor(
    private val repository: IModelRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Resource<List<AIModel>> =
        if (forceRefresh) repository.refreshModels() else repository.getModels()
}
