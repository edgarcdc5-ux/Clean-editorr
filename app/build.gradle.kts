plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets)
}

val ciDebugKeystorePath = providers.environmentVariable("CI_DEBUG_KEYSTORE").orNull
val ciDebugStorePassword = providers.environmentVariable("CLEANEDITOR_KEYSTORE_PASSWORD").orNull
val ciDebugKeyAlias = providers.environmentVariable("CLEANEDITOR_KEY_ALIAS").orNull
val ciDebugKeyPassword = providers.environmentVariable("CLEANEDITOR_KEY_PASSWORD").orNull
val ciDebugSigningValues = listOf(
    ciDebugKeystorePath,
    ciDebugStorePassword,
    ciDebugKeyAlias,
    ciDebugKeyPassword,
)
val isCiDebugSigningConfigured = ciDebugSigningValues.any { !it.isNullOrBlank() }

if (isCiDebugSigningConfigured) {
    require(ciDebugSigningValues.all { !it.isNullOrBlank() }) {
        "CI debug signing requires CI_DEBUG_KEYSTORE and all CLEANEDITOR_KEY* environment variables."
    }
}
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
        if (isCiDebugSigningConfigured) {
            val ciDebugKeystore = file(checkNotNull(ciDebugKeystorePath))
            require(ciDebugKeystore.isFile) {
                "CI debug keystore does not exist: ${ciDebugKeystore.absolutePath}"
            }

            create("ciDebug") {
                storeFile = ciDebugKeystore
                storePassword = checkNotNull(ciDebugStorePassword)
                keyAlias = checkNotNull(ciDebugKeyAlias)
                keyPassword = checkNotNull(ciDebugKeyPassword)
            }
        }
    }

    buildTypes {
        debug {
            // CI provides a stable keystore via GitHub Actions secrets.
            if (isCiDebugSigningConfigured) {
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
