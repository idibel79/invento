plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ma.bam.inventaire"
    compileSdk = 35

    defaultConfig {
        applicationId = "ma.bam.inventaire"
        minSdk = 24
        targetSdk = 35
        versionCode = 32
        versionName = "2.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core / lifecycle
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // CameraX
    val cameraxVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ML Kit barcode scanning (on-device, offline)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Runtime permissions helper for Compose
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Import/export .xlsx : implémentation maison (java.util.zip + SAX), voir
    // ma.bam.inventaire.util.xlsx — volontairement sans dépendance tierce (StAX absent d'Android).

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Matérialise le classeur généré par SampleInventoryFixture en un vrai fichier .xlsx,
// utile pour tester manuellement l'écran d'import sur un appareil (copier le fichier produit
// dans samples/inventaire_exemple.xlsx vers le téléphone, ou l'ouvrir depuis le sélecteur de fichiers).
tasks.register<JavaExec>("generateSampleFixture") {
    group = "verification"
    description = "Génère samples/inventaire_exemple.xlsx à partir de SampleInventoryFixture."
    dependsOn("compileDebugUnitTestKotlin")
    classpath = files(
        tasks.named("compileDebugUnitTestKotlin").map { it.outputs.files },
        tasks.named("compileDebugKotlin").map { it.outputs.files }
    ) + configurations.getByName("debugUnitTestRuntimeClasspath")
    mainClass.set("ma.bam.inventaire.testutil.SampleInventoryFixtureKt")
    args(file("$rootDir/samples/inventaire_exemple.xlsx").absolutePath)
}
