package com.skillbridge.app.data.sync

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.skillbridge.app.data.*
import com.skillbridge.app.data.local.DatabaseHelper
import kotlinx.coroutines.tasks.await

class FirebaseToSQLiteSync(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val gson = Gson()
    private val TAG = "FirebaseToSQLiteSync"

    private fun Boolean.toInt() = if (this) 1 else 0

    suspend fun syncAll(uid: String) {
        if (uid.isEmpty()) {
            Log.d(TAG, "Firebase → SQLite: SYNC CANCELLED (No authenticated user exists)")
            return
        }

        Log.d(TAG, "Firebase → SQLite: SYNC STARTED")
        Log.d(TAG, "Firebase → SQLite: current UID = $uid")
        try {
            syncProfiles(uid)
            syncProjects()
            syncApplications(uid)
            syncMessages(uid)
            syncNotifications(uid)
            Log.d(TAG, "Firebase → SQLite: SYNC COMPLETED")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase → SQLite: Error during sync", e)
        }
    }

    suspend fun syncProfiles(uid: String) {
        Log.d(TAG, "Firebase → SQLite: profiles query started")
        val db = dbHelper.writableDatabase
        var insertedCount = 0
        try {
            // Firestore Security Rules typically block `.get()` on the entire collection.
            // We must mirror the authorized queries: Current user's profile + Student profiles.
            val studentSnap = firestore.collection("profiles").whereEqualTo("role", "student").get().await()
            val myProfileSnap = firestore.collection("profiles").document(uid).get().await()

            val allProfiles = studentSnap.documents.toMutableList()
            if (myProfileSnap.exists() && allProfiles.none { it.id == uid }) {
                allProfiles.add(myProfileSnap)
            }

            Log.d(TAG, "Firebase → SQLite: profiles documents received = ${allProfiles.size}")
            
            db.beginTransaction()
            try {
                for (doc in allProfiles) {
                    val profile = doc.toProfileSafe() ?: continue
                    
                    val cv = ContentValues().apply {
                        put("uid", profile.uid)
                        put("role", profile.role)
                        put("name", profile.name)
                        put("email", profile.email)
                        put("phone", profile.phone)
                        put("location", profile.location)
                        put("locationEnabled", profile.locationEnabled.toInt())
                        put("latitude", profile.latitude)
                        put("longitude", profile.longitude)
                        put("locationUpdatedAt", profile.locationUpdatedAt)
                        put("photoUrl", profile.photoUrl)
                        put("degree", profile.degree)
                        put("college", profile.college)
                        put("branch", profile.branch)
                        put("graduationYear", profile.graduationYear)
                        put("skills", gson.toJson(profile.skills))
                        put("experience", profile.experience)
                        put("bio", profile.bio)
                        put("resumeUrl", profile.resumeUrl)
                        put("companyName", profile.companyName)
                        put("companyType", profile.companyType)
                        put("companyDescription", profile.companyDescription)
                        put("cardName", profile.cardName)
                        put("cardNumber", profile.cardNumber)
                        put("cardExpiry", profile.cardExpiry)
                        put("cardCvv", profile.cardCvv)
                        put("acceptedTerms", profile.acceptedTerms.toInt())
                        put("createdAt", profile.createdAt)
                        put("updatedAt", profile.updatedAt)
                    }
                    db.replace(DatabaseHelper.TABLE_PROFILES, null, cv)
                    insertedCount++
                }
                db.setTransactionSuccessful()
                Log.d(TAG, "Firebase → SQLite: profiles inserted/updated = $insertedCount")
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase → SQLite: Error syncing profiles", e)
        }
    }

    suspend fun syncProjects() {
        Log.d(TAG, "Firebase → SQLite: projects query started")
        try {
            val snap = firestore.collection("projects").get().await()
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                for (doc in snap.documents) {
                    val opp = doc.toOpportunitySafe() ?: continue
                    
                    val cv = ContentValues().apply {
                        put("id", opp.id)
                        put("title", opp.title)
                        put("company", opp.company)
                        put("category", opp.category)
                        put("budget", opp.budget)
                        put("workArrangement", opp.workArrangement)
                        put("location", opp.location)
                        put("latitude", opp.latitude)
                        put("longitude", opp.longitude)
                        put("maxDistanceKm", opp.maxDistanceKm)
                        put("remote", opp.remote.toInt())
                        put("skills", gson.toJson(opp.skills))
                        put("description", opp.description)
                        put("responsibilities", opp.responsibilities)
                        put("requirements", opp.requirements)
                        put("experienceRequired", opp.experienceRequired)
                        put("deadline", opp.deadline)
                        put("postedByUid", opp.postedByUid)
                        put("postedByName", opp.postedByName)
                        put("postedAt", opp.postedAt)
                    }
                    db.replace(DatabaseHelper.TABLE_PROJECTS, null, cv)
                }
                db.setTransactionSuccessful()
                Log.d(TAG, "Firebase → SQLite: projects documents received = ${snap.size()}")
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase → SQLite: Error syncing projects", e)
        }
    }

    suspend fun syncApplications(uid: String) {
        try {
            // Need to get student applications AND applications for the client's projects
            val studentSnap = firestore.collection("applications").whereEqualTo("studentUid", uid).get().await()
            val myProjects = dbHelper.readableDatabase.query(DatabaseHelper.TABLE_PROJECTS, null, "postedByUid = ?", arrayOf(uid), null, null, null)
            val myProjectIds = mutableListOf<String>()
            while (myProjects.moveToNext()) {
                myProjectIds.add(myProjects.getString(myProjects.getColumnIndexOrThrow("id")))
            }
            myProjects.close()

            val allDocs = studentSnap.documents.toMutableList()
            for (projectId in myProjectIds) {
                val projSnap = firestore.collection("applications").whereEqualTo("projectId", projectId).get().await()
                for (doc in projSnap.documents) {
                    if (allDocs.none { it.id == doc.id }) {
                        allDocs.add(doc)
                    }
                }
            }

            Log.d(TAG, "Firebase → SQLite: applications documents received = ${allDocs.size}")

            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                for (doc in allDocs) {
                    val app = doc.toObject(Application::class.java) ?: continue
                    val cv = ContentValues().apply {
                        put("id", app.id)
                        put("projectId", app.projectId)
                        put("projectTitle", app.projectTitle)
                        put("company", app.company)
                        put("budget", app.budget)
                        put("studentUid", app.studentUid)
                        put("studentName", app.studentName)
                        put("intro", app.intro)
                        put("resumeUrl", app.resumeUrl)
                        put("status", app.status)
                        put("appliedAt", app.appliedAt)
                    }
                    db.replace(DatabaseHelper.TABLE_APPLICATIONS, null, cv)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase → SQLite: Error syncing applications", e)
        }
    }

    suspend fun syncMessages(uid: String) {
        try {
            val studentSnap = firestore.collection("messages").whereEqualTo("studentUid", uid).get().await()
            val myProjects = dbHelper.readableDatabase.query(DatabaseHelper.TABLE_PROJECTS, null, "postedByUid = ?", arrayOf(uid), null, null, null)
            val myProjectTitles = mutableListOf<String>()
            while (myProjects.moveToNext()) {
                val title = myProjects.getString(myProjects.getColumnIndexOrThrow("title"))
                if (title.isNotEmpty()) myProjectTitles.add(title)
            }
            myProjects.close()
            
            val allDocs = studentSnap.documents.toMutableList()
            if (myProjectTitles.isNotEmpty()) {
                val clientSnap = firestore.collection("messages").whereIn("projectTitle", myProjectTitles.take(10)).get().await()
                for (doc in clientSnap.documents) {
                    if (allDocs.none { it.id == doc.id }) {
                        allDocs.add(doc)
                    }
                }
            }
            Log.d(TAG, "Firebase → SQLite: messages documents received = ${allDocs.size}")
            
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                for (doc in allDocs) {
                    val msg = doc.toObject(Message::class.java) ?: continue
                    val cv = ContentValues().apply {
                        put("id", msg.id)
                        put("applicationId", msg.applicationId)
                        put("projectTitle", msg.projectTitle)
                        put("company", msg.company)
                        put("studentUid", msg.studentUid)
                        put("clientName", msg.clientName)
                        put("fromClient", msg.fromClient.toInt())
                        put("text", msg.text)
                        put("sentAt", msg.sentAt)
                    }
                    db.replace(DatabaseHelper.TABLE_MESSAGES, null, cv)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase → SQLite: Error syncing messages", e)
        }
    }

    suspend fun syncNotifications(uid: String) {
        try {
            val snap = firestore.collection("notifications").whereEqualTo("uid", uid).get().await()
            Log.d(TAG, "Firebase → SQLite: notifications documents received = ${snap.size()}")
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                for (doc in snap.documents) {
                    val n = doc.toObject(AppNotification::class.java) ?: continue
                    val cv = ContentValues().apply {
                        put("id", n.id)
                        put("uid", n.uid)
                        put("type", n.type)
                        put("text", n.text)
                        put("createdAt", n.createdAt)
                        put("read", n.read.toInt())
                    }
                    db.replace(DatabaseHelper.TABLE_NOTIFICATIONS, null, cv)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase → SQLite: Error syncing notifications", e)
        }
    }
}
