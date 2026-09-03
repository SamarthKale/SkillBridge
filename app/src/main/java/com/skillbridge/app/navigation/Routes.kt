package com.skillbridge.app.navigation

/** All navigation routes in one place. Ids are Firestore document ids (String). */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PROFILE_SETUP = "profile_setup"     // also reused for "edit profile"
    const val EDIT_PROFILE = "profile_setup?edit=true"

    // Bottom-nav tabs
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val APPLICATIONS = "applications"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"

    const val NOTIFICATIONS = "notifications"
    const val APPLICATION_SUBMITTED = "application_submitted"

    const val PROJECT_DETAILS_ROUTE = "project_details/{id}"
    fun projectDetails(id: String) = "project_details/$id"

    const val APPLY_ROUTE = "apply/{id}"
    fun apply(id: String) = "apply/$id"

    const val APPLICATION_STATUS_ROUTE = "application_status/{appId}"
    fun applicationStatus(appId: String) = "application_status/$appId"

    const val CONVERSATION_ROUTE = "conversation/{appId}"
    fun conversation(appId: String) = "conversation/$appId"

    val BOTTOM_TABS = listOf(HOME, EXPLORE, APPLICATIONS, MESSAGES, PROFILE)
}
