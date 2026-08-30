package com.skillbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.ui.AddProjectScreen
import com.skillbridge.app.ui.HomeScreen
import com.skillbridge.app.ui.LoginScreen
import com.skillbridge.app.ui.ProfileScreen
import com.skillbridge.app.ui.ProjectDetailsScreen
import com.skillbridge.app.ui.ProjectsScreen
import com.skillbridge.app.ui.SignupScreen

/**
 * Single NavHost for the whole app. If a Firebase user is already signed in we start on
 * Home and skip Login entirely.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val start = if (AuthManager.isLoggedIn()) Routes.HOME else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccount = { navController.navigate(Routes.SIGNUP) }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignedUp = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenProjects = { navController.navigate(Routes.PROJECTS) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onAddProject = { navController.navigate(Routes.addProject()) },
                onOpenProject = { id -> navController.navigate(Routes.projectDetails(id)) }
            )
        }

        composable(Routes.PROJECTS) {
            ProjectsScreen(
                onBack = { navController.popBackStack() },
                onAddProject = { navController.navigate(Routes.addProject()) },
                onOpenProject = { id -> navController.navigate(Routes.projectDetails(id)) }
            )
        }

        composable(
            route = Routes.ADD_PROJECT_ROUTE,
            arguments = listOf(navArgument("projectId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            AddProjectScreen(
                projectId = entry.arguments?.getString("projectId"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PROJECT_DETAILS_ROUTE,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            ProjectDetailsScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.addProject(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
