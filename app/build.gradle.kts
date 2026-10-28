import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

// Clé de publication (Play Store) : keystore.properties à la racine, jamais commité (voir keystore.properties.example).
// Sans ce fichier, la release reste signée avec la clé de debug (installable par-dessus le debug, données conservées).
val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.isFile) keystorePropertiesFile.inputStream().use { load(it) }
}

android {
    namespace = "ma.bam.inventaire"
    compileSdk = 35

    defaultConfig {
        applicationId = "ma.bam.inventaire"
        minSdk = 24
        targetSdk = 35
        versionCode = 172
        versionName = "2.6.105"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.isFile) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
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

    // Schémas Room exportés (voir ksp.room.schemaLocation), utilisés par les tests de migration.
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
        checkDependencies = false
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

ktlint {
    baseline.set(file("config/ktlint/baseline.xml"))
    filter {
        exclude { it.file.path.contains("${File.separator}build${File.separator}") }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("config/detekt/baseline.xml")
}

dependencies {
    // Core / lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit barcode scanning (on-device, offline)
    implementation(libs.mlkit.barcode.scanning)

    // Runtime permissions helper for Compose
    implementation(libs.accompanist.permissions)

    // Import/export .xlsx : implémentation maison (java.util.zip + SAX), voir
    // ma.bam.inventaire.util.xlsx — volontairement sans dépendance tierce (StAX absent d'Android).

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
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
