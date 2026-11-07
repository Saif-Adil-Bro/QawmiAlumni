import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// google-services.json is NEVER committed. It is decoded from GitHub Secrets in CI
// (GOOGLE_SERVICES_JSON_BASE64). Apply the plugin only when the file exists so that
// local builds / static checks still work without Firebase credentials.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
} else {
    logger.warn("⚠ app/google-services.json not found — Firebase plugin skipped (local build only).")
}

// ---------------------------------------------------------------------------
// Zero-Secret-In-Repo: System.getenv() (GitHub Actions) -> local.properties fallback
// ---------------------------------------------------------------------------
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun getSecret(key: String): String {
    return System.getenv(key) ?: localProps.getProperty(key) ?: ""
}

val releaseKeystore = rootProject.file("app/release-keystore.jks")

android {
    namespace = "com.qawmialumni.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.qawmialumni.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Inject Image Hosting API Keys into BuildConfig safely
        buildConfigField("String", "FREEIMAGE_API_KEY", "\"${getSecret("FREEIMAGE_API_KEY")}\"")
        buildConfigField("String", "IMGBB_API_KEY", "\"${getSecret("IMGBB_API_KEY")}\"")

        // Keep only Bangla + English resources (strips translations pulled in by libraries)
        resourceConfigurations += listOf("en", "bn")
    }

    signingConfigs {
        create("release") {
            if (releaseKeystore.exists()) {
                storeFile = releaseKeystore
                storePassword = getSecret("KEYSTORE_PASSWORD")
                keyAlias = getSecret("KEY_ALIAS")
                keyPassword = getSecret("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (releaseKeystore.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // No applicationIdSuffix: debug & release share the package name registered
            // in Firebase (com.qawmialumni.app), so one google-services.json works for both.
            versionNameSuffix = "-debug"
        }
    }

    // ABI splits (Blueprint §3). Produces per-ABI APKs + a universal APK.
    // Only meaningful if/when native libraries are added; kept lightweight otherwise.
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
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

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.version",
                "/META-INF/*.kotlin_module",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json"
            )
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.exifinterface)

    // Compose (Material 3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Firebase (Auth + Firestore only — NO Firebase Storage)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    // Google Sign-In (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    // Room (local cache to save Firestore quota)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore (role / madrasaId / status cache)
    implementation(libs.datastore.preferences)

    // Networking — image upload API only
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging) // used only behind BuildConfig.DEBUG -> stripped by R8 in release
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Image loading
    implementation(libs.coil.compose)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
