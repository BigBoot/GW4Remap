import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val properties = gradleLocalProperties(rootDir)

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "de.bigboot.watch4payswitch"
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
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.wear:wear:1.2.0")
}