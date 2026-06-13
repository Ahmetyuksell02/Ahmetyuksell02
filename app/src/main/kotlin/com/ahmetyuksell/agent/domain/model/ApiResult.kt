package com.ahmetyuksell.agent.domain.model

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class NetworkError(val message: String) : ApiResult<Nothing>()
    data class RateLimitError(val retryAfterMs: Long) : ApiResult<Nothing>()
    data object AuthError : ApiResult<Nothing>()
    data class ApiError(val httpCode: Int, val message: String) : ApiResult<Nothing>()
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.NetworkError -> this
    is ApiResult.RateLimitError -> this
    is ApiResult.AuthError -> this
    is ApiResult.ApiError -> this
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (String) -> Unit): ApiResult<T> {
    when (this) {
        is ApiResult.NetworkError -> action(message)
        is ApiResult.ApiError -> action("HTTP $httpCode: $message")
        is ApiResult.AuthError -> action("Authentication failed — check your API key")
        is ApiResult.RateLimitError -> action("Rate limited — retry after ${retryAfterMs}ms")
        else -> Unit
    }
    return this
}
