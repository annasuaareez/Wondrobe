plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wondrobe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wondrobe"
        minSdk = 24
        targetSdk = 34
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

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    //Splash
    implementation("androidx.core:core-splashscreen:1.0.1")
    //Firebase auth
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.google.firebase:firebase-appcheck:18.0.0")
    implementation("com.google.firebase:firebase-analytics:22.0.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    //Cloud Firestore
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    //Glide es una biblioteca de carga y visualización de imágenes para Android que simplifica el proceso de cargar imágenes desde diversos orígenes
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation ("androidx.gridlayout:gridlayout:1.0.0")
    //Google Material Design
    implementation("com.google.android.material:material:1.12.0")
    //SearchView
    implementation("androidx.appcompat:appcompat:1.7.0")
    //implementation("com.android.car.ui:car-ui-lib:2.6.0")
    implementation("com.google.code.gson:gson:2.10.1")
    //Java Mail
    implementation("com.sun.mail:android-mail:1.6.2")
    implementation("com.sun.mail:android-activation:1.6.2")
    //Navigation
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    //Flexbox Grid
    implementation ("androidx.gridlayout:gridlayout:1.0.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    //RSA Password
    implementation ("org.bouncycastle:bcprov-jdk15on:1.68")
    implementation ("org.bouncycastle:bcpkix-jdk15on:1.68")
    implementation ("androidx.activity:activity-ktx:1.9.0")
    //Remove Background
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}
