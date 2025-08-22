// ✅ CI/CD에서 서명 파일을 다루기 위한 import 추가
import java.io.File
import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.apple10ocr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.apple10ocr"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // ✅ GitHub Actions의 Secret을 사용해 릴리즈 서명을 설정하는 부분
    signingConfigs {
        create("release") {
            val keyStoreBase64 = System.getenv("SIGNING_KEY_STORE_BASE64")
            if (keyStoreBase64 != null) {
                // Base64로 인코딩된 키스토어를 디코딩하여 임시 파일로 생성
                val decodedKeyStore = Base64.getDecoder().decode(keyStoreBase64)
                val keyStoreTempFile = File.createTempFile("keystore", ".jks")
                keyStoreTempFile.writeBytes(decodedKeyStore)

                storeFile = keyStoreTempFile
                storePassword = System.getenv("SIGNING_KEY_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ✅ 위에서 만든 'release' 서명 설정을 릴리즈 빌드에 적용
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    // 컴파일러 옵션 추가 (호환성을 위해 권장)
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
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-korean:16.0.1")
}
