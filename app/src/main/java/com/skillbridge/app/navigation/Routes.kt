package com.skillbridge.app.navigation

/** All navigation route strings in one place. Project ids are Firestore document ids (String). */
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val PROJECTS = "projects"
    const val PROFILE = "profile"

    // Add is reused for editing: pass a document id to edit, or omit to create.
    const val ADD_PROJECT_ROUTE = "add_project?projectId={projectId}"
    fun addProject(projectId: String? = null) =
        if (projectId == null) "add_project" else "add_project?projectId=$projectId"

    const val PROJECT_DETAILS_ROUTE = "project_details/{projectId}"
    fun projectDetails(projectId: String) = "project_details/$projectId"
}
