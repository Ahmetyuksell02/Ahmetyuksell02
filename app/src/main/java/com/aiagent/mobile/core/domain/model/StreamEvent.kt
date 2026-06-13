package com.aiagent.mobile.core.domain.model

/** Events emitted during a streaming AI response. */
sealed class StreamEvent {
    /** User message has been persisted to the database. */
    data class UserMessageSaved(val message: Message) : StreamEvent()

    /** A partial content chunk arrived from the API stream. */
    data class Chunk(val content: String) : StreamEvent()

    /** Stream finished; full AI message has been persisted. */
    data class Complete(val message: Message) : StreamEvent()

    /** A recoverable error occurred (network, auth, parsing). */
    data class Error(val message: String) : StreamEvent()
}
