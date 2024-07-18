plugins {
    id("com.android.application")
    id("com.google.gms.google-services") version "4.4.2" apply true
}

android {
    namespace = "vn.edu.tdc.selling_medicine_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.edu.tdc.selling_medicine_app"
        minSdk = 24
        targetSdk = 33
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
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.android.material:material:1.6.0")
    implementation ("com.google.firebase:firebase-firestore:24.0.0")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.firebase:firebase-database")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.google.firebase:firebase-auth:22.0.0")
    //implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    //implementation ("com.google.zxing:core:3.3.0")

}