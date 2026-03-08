
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.github.shanfishapp.pureyunhu"
    compileSdk = 36  // 简化写法

    defaultConfig {
        applicationId = "io.github.shanfishapp.pureyunhu"
        minSdk = 23
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.protobuf.kotlin.lite)  // protobuf Kotlin lite 依赖
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.androidx.compose.foundation.layout)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("me.zhanghai.compose.preference:preference:2.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.10.0")
    // WebView Compose 依赖
    implementation("androidx.compose.ui:ui-viewbinding:1.6.8")
    implementation("androidx.webkit:webkit:1.10.0")
    // 如果你需要标准 protobuf 而不是 lite 版本，使用这个
    // implementation(libs.protobuf.javalite)
}