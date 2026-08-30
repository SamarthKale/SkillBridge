package com.skillbridge.app.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

/**
 * All Firestore access for projects lives here. One flat collection, `projects`,
 * where every document has a `userId` field equal to the Firebase Auth UID.
 *
 * Reads are always filtered by `userId` so a student only ever sees their own work.
 * Results are sorted newest-first in memory (small data set) so no composite index
 * is needed.
 *
 * Firestore calls are asynchronous, so every method takes a callback.
 */
object ProjectRepository {

    private const val TAG = "ProjectRepository"
    private const val COLLECTION = "projects"

    private val projects get() = Firebase.firestore.collection(COLLECTION)

    /**
     * Live list of the user's projects. Calls [onChange] immediately with the current
     * data and again whenever it changes. Returns a registration the caller must
     * `remove()` when the screen goes away.
     */
    fun listenForProjects(
        userId: String,
        onChange: (List<Project>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        return projects.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "listenForProjects failed", error)
                    onError("Could not load projects.")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Project::class.java) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                onChange(list)
            }
    }

    /** One-shot read of a single project. Passes null if it is missing or unreadable. */
    fun getProject(id: String, onResult: (Project?) -> Unit) {
        projects.document(id).get()
            .addOnSuccessListener { onResult(it.toObject(Project::class.java)) }
            .addOnFailureListener {
                Log.w(TAG, "getProject failed", it)
                onResult(null)
            }
    }

    /** Creates a project. Fills in userId + createdAt here so callers can't forget. */
    fun addProject(project: Project, userId: String, onResult: (Boolean) -> Unit) {
        val toSave = project.copy(userId = userId, createdAt = System.currentTimeMillis())
        projects.add(toSave)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                Log.w(TAG, "addProject failed", it)
                onResult(false)
            }
    }

    /** Overwrites an existing project, keeping its id, userId and createdAt. */
    fun updateProject(project: Project, onResult: (Boolean) -> Unit) {
        if (project.id.isEmpty()) {
            onResult(false); return
        }
        projects.document(project.id).set(project)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                Log.w(TAG, "updateProject failed", it)
                onResult(false)
            }
    }

    /** Changes only the status field (used by "Mark Completed"). */
    fun setStatus(id: String, status: String, onResult: (Boolean) -> Unit) {
        projects.document(id).update("status", status)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                Log.w(TAG, "setStatus failed", it)
                onResult(false)
            }
    }

    /** Deletes a project. */
    fun deleteProject(id: String, onResult: (Boolean) -> Unit) {
        projects.document(id).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                Log.w(TAG, "deleteProject failed", it)
                onResult(false)
            }
    }
}

/** Simple holder for the three dashboard numbers, derived from a project list. */
data class Counts(val total: Int = 0, val active: Int = 0, val completed: Int = 0) {
    companion object {
        fun from(projects: List<Project>): Counts {
            val completed = projects.count { it.status == Status.COMPLETED }
            return Counts(
                total = projects.size,
                active = projects.size - completed,
                completed = completed
            )
        }
    }
}
