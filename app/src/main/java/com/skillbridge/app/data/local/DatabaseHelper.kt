package com.skillbridge.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "skillbridge.db"
        private const val DATABASE_VERSION = 2
        private const val TAG = "DatabaseHelper"

        // Profiles Table
        const val TABLE_PROFILES = "profiles"
        // Projects Table
        const val TABLE_PROJECTS = "projects"
        // Applications Table
        const val TABLE_APPLICATIONS = "applications"
        // Messages Table
        const val TABLE_MESSAGES = "messages"
        // Notifications Table
        const val TABLE_NOTIFICATIONS = "notifications"
        // Sync Queue Table
        const val TABLE_SYNC_QUEUE = "sync_queue"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create profiles table
        db.execSQL("""
            CREATE TABLE $TABLE_PROFILES (
                uid TEXT PRIMARY KEY,
                role TEXT,
                name TEXT,
                email TEXT,
                phone TEXT,
                location TEXT,
                locationEnabled INTEGER,
                latitude REAL,
                longitude REAL,
                locationUpdatedAt INTEGER,
                photoUrl TEXT,
                degree TEXT,
                college TEXT,
                branch TEXT,
                graduationYear INTEGER,
                skills TEXT,
                experience TEXT,
                bio TEXT,
                resumeUrl TEXT,
                companyName TEXT,
                companyType TEXT,
                companyDescription TEXT,
                cardName TEXT,
                cardNumber TEXT,
                cardExpiry TEXT,
                cardCvv TEXT,
                acceptedTerms INTEGER,
                createdAt INTEGER,
                updatedAt INTEGER
            )
        """)

        // Create projects table
        db.execSQL("""
            CREATE TABLE $TABLE_PROJECTS (
                id TEXT PRIMARY KEY,
                title TEXT,
                company TEXT,
                category TEXT,
                budget TEXT,
                workArrangement TEXT,
                location TEXT,
                latitude REAL,
                longitude REAL,
                maxDistanceKm INTEGER,
                remote INTEGER,
                skills TEXT,
                description TEXT,
                responsibilities TEXT,
                requirements TEXT,
                experienceRequired TEXT,
                deadline TEXT,
                postedByUid TEXT,
                postedByName TEXT,
                postedAt INTEGER
            )
        """)
        
        // Indexes for querying projects
        db.execSQL("CREATE INDEX idx_projects_postedByUid ON $TABLE_PROJECTS(postedByUid)")

        // Create applications table
        db.execSQL("""
            CREATE TABLE $TABLE_APPLICATIONS (
                id TEXT PRIMARY KEY,
                projectId TEXT,
                projectTitle TEXT,
                company TEXT,
                budget TEXT,
                studentUid TEXT,
                studentName TEXT,
                intro TEXT,
                resumeUrl TEXT,
                status TEXT,
                appliedAt INTEGER
            )
        """)

        // Indexes for querying applications
        db.execSQL("CREATE INDEX idx_applications_studentUid ON $TABLE_APPLICATIONS(studentUid)")
        db.execSQL("CREATE INDEX idx_applications_projectId ON $TABLE_APPLICATIONS(projectId)")

        // Create messages table
        db.execSQL("""
            CREATE TABLE $TABLE_MESSAGES (
                id TEXT PRIMARY KEY,
                applicationId TEXT,
                projectTitle TEXT,
                company TEXT,
                studentUid TEXT,
                clientName TEXT,
                fromClient INTEGER,
                text TEXT,
                sentAt INTEGER
            )
        """)

        // Indexes for querying messages
        db.execSQL("CREATE INDEX idx_messages_studentUid ON $TABLE_MESSAGES(studentUid)")
        db.execSQL("CREATE INDEX idx_messages_applicationId ON $TABLE_MESSAGES(applicationId)")

        // Create notifications table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTIFICATIONS (
                id TEXT PRIMARY KEY,
                uid TEXT,
                type TEXT,
                text TEXT,
                createdAt INTEGER,
                read INTEGER
            )
        """)
        
        db.execSQL("CREATE INDEX idx_notifications_uid ON $TABLE_NOTIFICATIONS(uid)")

        // Create sync queue table
        db.execSQL("""
            CREATE TABLE $TABLE_SYNC_QUEUE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                operation TEXT,
                entity_type TEXT,
                entity_id TEXT,
                payload TEXT,
                created_at INTEGER,
                status TEXT
            )
        """)
        
        // Logs table for MainActivity
        db.execSQL("""
            CREATE TABLE logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                message TEXT,
                timestamp INTEGER
            )
        """)
        Log.d(TAG, "Database created with tables")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_APPLICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SYNC_QUEUE")
        db.execSQL("DROP TABLE IF EXISTS logs")
        onCreate(db)
    }

    // For backwards compatibility with MainActivity
    fun insertLog(message: String) {
        val db = writableDatabase
        val cv = android.content.ContentValues().apply {
            put("message", message)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("logs", null, cv)
    }

    fun getAllLogs(): List<String> {
        val db = readableDatabase
        val cursor = db.query("logs", null, null, null, null, null, null)
        val logs = mutableListOf<String>()
        while (cursor.moveToNext()) {
            logs.add(cursor.getString(cursor.getColumnIndexOrThrow("message")))
        }
        cursor.close()
        return logs
    }
}
