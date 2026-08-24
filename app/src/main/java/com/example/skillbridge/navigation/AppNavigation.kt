package com.example.skillbridge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.skillbridge.screens.SplashScreen
import com.example.skillbridge.screens.WelcomeScreen
import com.example.skillbridge.screens.ExploreScreen
import com.example.skillbridge.screens.JobDetailsScreen
import com.example.skillbridge.screens.ApplyProjectScreen
import com.example.skillbridge.screens.ProfileScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("welcome") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("welcome") {
            WelcomeScreen(onGetStartedClick = {
                navController.navigate("explore")
            })
        }

        composable("explore") {
            ExploreScreen(
                onJobClick = { job ->
                    navController.navigate("job_details")
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }

        composable("job_details") {
            JobDetailsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onApplyClick = {
                    navController.navigate("apply_project")
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onExploreClick = {
                    navController.navigate("explore") {
                        popUpTo("explore") { inclusive = true }
                    }
                }
            )
        }

        composable("apply_project") {
            ApplyProjectScreen(onBackClick = {
                navController.popBackStack()
            })
        }

    }
}
