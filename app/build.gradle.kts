plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)

    id("com.google.gms.google-services")
}

android {
    namespace = "mikhail.shell.video.hosting"
    compileSdk = 35

    defaultConfig {
        applicationId = "mikhail.shell.video.hosting"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "mikhail.shell.video.hosting.HostingTestsRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-common:2.8.7")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.accompanist.permissions)
    implementation("androidx.compose.material3:material3-window-size-class-android:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout-compose-android:1.1.0")

    implementation (libs.androidx.lifecycle.viewmodel.compose)

    implementation (libs.hilt.android)
    kapt (libs.hilt.android.compiler)
    kaptAndroidTest (libs.hilt.android.compiler)
    //implementation (libs.androidx.hilt.lifecycle.viewmodel)
    kapt (libs.androidx.hilt.compiler)
    implementation (libs.androidx.hilt.navigation.compose)
    androidTestImplementation(libs.hilt.android.testing)
    testImplementation(libs.hilt.android.testing)
    testImplementation("org.robolectric:robolectric:4.9")

    implementation (libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.okhttp)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("androidx.media3:media3-exoplayer:1.0.0-beta02")
    implementation ("androidx.media3:media3-ui:1.0.0-beta02")

    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    implementation("androidx.compose.material3:material3-window-size-class-android:1.3.1")
}