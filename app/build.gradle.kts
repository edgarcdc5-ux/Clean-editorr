plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets)
}

val ciDebugKeystore = rootProject.file("app/ci-debug.keystore")
val ciVersionCode = System.getenv("GITHUB_RUN_NUMBER")
    ?.toIntOrNull()
    ?.takeIf { it > 0 }
    ?: 1

android {
    namespace = "com.cleaneditor.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cleaneditor.app"
        minSdk = 24
        targetSdk = 36
        versionCode = ciVersionCode
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (ciDebugKeystore.exists()) {
            create("ciDebug") {
                storeFile = ciDebugKeystore
                storePassword = "cleaneditor-debug"
                keyAlias = "cleaneditordebug"
                keyPassword = "cleaneditor-debug"
            }
        }
    }

    buildTypes {
        debug {
            // CI provides a stable debug keystore so APKs can be installed as updates.
            if (ciDebugKeystore.exists()) {
                signingConfig = signingConfigs.getByName("ciDebug")
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
}
