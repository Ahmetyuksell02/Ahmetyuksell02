package com.ahmetyuksell.agent.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ahmetyuksell.agent.presentation.screens.agents.AgentScreen
import com.ahmetyuksell.agent.presentation.screens.chat.ChatScreen
import com.ahmetyuksell.agent.presentation.screens.history.HistoryScreen
import com.ahmetyuksell.agent.presentation.screens.settings.SettingsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.History.route
    ) {
        composable(Screen.History.route) {
            HistoryScreen(
                onConversationClick = { id ->
                    navController.navigate(Screen.Chat.createRoute(id))
                },
                onNewConversation = { id ->
                    navController.navigate(Screen.Chat.createRoute(id))
                },
                onAgentsClick = { navController.navigate(Screen.Agents.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Chat.ROUTE,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            ChatScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Agents.route) {
            AgentScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
