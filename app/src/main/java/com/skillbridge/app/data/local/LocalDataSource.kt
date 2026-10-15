package com.skillbridge.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skillbridge.app.data.*
import java.util.UUID
import android.util.Log

class LocalDataSource(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val gson = Gson()
    private val TAG = "LocalDataSource"

    // Helper for List<String>
    private fun List<String>.toJson(): String = gson.toJson(this)
    private fun String?.toList(): List<String> {
        if (this.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(this, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun Boolean.toInt() = if (this) 1 else 0
    private fun Int.toBoolean() = this == 1

    // --- Profile ---

    fun insertOrUpdateProfile(profile: Profile) {
        val db = dbHelper.writableDatabase
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
            put("skills", profile.skills.toJson())
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
    }

    fun getProfile(uid: String): Profile? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PROFILES,
            null,
            "uid = ?",
            arrayOf(uid),
            null, null, null
        )
        var profile: Profile? = null
        if (cursor.moveToFirst()) {
            profile = cursorToProfile(cursor)
        }
        cursor.close()
        return profile
    }

    fun getAllStudentProfiles(): List<Profile> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PROFILES,
            null,
            "role = ?",
            arrayOf("student"),
            null, null, null
        )
        val list = mutableListOf<Profile>()
        while (cursor.moveToNext()) {
            list.add(cursorToProfile(cursor))
        }
        cursor.close()
        return list
    }

    private fun cursorToProfile(cursor: Cursor): Profile {
        return Profile(
            uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")) ?: "",
            role = cursor.getString(cursor.getColumnIndexOrThrow("role")) ?: "student",
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: "",
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "",
            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")) ?: "",
            location = cursor.getString(cursor.getColumnIndexOrThrow("location")) ?: "",
            locationEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("locationEnabled")).toBoolean(),
            latitude = if (cursor.isNull(cursor.getColumnIndexOrThrow("latitude"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
            longitude = if (cursor.isNull(cursor.getColumnIndexOrThrow("longitude"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
            locationUpdatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("locationUpdatedAt")),
            photoUrl = cursor.getString(cursor.getColumnIndexOrThrow("photoUrl")) ?: "",
            degree = cursor.getString(cursor.getColumnIndexOrThrow("degree")) ?: "",
            college = cursor.getString(cursor.getColumnIndexOrThrow("college")) ?: "",
            branch = cursor.getString(cursor.getColumnIndexOrThrow("branch")) ?: "",
            graduationYear = cursor.getInt(cursor.getColumnIndexOrThrow("graduationYear")),
            skills = cursor.getString(cursor.getColumnIndexOrThrow("skills")).toList(),
            experience = cursor.getString(cursor.getColumnIndexOrThrow("experience")) ?: "",
            bio = cursor.getString(cursor.getColumnIndexOrThrow("bio")) ?: "",
            resumeUrl = cursor.getString(cursor.getColumnIndexOrThrow("resumeUrl")) ?: "",
            companyName = cursor.getString(cursor.getColumnIndexOrThrow("companyName")) ?: "",
            companyType = cursor.getString(cursor.getColumnIndexOrThrow("companyType")) ?: "",
            companyDescription = cursor.getString(cursor.getColumnIndexOrThrow("companyDescription")) ?: "",
            cardName = cursor.getString(cursor.getColumnIndexOrThrow("cardName")) ?: "",
            cardNumber = cursor.getString(cursor.getColumnIndexOrThrow("cardNumber")) ?: "",
            cardExpiry = cursor.getString(cursor.getColumnIndexOrThrow("cardExpiry")) ?: "",
            cardCvv = cursor.getString(cursor.getColumnIndexOrThrow("cardCvv")) ?: "",
            acceptedTerms = cursor.getInt(cursor.getColumnIndexOrThrow("acceptedTerms")).toBoolean(),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt"))
        )
    }

    // --- Projects ---

    fun insertOrUpdateProject(opp: Opportunity) {
        val db = dbHelper.writableDatabase
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
            put("skills", opp.skills.toJson())
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

    fun getAllProjects(): List<Opportunity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, null, null, null, null, "postedAt DESC")
        val list = mutableListOf<Opportunity>()
        while (cursor.moveToNext()) {
            list.add(cursorToProject(cursor))
        }
        cursor.close()
        return list
    }

    fun getClientProjects(uid: String): List<Opportunity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PROJECTS, null, "postedByUid = ?", arrayOf(uid), null, null, "postedAt DESC"
        )
        val list = mutableListOf<Opportunity>()
        while (cursor.moveToNext()) {
            list.add(cursorToProject(cursor))
        }
        cursor.close()
        return list
    }

    fun getProject(id: String): Opportunity? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, "id = ?", arrayOf(id), null, null, null)
        var opp: Opportunity? = null
        if (cursor.moveToFirst()) {
            opp = cursorToProject(cursor)
        }
        cursor.close()
        return opp
    }

    private fun cursorToProject(cursor: Cursor): Opportunity {
        return Opportunity(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")) ?: "",
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")) ?: "",
            company = cursor.getString(cursor.getColumnIndexOrThrow("company")) ?: "",
            category = cursor.getString(cursor.getColumnIndexOrThrow("category")) ?: "",
            budget = cursor.getString(cursor.getColumnIndexOrThrow("budget")) ?: "",
            workArrangement = cursor.getString(cursor.getColumnIndexOrThrow("workArrangement")) ?: "",
            location = cursor.getString(cursor.getColumnIndexOrThrow("location")) ?: "",
            latitude = if (cursor.isNull(cursor.getColumnIndexOrThrow("latitude"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
            longitude = if (cursor.isNull(cursor.getColumnIndexOrThrow("longitude"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
            maxDistanceKm = cursor.getInt(cursor.getColumnIndexOrThrow("maxDistanceKm")),
            remote = cursor.getInt(cursor.getColumnIndexOrThrow("remote")).toBoolean(),
            skills = cursor.getString(cursor.getColumnIndexOrThrow("skills")).toList(),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
            responsibilities = cursor.getString(cursor.getColumnIndexOrThrow("responsibilities")) ?: "",
            requirements = cursor.getString(cursor.getColumnIndexOrThrow("requirements")) ?: "",
            experienceRequired = cursor.getString(cursor.getColumnIndexOrThrow("experienceRequired")) ?: "",
            deadline = cursor.getString(cursor.getColumnIndexOrThrow("deadline")) ?: "",
            postedByUid = cursor.getString(cursor.getColumnIndexOrThrow("postedByUid")) ?: "",
            postedByName = cursor.getString(cursor.getColumnIndexOrThrow("postedByName")) ?: "",
            postedAt = cursor.getLong(cursor.getColumnIndexOrThrow("postedAt"))
        )
    }

    // --- Applications ---

    fun insertOrUpdateApplication(app: Application) {
        val db = dbHelper.writableDatabase
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

    fun getStudentApplications(uid: String): List<Application> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_APPLICATIONS, null, "studentUid = ?", arrayOf(uid), null, null, "appliedAt DESC")
        val list = mutableListOf<Application>()
        while (cursor.moveToNext()) {
            list.add(cursorToApplication(cursor))
        }
        cursor.close()
        return list
    }

    fun getProjectApplications(projectId: String): List<Application> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_APPLICATIONS, null, "projectId = ?", arrayOf(projectId), null, null, "appliedAt DESC")
        val list = mutableListOf<Application>()
        while (cursor.moveToNext()) {
            list.add(cursorToApplication(cursor))
        }
        cursor.close()
        return list
    }

    fun getApplication(id: String): Application? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_APPLICATIONS, null, "id = ?", arrayOf(id), null, null, null)
        var app: Application? = null
        if (cursor.moveToFirst()) {
            app = cursorToApplication(cursor)
        }
        cursor.close()
        return app
    }

    private fun cursorToApplication(cursor: Cursor): Application {
        return Application(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")) ?: "",
            projectId = cursor.getString(cursor.getColumnIndexOrThrow("projectId")) ?: "",
            projectTitle = cursor.getString(cursor.getColumnIndexOrThrow("projectTitle")) ?: "",
            company = cursor.getString(cursor.getColumnIndexOrThrow("company")) ?: "",
            budget = cursor.getString(cursor.getColumnIndexOrThrow("budget")) ?: "",
            studentUid = cursor.getString(cursor.getColumnIndexOrThrow("studentUid")) ?: "",
            studentName = cursor.getString(cursor.getColumnIndexOrThrow("studentName")) ?: "",
            intro = cursor.getString(cursor.getColumnIndexOrThrow("intro")) ?: "",
            resumeUrl = cursor.getString(cursor.getColumnIndexOrThrow("resumeUrl")) ?: "",
            status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "",
            appliedAt = cursor.getLong(cursor.getColumnIndexOrThrow("appliedAt"))
        )
    }

    // --- Messages ---
    
    fun insertMessage(msg: Message) {
        val db = dbHelper.writableDatabase
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

    fun getStudentThreads(uid: String): List<Message> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_MESSAGES, null, "studentUid = ?", arrayOf(uid), null, null, "sentAt DESC")
        val list = mutableListOf<Message>()
        while (cursor.moveToNext()) {
            list.add(cursorToMessage(cursor))
        }
        cursor.close()
        return list
    }

    fun getClientThreads(projectTitles: List<String>): List<Message> {
        if (projectTitles.isEmpty()) return emptyList()
        val db = dbHelper.readableDatabase
        val placeholders = projectTitles.joinToString(",") { "?" }
        val cursor = db.query(DatabaseHelper.TABLE_MESSAGES, null, "projectTitle IN ($placeholders)", projectTitles.toTypedArray(), null, null, "sentAt ASC")
        val list = mutableListOf<Message>()
        while (cursor.moveToNext()) {
            list.add(cursorToMessage(cursor))
        }
        cursor.close()
        return list
    }

    fun getThread(applicationId: String): List<Message> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_MESSAGES, null, "applicationId = ?", arrayOf(applicationId), null, null, "sentAt ASC")
        val list = mutableListOf<Message>()
        while (cursor.moveToNext()) {
            list.add(cursorToMessage(cursor))
        }
        cursor.close()
        return list
    }

    private fun cursorToMessage(cursor: Cursor): Message {
        return Message(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")) ?: "",
            applicationId = cursor.getString(cursor.getColumnIndexOrThrow("applicationId")) ?: "",
            projectTitle = cursor.getString(cursor.getColumnIndexOrThrow("projectTitle")) ?: "",
            company = cursor.getString(cursor.getColumnIndexOrThrow("company")) ?: "",
            studentUid = cursor.getString(cursor.getColumnIndexOrThrow("studentUid")) ?: "",
            clientName = cursor.getString(cursor.getColumnIndexOrThrow("clientName")) ?: "",
            fromClient = cursor.getInt(cursor.getColumnIndexOrThrow("fromClient")).toBoolean(),
            text = cursor.getString(cursor.getColumnIndexOrThrow("text")) ?: "",
            sentAt = cursor.getLong(cursor.getColumnIndexOrThrow("sentAt"))
        )
    }

    // --- Notifications ---
    
    fun insertOrUpdateNotification(n: AppNotification) {
        val db = dbHelper.writableDatabase
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

    fun getNotifications(uid: String): List<AppNotification> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NOTIFICATIONS, null, "uid = ?", arrayOf(uid), null, null, "createdAt DESC")
        val list = mutableListOf<AppNotification>()
        while (cursor.moveToNext()) {
            list.add(cursorToNotification(cursor))
        }
        cursor.close()
        return list
    }

    private fun cursorToNotification(cursor: Cursor): AppNotification {
        return AppNotification(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")) ?: "",
            uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")) ?: "",
            type = cursor.getString(cursor.getColumnIndexOrThrow("type")) ?: "",
            text = cursor.getString(cursor.getColumnIndexOrThrow("text")) ?: "",
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
            read = cursor.getInt(cursor.getColumnIndexOrThrow("read")).toBoolean()
        )
    }

    // --- Sync Queue ---
    
    data class SyncItem(val id: Int, val operation: String, val entityType: String, val entityId: String, val payload: String)

    fun addToSyncQueue(operation: String, entityType: String, entityId: String, payload: String) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("operation", operation)
            put("entity_type", entityType)
            put("entity_id", entityId)
            put("payload", payload)
            put("created_at", System.currentTimeMillis())
            put("status", "PENDING")
        }
        db.insert(DatabaseHelper.TABLE_SYNC_QUEUE, null, cv)
    }

    fun getPendingSyncItems(): List<SyncItem> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_SYNC_QUEUE, null, "status = 'PENDING'", null, null, null, "created_at ASC")
        val list = mutableListOf<SyncItem>()
        while (cursor.moveToNext()) {
            list.add(SyncItem(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                operation = cursor.getString(cursor.getColumnIndexOrThrow("operation")) ?: "",
                entityType = cursor.getString(cursor.getColumnIndexOrThrow("entity_type")) ?: "",
                entityId = cursor.getString(cursor.getColumnIndexOrThrow("entity_id")) ?: "",
                payload = cursor.getString(cursor.getColumnIndexOrThrow("payload")) ?: ""
            ))
        }
        cursor.close()
        return list
    }

    fun markSyncItemComplete(id: Int) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("status", "COMPLETED")
        }
        db.update(DatabaseHelper.TABLE_SYNC_QUEUE, cv, "id = ?", arrayOf(id.toString()))
    }

    // Delete completed sync items
    fun clearCompletedSyncItems() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_SYNC_QUEUE, "status = 'COMPLETED'", null)
    }
}
