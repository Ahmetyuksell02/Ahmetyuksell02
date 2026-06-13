package com.aiagent.mobile.core.data.remote.api

import com.aiagent.mobile.core.data.remote.dto.ChatRequestDto
import com.aiagent.mobile.core.data.remote.dto.ChatResponseDto
import com.aiagent.mobile.core.data.remote.dto.ModelsResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenRouterApi {

    @GET("models")
    suspend fun getModels(): ModelsResponseDto

    /** Non-streaming completion — use for agent tasks and background work. */
    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: ChatRequestDto): ChatResponseDto

    /** Streaming completion via SSE — use for real-time chat UI. */
    @Streaming
    @POST("chat/completions")
    suspend fun chatCompletionStream(@Body request: ChatRequestDto): Response<ResponseBody>
}
