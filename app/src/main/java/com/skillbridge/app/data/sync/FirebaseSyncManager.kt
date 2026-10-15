package com.skillbridge.app.data.sync

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.skillbridge.app.data.*
import com.skillbridge.app.data.local.LocalDataSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseSyncManager(context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val localDb = LocalDataSource(context)
    private val TAG = "FirebaseSyncManager"

    fun syncAll(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Upload pending changes first
                syncPendingChanges()

                // Sync User Profile
                if (uid.isNotEmpty()) {
                    val profileSnap = db.collection("profiles").document(uid).get().await()
                    if (profileSnap.exists()) {
                        profileSnap.toProfileSafe()?.let { localDb.insertOrUpdateProfile(it) }
                    }
                }

                // Sync Student Profiles
                val studentsSnap = db.collection("profiles").whereEqualTo("role", "student").get().await()
                studentsSnap.documents.forEach { doc ->
                    doc.toProfileSafe()?.let { localDb.insertOrUpdateProfile(it) }
                }

                // Sync Opportunities / Projects
                val projectsSnap = db.collection("projects").get().await()
                projectsSnap.documents.forEach { doc ->
                    doc.toOpportunitySafe()?.let { localDb.insertOrUpdateProject(it) }
                }

                // Sync Applications if logged in
                if (uid.isNotEmpty()) {
                    // For students: their applications
                    val myAppsSnap = db.collection("applications").whereEqualTo("studentUid", uid).get().await()
                    myAppsSnap.documents.forEach { doc ->
                        doc.toObject(Application::class.java)?.let { localDb.insertOrUpdateApplication(it) }
                    }
                    
                    // For clients: proposals for their projects
                    val myProjects = localDb.getClientProjects(uid)
                    myProjects.forEach { proj ->
                        val proposalsSnap = db.collection("applications").whereEqualTo("projectId", proj.id).get().await()
                        proposalsSnap.documents.forEach { doc ->
                            doc.toObject(Application::class.java)?.let { localDb.insertOrUpdateApplication(it) }
                        }
                    }

                    // Sync Messages
                    val studentThreads = db.collection("messages").whereEqualTo("studentUid", uid).get().await()
                    studentThreads.documents.forEach { doc ->
                        doc.toObject(Message::class.java)?.let { localDb.insertMessage(it) }
                    }

                    if (myProjects.isNotEmpty()) {
                        val projectTitles = myProjects.map { it.title }.take(10)
                        if (projectTitles.isNotEmpty()) {
                            val clientThreads = db.collection("messages").whereIn("projectTitle", projectTitles).get().await()
                            clientThreads.documents.forEach { doc ->
                                doc.toObject(Message::class.java)?.let { localDb.insertMessage(it) }
                            }
                        }
                    }

                    // Sync Notifications
                    val notesSnap = db.collection("notifications").whereEqualTo("uid", uid).get().await()
                    notesSnap.documents.forEach { doc ->
                        doc.toObject(AppNotification::class.java)?.let { localDb.insertOrUpdateNotification(it) }
                    }
                }
                
                Log.d(TAG, "Sync complete!")
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
            }
        }
    }

    private suspend fun syncPendingChanges() {
        val pending = localDb.getPendingSyncItems()
        for (item in pending) {
            try {
                // simplified pending sync implementation
                when (item.entityType) {
                    "profile" -> {
                        // Assuming payload is full JSON string
                        // For a real app, you'd parse JSON and update
                        Log.d(TAG, "Syncing pending profile: ${item.entityId}")
                    }
                    // Implement other types as needed
                }
                localDb.markSyncItemComplete(item.id)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing pending item ${item.id}", e)
            }
        }
        localDb.clearCompletedSyncItems()
    }
}
