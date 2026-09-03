package com.skillbridge.app.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

/**
 * Single Firestore access point for the whole app. Flat and callback-based:
 * one-shot reads/writes take an `onResult`; live lists return a ListenerRegistration
 * the caller removes when its screen goes away. Lists are sorted in memory (small data),
 * so no composite indexes are needed.
 */
object Repo {

    private const val TAG = "Repo"
    private val db get() = Firebase.firestore

    // ---- Opportunities (collection `projects`) --------------------------

    fun listenOpportunities(
        onChange: (List<Opportunity>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration =
        db.collection("projects").addSnapshotListener { snap, err ->
            if (err != null) { Log.w(TAG, "listenOpportunities", err); onError("Could not load projects."); return@addSnapshotListener }
            onChange(snap?.documents?.mapNotNull { it.toObject(Opportunity::class.java) }
                ?.sortedByDescending { it.postedAt } ?: emptyList())
        }

    fun getOpportunity(id: String, onResult: (Opportunity?) -> Unit) {
        db.collection("projects").document(id).get()
            .addOnSuccessListener { onResult(it.toObject(Opportunity::class.java)) }
            .addOnFailureListener { Log.w(TAG, "getOpportunity", it); onResult(null) }
    }

    // ---- Profiles (collection `profiles`, id == uid) -------------------

    fun getProfile(uid: String, onResult: (Profile?) -> Unit) {
        if (uid.isEmpty()) { onResult(null); return }
        db.collection("profiles").document(uid).get()
            .addOnSuccessListener { onResult(if (it.exists()) it.toObject(Profile::class.java) else null) }
            .addOnFailureListener { Log.w(TAG, "getProfile", it); onResult(null) }
    }

    fun listenProfile(uid: String, onChange: (Profile?) -> Unit): ListenerRegistration =
        db.collection("profiles").document(uid).addSnapshotListener { snap, err ->
            if (err != null) { Log.w(TAG, "listenProfile", err); onChange(null); return@addSnapshotListener }
            onChange(if (snap != null && snap.exists()) snap.toObject(Profile::class.java) else null)
        }

    fun saveProfile(profile: Profile, onResult: (Boolean) -> Unit) {
        if (profile.uid.isEmpty()) { onResult(false); return }
        db.collection("profiles").document(profile.uid).set(profile)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.w(TAG, "saveProfile", it); onResult(false) }
    }

    // ---- Applications (collection `applications`) ---------------------

    fun applicationId(uid: String, projectId: String) = "${uid}_$projectId"

    fun listenMyApplications(
        uid: String,
        onChange: (List<Application>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration =
        db.collection("applications").whereEqualTo("studentUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenMyApplications", err); onError("Could not load applications."); return@addSnapshotListener }
                onChange(snap?.documents?.mapNotNull { it.toObject(Application::class.java) }
                    ?.sortedByDescending { it.appliedAt } ?: emptyList())
            }

    fun getApplication(id: String, onResult: (Application?) -> Unit) {
        db.collection("applications").document(id).get()
            .addOnSuccessListener { onResult(if (it.exists()) it.toObject(Application::class.java) else null) }
            .addOnFailureListener { Log.w(TAG, "getApplication", it); onResult(null) }
    }

    /** Creates an application if the student has not already applied. */
    fun submitApplication(
        opp: Opportunity,
        uid: String,
        studentName: String,
        intro: String,
        resumeUrl: String,
        onResult: (Boolean) -> Unit
    ) {
        val id = applicationId(uid, opp.id)
        val ref = db.collection("applications").document(id)
        ref.get().addOnSuccessListener { existing ->
            if (existing.exists()) { onResult(false); return@addOnSuccessListener }
            val app = Application(
                projectId = opp.id, projectTitle = opp.title, company = opp.company,
                budget = opp.budget, studentUid = uid, studentName = studentName,
                intro = intro, resumeUrl = resumeUrl,
                status = AppStatus.APPLIED, appliedAt = System.currentTimeMillis()
            )
            ref.set(app)
                .addOnSuccessListener {
                    addNotification(uid, "application_submitted",
                        "Your application for \"${opp.title}\" was submitted.")
                    onResult(true)
                }
                .addOnFailureListener { Log.w(TAG, "submitApplication", it); onResult(false) }
        }.addOnFailureListener { Log.w(TAG, "submitApplication/get", it); onResult(false) }
    }

    // ---- Messages (collection `messages`) ---------------------------

    fun listenThread(
        uid: String,
        applicationId: String,
        onChange: (List<Message>) -> Unit
    ): ListenerRegistration =
        db.collection("messages")
            .whereEqualTo("studentUid", uid)
            .whereEqualTo("applicationId", applicationId)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenThread", err); onChange(emptyList()); return@addSnapshotListener }
                onChange(snap?.documents?.mapNotNull { it.toObject(Message::class.java) }
                    ?.sortedBy { it.sentAt } ?: emptyList())
            }

    fun sendMessage(app: Application, uid: String, text: String, onResult: (Boolean) -> Unit) {
        val msg = Message(
            applicationId = app.id, projectTitle = app.projectTitle, company = app.company,
            studentUid = uid, clientName = app.company,
            fromClient = false, text = text, sentAt = System.currentTimeMillis()
        )
        db.collection("messages").add(msg)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.w(TAG, "sendMessage", it); onResult(false) }
    }

    // ---- Notifications (collection `notifications`) ----------------

    fun listenNotifications(uid: String, onChange: (List<AppNotification>) -> Unit): ListenerRegistration =
        db.collection("notifications").whereEqualTo("uid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenNotifications", err); onChange(emptyList()); return@addSnapshotListener }
                onChange(snap?.documents?.mapNotNull { it.toObject(AppNotification::class.java) }
                    ?.sortedByDescending { it.createdAt } ?: emptyList())
            }

    fun addNotification(uid: String, type: String, text: String) {
        val n = AppNotification(uid = uid, type = type, text = text,
            createdAt = System.currentTimeMillis(), read = false)
        db.collection("notifications").add(n)
            .addOnFailureListener { Log.w(TAG, "addNotification", it) }
    }

    fun markNotificationRead(id: String) {
        db.collection("notifications").document(id).update("read", true)
            .addOnFailureListener { Log.w(TAG, "markNotificationRead", it) }
    }
}
