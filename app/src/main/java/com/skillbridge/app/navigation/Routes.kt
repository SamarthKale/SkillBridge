package com.skillbridge.app.navigation

/** All navigation routes in one place. Ids are Firestore document ids (String). */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PROFILE_SETUP = "profile_setup"     // also reused for "edit profile"
    const val PAYMENT_SETUP = "payment_setup"
    const val EDIT_PROFILE = "profile_setup?edit=true"

    // Bottom-nav tabs
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val APPLICATIONS = "applications"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"

    // Client specific bottom-nav tabs
    const val CLIENT_HOME = "client_home"
    const val CLIENT_DISCOVER = "client_discover"
    const val CLIENT_POST_PROJECT = "client_post_project"
    const val CLIENT_PROJECTS = "client_projects"

    const val NOTIFICATIONS = "notifications"
    const val AI_ASSISTANT = "ai_assistant"
    const val APPLICATION_SUBMITTED = "application_submitted"

    const val PROJECT_DETAILS_ROUTE = "project_details/{id}"
    fun projectDetails(id: String) = "project_details/$id"

    const val CLIENT_PROJECT_DETAILS_ROUTE = "client_project_details/{id}"
    fun clientProjectDetails(id: String) = "client_project_details/$id"

    const val PROPOSALS_ROUTE = "proposals/{id}"
    fun proposals(id: String) = "proposals/$id"
    
    const val CLIENT_PROPOSAL_DETAILS_ROUTE = "client_proposal_details/{id}"
    fun clientProposalDetails(id: String) = "client_proposal_details/$id"

    const val APPLY_ROUTE = "apply/{id}"
    fun apply(id: String) = "apply/$id"

    const val APPLICATION_STATUS_ROUTE = "application_status/{appId}"
    fun applicationStatus(appId: String) = "application_status/$appId"

    const val CONVERSATION_ROUTE = "conversation/{appId}"
    fun conversation(appId: String) = "conversation/$appId"

    const val STUDENT_PROFILE_ROUTE = "student_profile/{uid}"
    fun studentProfile(uid: String) = "student_profile/$uid"

    val BOTTOM_TABS = listOf(
        HOME, EXPLORE, APPLICATIONS, MESSAGES, PROFILE,
        CLIENT_HOME, CLIENT_DISCOVER, CLIENT_POST_PROJECT, CLIENT_PROJECTS
    )
}
