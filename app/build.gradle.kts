import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example"
    compileSdk { version = release(36) { minorApiLevel = 1 } }

    defaultConfig {
        applicationId = "com.example.familycallapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }

    /*
     * CUSTOM KEYSTORE SIGNING
     *
     * GitHub Actions restores:
     * app/facetime-upload.keystore
     *
     * The same keystore is used for the debug APK,
     * so the SHA-1 remains consistent with Firebase.
     */
    signingConfigs {
        getByName("debug") {
            val customKeystore = file(
                "${project.projectDir}/facetime-upload.keystore"
            )

            if (!customKeystore.exists()) {
                throw GradleException(
                    "Custom keystore not found: ${customKeystore.absolutePath}"
                )
            }

            storeFile = customKeystore

            storePassword =
                System.getenv("ANDROID_KEYSTORE_PASSWORD")
                    ?: "facetime123456"

            keyAlias =
                System.getenv("ANDROID_KEY_ALIAS")
                    ?: "facetime"

            keyPassword =
                System.getenv("ANDROID_KEY_PASSWORD")
                    ?: "facetime123456"
        }

        create("release") {
            val keystorePath =
                System.getenv("KEYSTORE_PATH")
                    ?: "${project.projectDir}/facetime-upload.keystore"

            val releaseKeystore = file(keystorePath)

            if (!releaseKeystore.exists()) {
                throw GradleException(
                    "Release keystore not found: ${releaseKeystore.absolutePath}"
                )
            }

            storeFile = releaseKeystore

            storePassword =
                System.getenv("ANDROID_KEYSTORE_PASSWORD")
                    ?: "facetime123456"

            keyAlias =
                System.getenv("ANDROID_KEY_ALIAS")
                    ?: "facetime"

            keyPassword =
                System.getenv("ANDROID_KEY_PASSWORD")
                    ?: "facetime123456"
        }
    }

    buildTypes {
        release {
            isCrunchPngs = true
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/*.version",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/MANIFEST.MF",
                "**/module-info.class"
            )
        }

        jniLibs {
            useLegacyPackaging = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
    ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices {
    missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)

    implementation(libs.firebase.ai)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)

    // Firebase Auth and Google Sign-In via Credential Manager
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    implementation(libs.agora.rtc)
    implementation(libs.firebase.appcheck.recaptcha)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit)

    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
}
