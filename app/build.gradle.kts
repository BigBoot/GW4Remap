import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp").version("1.6.10-1.0.2")
}

val properties = gradleLocalProperties(rootDir)

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "de.bigboot.gw4remap"
        minSdk = 30
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

    }

    signingConfigs {
        create("release") {
            storeType = properties.getProperty("signing.release.storeType", "PKCS12")
            storeFile = rootDir.resolve(properties.getProperty("signing.release.storeFile", "keystore.p12"))
            storePassword = properties.getProperty("signing.release.storePassword", "")
            keyAlias = properties.getProperty("signing.release.keyAlias", "")
            keyPassword = properties.getProperty("signing.release.keyPassword", "")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["release"]
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.wear:wear:1.2.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.4.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0") {
        exclude("org.jetbrains.kotlin", "kotlin-reflect ")
    }
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")
}