package com.aiagent.mobile.navigation

/**
 * Sealed hierarchy of all navigation destinations in the app.
 * Each object/class holds the route string and optional argument key constants.
 */
sealed class Screen(val route: String) {

    // ─── Bottom Navigation Tabs ───────────────────────────────────────────────
    data object Home : Screen("home")
    data object Agents : Screen("agents")
    data object Settings : Screen("settings")

    // ─── Chat ─────────────────────────────────────────────────────────────────
    data object Chat : Screen("chat/{$ARG_CONVERSATION_ID}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
        const val ARG_CONVERSATION_ID = "conversationId"
    }

    data object NewChat : Screen("new_chat")

    // ─── Agents ───────────────────────────────────────────────────────────────
    data object AgentDetail : Screen("agent_detail/{$ARG_TASK_ID}") {
        fun createRoute(taskId: String) = "agent_detail/$taskId"
        const val ARG_TASK_ID = "taskId"
    }

    data object CreateAgent : Screen("create_agent")
}
