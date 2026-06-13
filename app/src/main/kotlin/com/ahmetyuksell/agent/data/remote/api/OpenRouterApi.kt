package com.ahmetyuksell.agent.data.remote.api

import com.ahmetyuksell.agent.data.remote.dto.ChatCompletionRequestDto
import com.ahmetyuksell.agent.data.remote.dto.ChatCompletionResponseDto
import com.ahmetyuksell.agent.data.remote.dto.ModelListResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OpenRouterApi {

    @POST("chat/completions")
    suspend fun chatCompletion(
        @Body request: ChatCompletionRequestDto
    ): ChatCompletionResponseDto

    @GET("models")
    suspend fun listModels(): ModelListResponseDto
}
