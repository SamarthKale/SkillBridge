package com.skillbridge.app.data

import com.google.firebase.firestore.DocumentId

/**
 * A single freelance project. One document in the Firestore `projects` collection.
 *
 * All fields have defaults so Firestore's `toObject(Project::class.java)` can build it
 * with its required no-arg constructor. `id` is filled from the document id by
 * `@DocumentId` and is never written back as a field.
 */
data class Project(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectName: String = "",
    val clientName: String = "",
    val description: String = "",
    val budget: String = "",       // stored as text, e.g. "1500"
    val dueDate: String = "",      // stored as text, e.g. "2026-09-15"
    val status: String = Status.PENDING,
    val createdAt: Long = 0L       // epoch millis, set on create; used for sorting
)

/** The three allowed status values, in one place. */
object Status {
    const val PENDING = "Pending"
    const val IN_PROGRESS = "In Progress"
    const val COMPLETED = "Completed"

    val ALL = listOf(PENDING, IN_PROGRESS, COMPLETED)
}
