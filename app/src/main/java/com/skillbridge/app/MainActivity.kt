package com.skillbridge.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.skillbridge.app.data.local.DatabaseHelper
import com.skillbridge.app.navigation.AppNavigation
import com.skillbridge.app.ui.theme.SkillBridgeTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        com.maptiler.maptilersdk.MTConfig.apiKey = BuildConfig.MAPTILER_API_KEY

        // Store a simple launch log in the local SQLite database
        val dbHelper = DatabaseHelper(this)
        dbHelper.insertLog("App Launched")
        
        // Print the logs to logcat to verify it's working
        val logs = dbHelper.getAllLogs()
        Log.d("LocalDB", "Total local logs stored: ${logs.size}")
        logs.forEach { log -> 
            Log.d("LocalDB", "Log entry: $log") 
        }

        com.skillbridge.app.data.Repo.init(this)
        
        // Trigger SQLite synchronization for the current user
        val uid = com.skillbridge.app.auth.AuthManager.uid()
        if (uid.isNotEmpty()) {
            lifecycleScope.launch {
                com.skillbridge.app.data.sync.FirebaseToSQLiteSync(this@MainActivity).syncAll(uid)
            }
        }

        setContent {
            SkillBridgeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}
