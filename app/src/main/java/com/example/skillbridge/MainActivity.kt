package com.example.skillbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.skillbridge.navigation.AppNavigation
import com.example.skillbridge.ui.theme.SkillBridgeTheme
import com.example.skillbridge.screens.SplashScreen
import com.example.skillbridge.navigation.AppNavigation
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SkillBridgeTheme {
                AppNavigation()
            }
        }
    }
}