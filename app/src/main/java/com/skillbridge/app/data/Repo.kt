package com.skillbridge.app.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.skillbridge.app.auth.AuthManager

object Repo {

    private const val TAG = "Repo"
    private val db get() = Firebase.firestore
    
    private var localDb: com.skillbridge.app.data.local.LocalDataSource? = null
    private var syncManager: com.skillbridge.app.data.sync.FirebaseSyncManager? = null
    private val gson = Gson()

    fun init(context: android.content.Context) {
        localDb = com.skillbridge.app.data.local.LocalDataSource(context.applicationContext)
        syncManager = com.skillbridge.app.data.sync.FirebaseSyncManager(context.applicationContext)
    }
    
    fun sync(uid: String) {
        syncManager?.syncAll(uid)
    }

    // ---- Opportunities (collection `projects`) --------------------------

    fun listenOpportunities(
        onChange: (List<Opportunity>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        val initialList = localDb?.getAllProjects() ?: emptyList()
        Log.d("Repo", "listenOpportunities: initial local size = ${initialList.size}")
        if (initialList.isNotEmpty()) onChange(initialList)
        
        return db.collection("projects").addSnapshotListener { snap, err ->
            if (err != null) { Log.e("Repo", "listenOpportunities: error", err); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { doc ->
                doc.toOpportunitySafe()
            }?.sortedByDescending { it.postedAt } ?: emptyList()

            val withCoords = list.count { it.latitude != null && it.longitude != null }
            Log.d("SkillBridgeMapDebug", "Firestore documents received: ${snap?.size() ?: 0}")
            Log.d("SkillBridgeMapDebug", "Successfully mapped projects: ${list.size}")
            Log.d("SkillBridgeMapDebug", "Projects with valid coordinates: $withCoords")

            // Log rejection reasons for projects without coordinates
            list.filter { it.latitude == null || it.longitude == null }.forEach { opp ->
                val reason = when {
                    opp.latitude == null && opp.longitude == null -> "Missing latitude and longitude"
                    opp.latitude == null -> "Missing latitude"
                    else -> "Missing longitude"
                }
                Log.d("SkillBridgeMapDebug", "Project '${opp.title}' (${opp.id}) no map marker: $reason, workArrangement=${opp.workArrangement}, remote=${opp.remote}")
            }

            list.forEach { localDb?.insertOrUpdateProject(it) }
            onChange(list)
        }
    }

    fun getOpportunity(id: String, onResult: (Opportunity?) -> Unit) {
        localDb?.getProject(id)?.let { onResult(it) }
        db.collection("projects").document(id).get()
            .addOnSuccessListener { doc ->
                doc.toOpportunitySafe()?.let { opp ->
                    localDb?.insertOrUpdateProject(opp)
                    onResult(opp)
                }
            }
            .addOnFailureListener { Log.w(TAG, "getOpportunity", it) }
    }

    fun listenClientProjects(
        uid: String,
        onChange: (List<Opportunity>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        localDb?.getClientProjects(uid)?.let { if (it.isNotEmpty()) onChange(it) }
        return db.collection("projects").whereEqualTo("postedByUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenClientProjects", err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toOpportunitySafe()
                }
                    ?.sortedByDescending { it.postedAt } ?: emptyList()
                list.forEach { localDb?.insertOrUpdateProject(it) }
                onChange(list)
            }
    }

    fun postOpportunity(opp: Opportunity, onResult: (Boolean) -> Unit) {
        val newRef = db.collection("projects").document()
        val finalOpp = if (opp.id.isEmpty()) opp.copy(id = newRef.id) else opp
        localDb?.insertOrUpdateProject(finalOpp)
        
        newRef.set(finalOpp)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { 
                Log.w(TAG, "postOpportunity", it)
                localDb?.addToSyncQueue("INSERT", "project", finalOpp.id, gson.toJson(finalOpp))
                onResult(true)
            }
    }

    // ---- Profiles (collection `profiles`, id == uid) -------------------

    fun getProfile(uid: String, onResult: (Profile?) -> Unit) {
        if (uid.isEmpty()) { onResult(null); return }
        localDb?.getProfile(uid)?.let { onResult(it) }
        db.collection("profiles").document(uid).get()
            .addOnSuccessListener { doc ->
                val p = doc.toProfileSafe()
                if (p != null) {
                    localDb?.insertOrUpdateProfile(p)
                    onResult(p)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { Log.w(TAG, "getProfile", it) }
    }

    fun listenProfile(uid: String, onChange: (Profile?) -> Unit): ListenerRegistration {
        localDb?.getProfile(uid)?.let { onChange(it) }
        return db.collection("profiles").document(uid).addSnapshotListener { snap, err ->
            if (err != null) { Log.w(TAG, "listenProfile", err); return@addSnapshotListener }
            val p = snap?.toProfileSafe()
            if (p != null) {
                localDb?.insertOrUpdateProfile(p)
                onChange(p)
            }
        }
    }

    fun listenStudentProfiles(onChange: (List<Profile>) -> Unit): ListenerRegistration {
        localDb?.getAllStudentProfiles()?.let { if (it.isNotEmpty()) onChange(it) }
        return db.collection("profiles").whereEqualTo("role", "student")
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenStudentProfiles", err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toProfileSafe()
                } ?: emptyList()
                list.forEach { localDb?.insertOrUpdateProfile(it) }
                onChange(list)
            }
    }

    fun saveProfile(profile: Profile, onResult: (Boolean) -> Unit) {
        if (profile.uid.isEmpty()) { onResult(false); return }
        localDb?.insertOrUpdateProfile(profile)
        db.collection("profiles").document(profile.uid).set(profile)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { 
                Log.w(TAG, "saveProfile", it)
                localDb?.addToSyncQueue("UPDATE", "profile", profile.uid, gson.toJson(profile))
                onResult(true)
            }
    }

    fun updatePaymentInfo(
        uid: String, 
        cardName: String, 
        cardNumber: String, 
        cardExpiry: String, 
        cardCvv: String, 
        onResult: (Boolean) -> Unit
    ) {
        if (uid.isEmpty()) { onResult(false); return }
        val updates = mapOf(
            "cardName" to cardName,
            "cardNumber" to cardNumber,
            "cardExpiry" to cardExpiry,
            "cardCvv" to cardCvv,
            "acceptedTerms" to true
        )
        // Update local first
        localDb?.getProfile(uid)?.let { p ->
            val updated = p.copy(cardName = cardName, cardNumber = cardNumber, cardExpiry = cardExpiry, cardCvv = cardCvv, acceptedTerms = true)
            localDb?.insertOrUpdateProfile(updated)
        }

        db.collection("profiles").document(uid).update(updates)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.w(TAG, "updatePaymentInfo", it); onResult(true) }
    }

    // ---- Applications (collection `applications`) ---------------------

    fun applicationId(uid: String, projectId: String) = "${uid}_$projectId"

    fun listenMyApplications(
        uid: String,
        onChange: (List<Application>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        localDb?.getStudentApplications(uid)?.let { if (it.isNotEmpty()) onChange(it) }
        return db.collection("applications").whereEqualTo("studentUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenMyApplications", err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(Application::class.java) }
                    ?.sortedByDescending { it.appliedAt } ?: emptyList()
                list.forEach { localDb?.insertOrUpdateApplication(it) }
                onChange(list)
            }
    }

    fun getApplication(id: String, onResult: (Application?) -> Unit) {
        localDb?.getApplication(id)?.let { onResult(it) }
        db.collection("applications").document(id).get()
            .addOnSuccessListener { 
                val a = if (it.exists()) it.toObject(Application::class.java) else null
                if (a != null) {
                    localDb?.insertOrUpdateApplication(a)
                    onResult(a)
                }
            }
            .addOnFailureListener { Log.w(TAG, "getApplication", it) }
    }

    fun listenProposals(
        projectId: String,
        onChange: (List<Application>) -> Unit
    ): ListenerRegistration {
        localDb?.getProjectApplications(projectId)?.let { if (it.isNotEmpty()) onChange(it) }
        return db.collection("applications").whereEqualTo("projectId", projectId)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenProposals", err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(Application::class.java) }
                    ?.sortedByDescending { it.appliedAt } ?: emptyList()
                list.forEach { localDb?.insertOrUpdateApplication(it) }
                onChange(list)
            }
    }

    fun updateApplicationStatus(id: String, newStatus: String, onResult: (Boolean) -> Unit) {
        localDb?.getApplication(id)?.let { a ->
            localDb?.insertOrUpdateApplication(a.copy(status = newStatus))
        }
        db.collection("applications").document(id).update("status", newStatus)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.w(TAG, "updateApplicationStatus", it); onResult(true) }
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
        localDb?.getApplication(id)?.let {
            // locally already exists
            onResult(false)
            return
        }
        
        val app = Application(
            id = id, projectId = opp.id, projectTitle = opp.title, company = opp.company,
            budget = opp.budget, studentUid = uid, studentName = studentName,
            intro = intro, resumeUrl = resumeUrl,
            status = AppStatus.APPLIED, appliedAt = System.currentTimeMillis()
        )
        localDb?.insertOrUpdateApplication(app)
        
        val ref = db.collection("applications").document(id)
        ref.set(app)
            .addOnSuccessListener {
                addNotification(uid, "application_submitted", "Your application for \"${opp.title}\" was submitted.")
                onResult(true)
            }
            .addOnFailureListener { 
                Log.w(TAG, "submitApplication", it)
                localDb?.addToSyncQueue("INSERT", "application", id, gson.toJson(app))
                addNotification(uid, "application_submitted", "Your application for \"${opp.title}\" is queued.")
                onResult(true) 
            }
    }

    // ---- Messages (collection `messages`) ---------------------------

    fun listenClientThreads(
        uid: String,
        onChange: (List<Message>) -> Unit
    ): ListenerRegistration {
        localDb?.getClientProjects(uid)?.map { it.title }?.let { titles ->
            localDb?.getClientThreads(titles)?.let { if (it.isNotEmpty()) onChange(it) }
        }
        
        return db.collection("projects").whereEqualTo("postedByUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val projectTitles = snap.documents.mapNotNull { it.getString("title") }
                if (projectTitles.isEmpty()) return@addSnapshotListener

                db.collection("messages").whereIn("projectTitle", projectTitles.take(10))
                    .addSnapshotListener { msgSnap, _ ->
                        val msgs = msgSnap?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
                        msgs.forEach { localDb?.insertMessage(it) }
                        onChange(msgs.sortedBy { it.sentAt })
                    }
            }
    }

    fun listenStudentThreads(
        uid: String,
        onChange: (List<Message>) -> Unit
    ): ListenerRegistration {
        localDb?.getStudentThreads(uid)?.let { if (it.isNotEmpty()) onChange(it) }
        return db.collection("messages").whereEqualTo("studentUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenStudentThreads", err); return@addSnapshotListener }
                val msgs = snap?.documents?.mapNotNull { it.toObject(Message::class.java) }?.sortedByDescending { it.sentAt } ?: emptyList()
                msgs.forEach { localDb?.insertMessage(it) }
                onChange(msgs)
            }
    }

    fun listenThread(
        applicationId: String,
        onChange: (List<Message>) -> Unit
    ): ListenerRegistration {
        localDb?.getThread(applicationId)?.let { if (it.isNotEmpty()) onChange(it) }
        return db.collection("messages").whereEqualTo("applicationId", applicationId)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenThread", err); return@addSnapshotListener }
                val msgs = snap?.documents?.mapNotNull { it.toObject(Message::class.java) }?.sortedBy { it.sentAt } ?: emptyList()
                msgs.forEach { localDb?.insertMessage(it) }
                onChange(msgs)
            }
    }

    fun sendMessage(app: Application, uid: String, text: String, isClient: Boolean, onResult: (Boolean) -> Unit) {
        val newRef = db.collection("messages").document()
        val msg = Message(
            id = newRef.id, applicationId = app.id, projectTitle = app.projectTitle, company = app.company,
            studentUid = app.studentUid, clientName = app.company,
            fromClient = isClient, text = text, sentAt = System.currentTimeMillis()
        )
        localDb?.insertMessage(msg)
        
        newRef.set(msg)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { 
                Log.w(TAG, "sendMessage", it)
                localDb?.addToSyncQueue("INSERT", "message", msg.id, gson.toJson(msg))
                onResult(true)
            }
    }

    // ---- Notifications (collection `notifications`) ----------------

    fun listenNotifications(uid: String, onChange: (List<AppNotification>) -> Unit): ListenerRegistration {
        localDb?.getNotifications(uid)?.let { if (it.isNotEmpty()) onChange(it) }
        return db.collection("notifications").whereEqualTo("uid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { Log.w(TAG, "listenNotifications", err); return@addSnapshotListener }
                val notes = snap?.documents?.mapNotNull { it.toObject(AppNotification::class.java) }?.sortedByDescending { it.createdAt } ?: emptyList()
                notes.forEach { localDb?.insertOrUpdateNotification(it) }
                onChange(notes)
            }
    }

    fun addNotification(uid: String, type: String, text: String) {
        val newRef = db.collection("notifications").document()
        val n = AppNotification(id = newRef.id, uid = uid, type = type, text = text,
            createdAt = System.currentTimeMillis(), read = false)
        localDb?.insertOrUpdateNotification(n)
            
        newRef.set(n).addOnFailureListener { 
            Log.w(TAG, "addNotification", it)
            localDb?.addToSyncQueue("INSERT", "notification", n.id, gson.toJson(n))
        }
    }

    fun markNotificationRead(id: String) {
        localDb?.getNotifications(AuthManager.uid())?.find { it.id == id }?.let { n ->
            localDb?.insertOrUpdateNotification(n.copy(read = true))
        }
        db.collection("notifications").document(id).update("read", true)
            .addOnFailureListener { Log.w(TAG, "markNotificationRead", it) }
    }
}
