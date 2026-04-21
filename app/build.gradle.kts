    import java.util.Properties

    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.compose.compiler)
    }

    android {
        namespace = "com.example.teman_belajar"
        compileSdk = 36

        defaultConfig {
            applicationId = "com.example.teman_belajar"
            minSdk = 33
            targetSdk = 36
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(localPropertiesFile.inputStream())
            }

            val emuIp = localProperties.getProperty("EMULATOR_IP") ?: "\"IP must be set in local Properties\""
            val phyIp = localProperties.getProperty("PHYSICAL_DEVICE_IP") ?: "\"IP must be set in local Properties\""
             /*
                masukkin ini ke local properties
                EMULATOR_IP="http://10.0.2.2:8080/" (ini fix)
                PHYSICAL_DEVICE_IP="http://sesuaiin ip kalian/"
             */
            buildConfigField("String", "EMULATOR_IP", emuIp)
            buildConfigField("String", "PHYSICAL_DEVICE_IP", phyIp)
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
        kotlinOptions {
            jvmTarget = "11"
        }
        buildFeatures {
            viewBinding = true
            compose = true
            buildConfig = true
        }
    }

    dependencies {
        val composeBom = platform("androidx.compose:compose-bom:2026.03.00")
        implementation(composeBom)
        androidTestImplementation(composeBom)

        implementation("androidx.compose.material3:material3")
        implementation("androidx.compose.foundation:foundation")
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-tooling-preview")
        debugImplementation("androidx.compose.ui:ui-tooling")

        // UI Tests
        androidTestImplementation("androidx.compose.ui:ui-test-junit4")
        debugImplementation("androidx.compose.ui:ui-test-manifest")

        // DataStore
        implementation("androidx.datastore:datastore-preferences:1.1.1")

        // Navigation
        implementation("androidx.navigation:navigation-compose:2.8.5")

        // Optional - Add window size utils
        implementation("androidx.compose.material3.adaptive:adaptive")

        // Optional - Integration with activities
        implementation("androidx.activity:activity-compose:1.13.0")
        // Optional - Integration with ViewModels
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
        // Optional - Integration with LiveData
        implementation("androidx.compose.runtime:runtime-livedata")

        implementation("androidx.compose.material:material-icons-extended")

        // Retrofit
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.androidx.activity)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }
