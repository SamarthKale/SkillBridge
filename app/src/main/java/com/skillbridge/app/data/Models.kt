package com.skillbridge.app.data

import com.google.firebase.firestore.DocumentId

/**
 * All Firestore data classes for SkillBridge, in one place.
 * Every field has a default so Firestore's toObject() can build it with a no-arg constructor.
 */

/** A client-posted opportunity that students browse and apply to. Collection: `projects`. */
data class Opportunity(
    @DocumentId val id: String = "",
    val title: String = "",
    val company: String = "",
    val category: String = Category.OTHER,
    val budget: String = "",
    val workArrangement: String = WorkArrangement.REMOTE,
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val maxDistanceKm: Int = 0,
    val remote: Boolean = false,
    val skills: List<String> = emptyList(),
    val description: String = "",
    val responsibilities: String = "",
    val requirements: String = "",
    val experienceRequired: String = "",
    val deadline: String = "",
    val postedByUid: String = "",
    val postedByName: String = "",
    val postedAt: Long = 0L
)

/**
 * One per user. Collection: `profiles`, document id == uid.
 * `uid` is stored as a real field (not @DocumentId) because the security rules
 * validate `request.resource.data.uid`, and the seed data includes it.
 */
data class Profile(
    val uid: String = "",
    val role: String = "student",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val locationEnabled: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationUpdatedAt: Long = 0L,
    val photoUrl: String = "",
    val degree: String = "",
    val college: String = "",
    val branch: String = "",
    val graduationYear: Int = 0,
    val skills: List<String> = emptyList(),
    val experience: String = "",
    val bio: String = "",
    val resumeUrl: String = "",
    val companyName: String = "",
    val companyType: String = "",
    val companyDescription: String = "",
    val cardName: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    val acceptedTerms: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/** A student's application. Collection: `applications`, id == "{studentUid}_{projectId}". */
data class Application(
    @DocumentId val id: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val company: String = "",
    val budget: String = "",
    val studentUid: String = "",
    val studentName: String = "",
    val intro: String = "",
    val resumeUrl: String = "",
    val status: String = AppStatus.APPLIED,
    val appliedAt: Long = 0L
)

/** A single chat message tied to an application thread. Collection: `messages`. */
data class Message(
    @DocumentId val id: String = "",
    val applicationId: String = "",
    val projectTitle: String = "",
    val company: String = "",
    val studentUid: String = "",
    val clientName: String = "",
    val fromClient: Boolean = false,
    val text: String = "",
    val sentAt: Long = 0L
)

/** A lightweight per-user notification. Collection: `notifications`. */
data class AppNotification(
    @DocumentId val id: String = "",
    val uid: String = "",
    val type: String = "",
    val text: String = "",
    val createdAt: Long = 0L,
    val read: Boolean = false
)

// ---- Constant sets ----------------------------------------------------

object Category {
    const val DEVELOPMENT = "Development"
    const val DESIGN = "Design"
    const val WRITING = "Writing"
    const val MARKETING = "Marketing"
    const val DATA = "Data"
    const val OTHER = "Other"
    val ALL = listOf(DEVELOPMENT, DESIGN, WRITING, MARKETING, DATA, OTHER)
}

object AppStatus {
    const val APPLIED = "Applied"
    const val UNDER_REVIEW = "Under Review"
    const val SHORTLISTED = "Shortlisted"
    const val ACCEPTED = "Accepted"
    const val REJECTED = "Rejected"

    /** The happy-path pipeline, in order. Rejected sits outside it. */
    val PIPELINE = listOf(APPLIED, UNDER_REVIEW, SHORTLISTED, ACCEPTED)

    fun messagingUnlocked(status: String) = status == SHORTLISTED || status == ACCEPTED
}

object Skills {
    val ALL = listOf(
        "Android Development", "Web Development", "UI/UX", "Python", "Java", "Kotlin",
        "Firebase", "React", "Flutter", "Figma", "Data Analysis", "Machine Learning",
        "Content Writing", "Node.js", "SQL", "Marketing", "Video Editing"
    )
}

object WorkArrangement {
    const val REMOTE = "Remote"
    const val HYBRID = "Hybrid"
    const val ON_SITE = "On-site"
    val ALL = listOf(REMOTE, HYBRID, ON_SITE)
}
