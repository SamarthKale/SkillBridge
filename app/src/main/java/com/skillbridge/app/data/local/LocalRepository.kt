package com.skillbridge.app.data.local

import android.content.Context
import android.database.Cursor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skillbridge.app.data.*

class LocalRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val gson = Gson()

    private fun String?.toList(): List<String> {
        if (this.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(this, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun Int.toBoolean() = this == 1

    fun getStudentProfiles(): List<Profile> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PROFILES, null, "role = ?", arrayOf("student"), null, null, null)
        val list = mutableListOf<Profile>()
        while (cursor.moveToNext()) {
            list.add(cursorToProfile(cursor))
        }
        cursor.close()
        return list
    }

    fun getProfile(uid: String): Profile? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PROFILES, null, "uid = ?", arrayOf(uid), null, null, null)
        var profile: Profile? = null
        if (cursor.moveToFirst()) {
            profile = cursorToProfile(cursor)
        }
        cursor.close()
        return profile
    }

    fun getProjects(): List<Opportunity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, null, null, null, null, "postedAt DESC")
        val list = mutableListOf<Opportunity>()
        while (cursor.moveToNext()) {
            list.add(cursorToProject(cursor))
        }
        cursor.close()
        return list
    }

    fun getApplications(studentUid: String): List<Application> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_APPLICATIONS, null, "studentUid = ?", arrayOf(studentUid), null, null, "appliedAt DESC")
        val list = mutableListOf<Application>()
        while (cursor.moveToNext()) {
            list.add(cursorToApplication(cursor))
        }
        cursor.close()
        return list
    }

    fun getMessages(studentUid: String): List<Message> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_MESSAGES, null, "studentUid = ?", arrayOf(studentUid), null, null, "sentAt DESC")
        val list = mutableListOf<Message>()
        while (cursor.moveToNext()) {
            list.add(cursorToMessage(cursor))
        }
        cursor.close()
        return list
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
}
