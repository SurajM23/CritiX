plugins {
    id("kotlin-kapt") // For Kotlin annotation processing
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.videomate.critix"
    compileSdk = 35 // Updated to 35

    defaultConfig {
        applicationId = "com.videomate.critix"
        minSdk = 29
        targetSdk = 35 // Updated to 35
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
    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.retrofit.v290)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)



    implementation(libs.ssp.android)
    implementation(libs.picasso)
    implementation(libs.circleimageview)
    implementation(libs.kotlinx.metadata.jvm)
    implementation(libs.calligraphy3)
    implementation(libs.viewpump)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.work.runtime.ktx)
}
