package com.hello.android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hello.android.ui.auth.LoginScreen
import com.hello.android.ui.auth.RegisterScreen
import com.hello.android.ui.screens.CounterScreen
import com.hello.android.ui.screens.DiscoverScreen
import com.hello.android.ui.screens.HomeScreen
import com.hello.android.ui.screens.SettingsScreen
import com.hello.android.ui.viewmodel.AuthViewModel

// Auth Routes
object AuthRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
}

// Main Routes
object MainRoutes {
    const val HOME = "home"
    const val DISCOVER = "discover"
    const val SETTINGS = "settings"
    const val COUNTER = "counter"
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = MainRoutes.HOME,
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = MainRoutes.DISCOVER,
        title = "发现",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    ),
    BottomNavItem(
        route = MainRoutes.SETTINGS,
        title = "设置",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@Composable
fun HelloNavHost(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val rootNavController = rememberNavController()

    // Wait for auto-login check before deciding which screen to show
    if (!authState.isAutoLoginChecked) {
        // Show loading or splash screen while checking auth
        return
    }

    if (authState.isLoggedIn) {
        // User is logged in, show main app with bottom nav
        MainScaffold(
            onLogout = {
                rootNavController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    } else {
        // User is not logged in, show auth flow
        AuthNavHost(
            onLoginSuccess = {
                // Navigation will be handled by state change
            }
        )
    }
}

@Composable
fun AuthNavHost(
    onLoginSuccess: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.LOGIN
    ) {
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER)
                },
                onLoginSuccess = onLoginSuccess
            )
        }
        composable(AuthRoutes.REGISTER) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = onLoginSuccess
            )
        }
    }
}

@Composable
fun MainScaffold(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MainRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MainRoutes.HOME) {
                HomeScreen(
                    onNavigateToCounter = {
                        navController.navigate(MainRoutes.COUNTER)
                    }
                )
            }
            composable(MainRoutes.DISCOVER) {
                DiscoverScreen()
            }
            composable(MainRoutes.SETTINGS) {
                SettingsScreen(onLogout = onLogout)
            }
            composable(MainRoutes.COUNTER) {
                CounterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
