plugins {
    id("com.android.application") version "8.3.2"
}

android {
    namespace = "com.telnyx.videodemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.telnyx.videodemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22")
    implementation("androidx.fragment:fragment:1.6.2")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.github.team-telnyx:telnyx-meet-android-sdk:0.3.7@aar") {
        isTransitive = true
    }
    implementation("com.github.team-telnyx:telnyx-webrtc-android:1.3.9")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.jakewharton.timber:timber:5.0.1");
    implementation("com.squareup.okhttp3:okhttp:4.9.0")


    implementation("androidx.camera:camera-camera2:1.3.3");
    implementation("androidx.camera:camera-lifecycle:1.3.3");
    implementation("androidx.camera:camera-view:1.3.3");
    implementation("com.braintreepayments.api:card:4.39.0")

}