    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.google.gms.google.services)

    }

    android {
        namespace = "com.nibm.healthassistance"
        compileSdk = 35

        defaultConfig {
            applicationId = "com.nibm.healthassistance"
            minSdk = 26
            targetSdk = 33
            versionCode = 1
            versionName = "1.0"
            vectorDrawables.useSupportLibrary = true

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
        buildFeatures {
            viewBinding = true
        }
    }

    dependencies {

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.constraintlayout)
        implementation(libs.lifecycle.livedata.ktx)
        implementation(libs.lifecycle.viewmodel.ktx)
        implementation(libs.navigation.fragment)
        implementation(libs.navigation.ui)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation(libs.mpandroidchart)
        implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
        implementation(platform(libs.firebase.bom))
        implementation ("com.google.firebase:firebase-database")
        implementation ("com.google.firebase:firebase-firestore")
        implementation ("com.jakewharton.threetenabp:threetenabp:1.4.6")


    }


