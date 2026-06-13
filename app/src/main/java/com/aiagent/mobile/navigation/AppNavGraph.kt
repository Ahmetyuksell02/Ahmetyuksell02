package com.aiagent.mobile.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aiagent.mobile.feature.agents.AgentDetailScreen
import com.aiagent.mobile.feature.agents.AgentsScreen
import com.aiagent.mobile.feature.agents.CreateAgentScreen
import com.aiagent.mobile.feature.chat.ChatScreen
import com.aiagent.mobile.feature.home.HomeScreen
import com.aiagent.mobile.feature.settings.SettingsScreen

private data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Chats", Icons.Filled.Chat, Icons.Outlined.Chat),
    BottomNavItem(Screen.Agents, "Agents", Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

private val bottomNavRoutes = setOf(Screen.Home.route, Screen.Agents.route, Screen.Settings.route)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280)
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = { navController.navigate(Screen.Chat.createRoute(it)) },
                onNavigateToNewChat = { navController.navigate(Screen.NewChat.route) }
            )
        }

        composable(Screen.Agents.route) {
            AgentsScreen(
                onNavigateToAgentDetail = { navController.navigate(Screen.AgentDetail.createRoute(it)) },
                onNavigateToCreateAgent = { navController.navigate(Screen.CreateAgent.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument(Screen.Chat.ARG_CONVERSATION_ID) { type = NavType.StringType }
            )
        ) {
            ChatScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.NewChat.route) {
            ChatScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AgentDetail.route,
            arguments = listOf(
                navArgument(Screen.AgentDetail.ARG_TASK_ID) { type = NavType.StringType }
            )
        ) {
            AgentDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.CreateAgent.route) {
            CreateAgentScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
