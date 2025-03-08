plugins {
    id("com.android.application") // Android uygulama plugin'i
    id("com.google.gms.google-services") // Google servisleri için plugin
}

android {
    compileSdk = 34 // Android SDK sürümü
    namespace = "com.example.healthtrackpro" // Paket adı
    defaultConfig {
        applicationId = "com.example.healthtrackpro" // Uygulamanızın paket adı
        minSdk = 23 // Minimum SDK seviyesi
        targetSdk = 34 // Hedef SDK seviyesi
        versionCode = 1 // Uygulama versiyon kodu
        versionName = "1.0" // Uygulama versiyon adı
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // Minify işlemi devre dışı
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.5.0") // Uygulamanız için gerekli kütüphaneler
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-tasks:18.2.0")
    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation ("com.github.PhilJay:MPAndroidChart:3.1.0")
    testImplementation("junit:junit:4.13.2") // JUnit testi için
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Android test bağımlılığı
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // UI test için
    androidTestImplementation("androidx.test:core:1.5.0") // InstrumentationRegistry için gerekli kütüphane

}
