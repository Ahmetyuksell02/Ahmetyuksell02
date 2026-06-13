package com.ahmetyuksell.agent.util

object TokenEstimator {
    fun estimate(text: String): Int = (text.length / 4).coerceAtLeast(1)

    fun estimateMessages(messages: List<String>): Int =
        messages.sumOf { estimate(it) } + messages.size * 4
}
