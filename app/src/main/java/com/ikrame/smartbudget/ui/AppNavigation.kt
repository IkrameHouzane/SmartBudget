package com.ikrame.smartbudget.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ikrame.smartbudget.ui.screen.ExpenseListScreen
import com.ikrame.smartbudget.ui.screen.SettingsScreen
import com.ikrame.smartbudget.ui.screen.StatsScreen
import com.ikrame.smartbudget.viewmodel.ExpenseViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Expenses : Screen("expenses", "Dépenses", Icons.Default.Home)
    object Stats    : Screen("stats",    "Stats",    Icons.Default.Star)
    object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)
}

@Composable
fun AppNavigation(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Expenses, Screen.Stats, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon  = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Expenses.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Expenses.route) { ExpenseListScreen(viewModel) }
            composable(Screen.Stats.route)    { StatsScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}