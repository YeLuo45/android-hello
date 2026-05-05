package com.hello.android.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hello.android.ui.screens.CounterScreen
import com.hello.android.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Counter : Screen("counter")
}

@Composable
fun HelloNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCounter = {
                    navController.navigate(Screen.Counter.route)
                }
            )
        }
        composable(Screen.Counter.route) {
            CounterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
