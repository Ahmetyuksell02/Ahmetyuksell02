package com.ahmetyuksell.agent.presentation.navigation

sealed class Screen(val route: String) {
    data object History : Screen("history")
    data class Chat(val conversationId: String) : Screen("chat/{conversationId}") {
        companion object {
            const val ROUTE = "chat/{conversationId}"
            fun createRoute(conversationId: String) = "chat/$conversationId"
        }
    }
    data object Agents : Screen("agents")
    data object Settings : Screen("settings")
}
