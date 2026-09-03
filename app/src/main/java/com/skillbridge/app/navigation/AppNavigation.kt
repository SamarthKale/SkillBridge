package com.skillbridge.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.ApplicationStatusScreen
import com.skillbridge.app.ui.ApplicationSubmittedScreen
import com.skillbridge.app.ui.ApplicationsScreen
import com.skillbridge.app.ui.ApplyScreen
import com.skillbridge.app.ui.ConversationScreen
import com.skillbridge.app.ui.ExploreScreen
import com.skillbridge.app.ui.HomeScreen
import com.skillbridge.app.ui.LoginScreen
import com.skillbridge.app.ui.MessagesScreen
import com.skillbridge.app.ui.NotificationsScreen
import com.skillbridge.app.ui.ProfileScreen
import com.skillbridge.app.ui.ProfileSetupScreen
import com.skillbridge.app.ui.ProjectDetailsScreen
import com.skillbridge.app.ui.SignupScreen
import com.skillbridge.app.ui.SplashScreen
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in Routes.BOTTOM_TABS

    Scaffold(
        containerColor = Paper,
        bottomBar = { if (showBottomBar) BottomBar(nav, currentRoute) }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(inner)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onResolved = { route ->
                    nav.navigate(route) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = { resolveAfterAuth(nav) },
                    onCreateAccount = { nav.navigate(Routes.SIGNUP) }
                )
            }

            composable(Routes.SIGNUP) {
                SignupScreen(
                    onSignedUp = {
                        nav.navigate(Routes.PROFILE_SETUP) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                route = "profile_setup?edit={edit}",
                arguments = listOf(navArgument("edit") { type = NavType.BoolType; defaultValue = false })
            ) { entry ->
                val edit = entry.arguments?.getBoolean("edit") ?: false
                ProfileSetupScreen(
                    isEdit = edit,
                    onDone = {
                        if (edit) nav.popBackStack()
                        else nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    },
                    onBack = { if (edit) nav.popBackStack() }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onOpenExplore = { navigateTab(nav, Routes.EXPLORE) },
                    onOpenProject = { id -> nav.navigate(Routes.projectDetails(id)) },
                    onOpenApplications = { navigateTab(nav, Routes.APPLICATIONS) },
                    onOpenApplication = { appId -> nav.navigate(Routes.applicationStatus(appId)) },
                    onOpenNotifications = { nav.navigate(Routes.NOTIFICATIONS) }
                )
            }

            composable(Routes.EXPLORE) {
                ExploreScreen(onOpenProject = { id -> nav.navigate(Routes.projectDetails(id)) })
            }

            composable(Routes.APPLICATIONS) {
                ApplicationsScreen(
                    onOpenApplication = { appId -> nav.navigate(Routes.applicationStatus(appId)) }
                )
            }

            composable(Routes.MESSAGES) {
                MessagesScreen(
                    onOpenConversation = { appId -> nav.navigate(Routes.conversation(appId)) }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                    onLoggedOut = {
                        AuthManager.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(onBack = { nav.popBackStack() })
            }

            composable(
                route = Routes.PROJECT_DETAILS_ROUTE,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ProjectDetailsScreen(
                    opportunityId = id,
                    onBack = { nav.popBackStack() },
                    onApply = { nav.navigate(Routes.apply(id)) }
                )
            }

            composable(
                route = Routes.APPLY_ROUTE,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ApplyScreen(
                    opportunityId = id,
                    onBack = { nav.popBackStack() },
                    onSubmitted = {
                        nav.navigate(Routes.APPLICATION_SUBMITTED) {
                            popUpTo(Routes.projectDetails(id)) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.APPLICATION_SUBMITTED) {
                ApplicationSubmittedScreen(
                    onViewApplications = {
                        nav.navigate(Routes.APPLICATIONS) { popUpTo(Routes.HOME) }
                    },
                    onHome = { nav.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
                )
            }

            composable(
                route = Routes.APPLICATION_STATUS_ROUTE,
                arguments = listOf(navArgument("appId") { type = NavType.StringType })
            ) { entry ->
                val appId = entry.arguments?.getString("appId").orEmpty()
                ApplicationStatusScreen(
                    applicationId = appId,
                    onBack = { nav.popBackStack() },
                    onMessage = { nav.navigate(Routes.conversation(appId)) }
                )
            }

            composable(
                route = Routes.CONVERSATION_ROUTE,
                arguments = listOf(navArgument("appId") { type = NavType.StringType })
            ) { entry ->
                val appId = entry.arguments?.getString("appId").orEmpty()
                ConversationScreen(applicationId = appId, onBack = { nav.popBackStack() })
            }
        }
    }
}

/** After login: send to Home if the profile exists, else Profile Setup. */
private fun resolveAfterAuth(nav: NavHostController) {
    Repo.getProfile(AuthManager.uid()) { profile ->
        val dest = if (profile != null) Routes.HOME else Routes.PROFILE_SETUP
        nav.navigate(dest) { popUpTo(0) { inclusive = true } }
    }
}

private fun navigateTab(nav: NavHostController, route: String) {
    nav.navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomBar(nav: NavHostController, currentRoute: String?) {
    data class Tab(val route: String, val label: String, val icon: ImageVector)
    val tabs = listOf(
        Tab(Routes.HOME, "Home", Icons.Filled.Home),
        Tab(Routes.EXPLORE, "Explore", Icons.Filled.Search),
        Tab(Routes.APPLICATIONS, "Applications", Icons.Filled.List),
        Tab(Routes.MESSAGES, "Messages", Icons.Filled.Email),
        Tab(Routes.PROFILE, "Profile", Icons.Filled.Person),
    )
    NavigationBar(containerColor = Paper) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { navigateTab(nav, tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    indicatorColor = Paper,
                    unselectedIconColor = Mute,
                    unselectedTextColor = Mute
                )
            )
        }
    }
}
