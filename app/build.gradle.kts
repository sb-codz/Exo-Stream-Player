plugins {
    id("com.android.application")
}

android {
    namespace = "com.venomdino.exonetworkstreamer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.venomdino.exonetworkstreamer"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 12
        versionName = "2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("androidx.media3:media3-exoplayer:1.1.1")
    implementation ("androidx.media3:media3-exoplayer-dash:1.1.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.1.1")
    implementation ("androidx.media3:media3-ui:1.1.1")

    implementation ("com.google.android.play:app-update:2.1.0")
}