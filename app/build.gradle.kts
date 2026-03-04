import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application.gradle)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    id ("com.google.devtools.ksp")
    id ("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
    alias(libs.plugins.firebase.performance)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "mikhail.shell.video.hosting"
    compileSdk = 36
    defaultConfig {
        applicationId = "mikhail.shell.video.hosting"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "3.0.0"
        testInstrumentationRunner = "mikhail.shell.video.hosting.VideoHostingTestsRunner"
    }
    signingConfigs {
        create("staging") {
            storeFile = file(project.findProperty("hosting.keystore.path").toString())
            storePassword = project.findProperty("hosting.keystore.password").toString()
            keyAlias = project.findProperty("hosting.key.alias").toString()
            keyPassword = project.findProperty("hosting.key.password").toString()
        }
        create("release") {
            storeFile = file(project.findProperty("hosting.keystore.path").toString())
            storePassword = project.findProperty("hosting.keystore.password").toString()
            keyAlias = project.findProperty("hosting.key.alias").toString()
            keyPassword = project.findProperty("hosting.key.password").toString()
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.2/api/v2\"")
            signingConfig = signingConfigs.getByName("debug")
        }
        create("staging") {
            isDebuggable = true
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"https://trendy-app.ru/api\"")
            signingConfig = signingConfigs.getByName("staging")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"https://trendy-app.ru/api\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-XXLanguage:+WhenGuards")
        freeCompilerArgs.add("-Xcontext-parameters")
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.rules)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.window)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)
    testImplementation(libs.junit)
    implementation(libs.kotlinx.datetime)

    //androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.core)

    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material3.window.size.klass.android)
    implementation(libs.androidx.constraintlayout.compose.android)
    implementation(libs.androidx.appcompat)

    implementation (libs.androidx.lifecycle.viewmodel.compose)

    implementation (libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp (libs.androidx.hilt.compiler)
    implementation (libs.androidx.hilt.navigation.compose)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.android.testing)

    implementation (libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    implementation(libs.androidx.media3.exoplayer)
    implementation (libs.androidx.media3.ui)
    implementation (libs.androidx.media3.ui.compose)
    implementation (libs.androidx.media3.ui.compose.material3)

    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)

    implementation(libs.androidx.datastore)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Source: https://mvnrepository.com/artifact/com.google.firebase/firebase-perf
    implementation("com.google.firebase:firebase-perf:22.0.4")
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}