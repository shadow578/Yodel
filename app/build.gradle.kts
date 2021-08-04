plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "io.github.shadow578.yodel"
        minSdk = 23
        targetSdk = 30
        compileSdk = 30
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += setOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        //TODO reverted back to JDK 8 until the next version of AS is released in stable
        // https://issuetracker.google.com/issues/180946610?pli=1
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        viewBinding = true
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }
}
aboutLibraries{
    configPath = "aboutlibraries"
}

dependencies {
    // androidX
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.lifecycle:lifecycle-service:2.3.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.1.1")

    // material design
    implementation("com.google.android.material:material:1.4.0")

    // Room
    implementation("androidx.room:room-runtime:2.3.0")
    kapt("androidx.room:room-compiler:2.3.0")

    // youtube-dl
    implementation("com.github.yausername.youtubedl-android:library:0.12.4")
    implementation("com.github.yausername.youtubedl-android:ffmpeg:0.12.4")

    // id3v2 tagging
    implementation("com.mpatric:mp3agic:0.9.1")

    // gson
    implementation("com.google.code.gson:gson:2.8.7")

    // glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    // about libraries
    implementation("com.mikepenz:aboutlibraries-core:8.9.1")
    implementation("com.mikepenz:aboutlibraries:8.9.1")

    // desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("com.github.npathai:hamcrest-optional:2.0.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("com.github.npathai:hamcrest-optional:2.0.0")
}
