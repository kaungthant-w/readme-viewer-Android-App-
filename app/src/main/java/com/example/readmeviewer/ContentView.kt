package com.example.readmeviewer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.readmeviewer.ui.screens.HomeScreen
import com.example.readmeviewer.ui.screens.RecentScreen
import com.example.readmeviewer.ui.screens.FullScreenScreen
import com.example.readmeviewer.ui.theme.ReadmeViewerTheme
import com.example.readmeviewer.viewmodel.MainViewModel

import androidx.compose.material.icons.filled.Info

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Recent : Screen("recent", "Recent", Icons.Default.History)
    object About : Screen("about", "About", Icons.Default.Info)
    object FullScreen : Screen("fullscreen", "Full Screen", Icons.Default.Home)
    object Feedback : Screen("feedback", "Feedback", Icons.Default.Info)
}

@Composable
fun ContentView() {
    val context = LocalContext.current
    val viewModel = remember { com.example.readmeviewer.viewmodel.MainViewModel(context) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    ReadmeViewerTheme(darkTheme = isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val items = listOf(Screen.Home, Screen.Recent, Screen.About)

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isFullScreen = currentDestination?.route == Screen.FullScreen.route

            Scaffold(
                bottomBar = {
                    if (!isFullScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ) {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground) },
                                    label = { Text(screen.title, color = MaterialTheme.colorScheme.onBackground) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        viewModel.selectTab(items.indexOf(screen))
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToFullScreen = {
                                navController.navigate(Screen.FullScreen.route)
                            },
                            isDarkMode = isDarkMode,
                            toggleDarkMode = { viewModel.toggleDarkMode() }
                        )
                    }

                    composable(Screen.Recent.route) {
                        RecentScreen(
                            viewModel = viewModel,
                            onFileSelected = { file ->
                                viewModel.selectFile(file.uri)
                                navController.navigate(Screen.Home.route)
                            }
                        )
                    }

                    composable(Screen.About.route) {
                        com.example.readmeviewer.ui.screens.AboutScreen(
                            onFeedbackClick = {
                                navController.navigate(Screen.Feedback.route)
                            }
                        )
                    }

                    composable(Screen.Feedback.route) {
                        com.example.readmeviewer.ui.screens.FeedbackScreen(
                            onSubmit = { feedback ->
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.FullScreen.route) {
                        FullScreenScreen(
                            viewModel = viewModel,
                            onExitFullScreen = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}