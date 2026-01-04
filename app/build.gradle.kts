plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "lat.pam.ydjob"
    compileSdk = 36

    defaultConfig {
        applicationId = "lat.pam.ydjob"
        minSdk = 27
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))

    // Firebase Features
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    // Cloudinary (Penyebab SoLoader lama biasanya dari sini)
    implementation("com.cloudinary:cloudinary-android:2.5.0")

    // Glide (Sudah dirapikan, duplikat dihapus)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.16.0")
}

// --- FIX SOLOADER 64-BIT ERROR ---
configurations.all {
    resolutionStrategy {
        // Memaksa project menggunakan SoLoader versi aman (0.10.5+)
        // meskipun library lain meminta versi lama.
        force("com.facebook.soloader:soloader:0.10.5")
    }
}