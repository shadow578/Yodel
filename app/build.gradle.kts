@file:Suppress("UnstableApiUsage")
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.mikepenz.aboutlibraries.plugin")
}

// load signing config
val signProps = Properties()
val signPropsFile = project.file("../sign.properties")
val useSignProps = signPropsFile.exists()
if (useSignProps)
    FileInputStream(signPropsFile).use { signProps.load(it) }

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"
    namespace = "io.github.shadow578.yodel"

    defaultConfig {
        applicationId = "io.github.shadow578.yodel"
        minSdk = 24
        targetSdk = 33
        compileSdk = 33
        versionCode = 8
        versionName = "1.4.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }
    signingConfigs {
        create("from_props") {
            keyAlias = signProps.getProperty("key_alias")
            keyPassword = signProps.getProperty("key_password")
            storeFile = file("../" + signProps.getProperty("keystore_path"))
            storePassword = signProps.getProperty("keystore_password")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // use sign.properties
            if (useSignProps) {
                println("using sign.properties for release build signing")
                signingConfig = signingConfigs.getByName("from_props")
            }
        }
        getByName("debug") {
            applicationIdSuffix = ".dev"
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
            isUniversalApk = false
        }
    }
    packagingOptions.resources {
        excludes.add("META-INF/LICENSE.md")
        excludes.add("META-INF/LICENSE-notice.md")
    }
    testOptions.unitTests {
        isIncludeAndroidResources = true
    }
}
aboutLibraries {
    configPath = "aboutlibraries"
}

dependencies {
    // androidX
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation("androidx.lifecycle:lifecycle-service:2.6.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.0")

    // material design
    implementation("com.google.android.material:material:1.8.0")

    // Room
    implementation("androidx.room:room-runtime:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")

    // youtube-dl
    implementation("io.github.junkfood02.youtubedl-android:library:0.17.1")
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.17.1")
    implementation("io.github.junkfood02.youtubedl-android:aria2c:0.17.1")

    // id3v2 tagging
    implementation("com.mpatric:mp3agic:0.9.1")

    // gson
    implementation("com.google.code.gson:gson:2.9.1")

    // glide
    implementation("com.github.bumptech.glide:glide:4.14.1")
    kapt("com.github.bumptech.glide:compiler:4.14.1")

    // timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // about libraries
    implementation("com.mikepenz:aboutlibraries-core:8.9.4")
    implementation("com.mikepenz:aboutlibraries:8.9.4")

    // desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.2")

    // unit testing
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("org.robolectric:robolectric:4.8.2")
    testImplementation("io.kotest:kotest-assertions-core:5.4.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("io.kotest:kotest-assertions-core:5.4.2")

    // leakcanary (only on debug builds)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")
}

tasks.withType<Test>().all {
    // fix for AS code coverage with robolectric
    jvmArgs("-noverify", "-ea")
}
