import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.skillbridge.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.skillbridge.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        // Load properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream -> localProperties.load(stream) }
        }
        buildConfigField("String", "MAPTILER_API_KEY", "\"${localProperties.getProperty("MAPTILER_API_KEY", "")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // --------------------------------------------------
    // Jetpack Compose
    // --------------------------------------------------

    val composeBom =
        platform("androidx.compose:compose-bom:2024.09.00")

    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    implementation("androidx.activity:activity-compose:1.9.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")


    // --------------------------------------------------
    // Firebase
    // --------------------------------------------------

    implementation(
        platform("com.google.firebase:firebase-bom:33.5.1")
    )

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Cloud Functions for secure AI backend proxy
    implementation("com.google.firebase:firebase-functions")


    // --------------------------------------------------
    // Location
    // --------------------------------------------------

    implementation("com.google.android.gms:play-services-location:21.0.1")

    // MapTiler
    implementation("com.maptiler:maptiler-sdk-kotlin:2.0.0")

    // Gson for SQLite JSON serialization
    implementation("com.google.code.gson:gson:2.11.0")

    // Unit testing
    testImplementation("junit:junit:4.13.2")
}