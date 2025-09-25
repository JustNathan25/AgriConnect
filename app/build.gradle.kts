plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // ✅ apply plugin here, no version needed in module
}

android {
    namespace = "com.example.agriconnect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.agriconnect"
        minSdk = 29
        targetSdk = 36
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

    // Android GIF Drawable
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")

    // Firebase BoM for version management
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")

    implementation("com.google.firebase:firebase-firestore:25.1.1") // latest
    implementation("com.google.firebase:firebase-auth:23.1.0")    // for users


    // RecyclerView, Material
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.google.android.material:material:1.9.0")


    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.15.1")


    // CircleImageView (optional)
    implementation("de.hdodenhof:circleimageview:3.1.0")


    // UI libraries
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.14.0-alpha04")

    // Glide (with annotation processor for codegen)
    implementation(libs.glide)
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.4")

    //google map
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.libraries.places:places:3.5.0")

}
