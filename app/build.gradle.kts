import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// release 서명 정보. 저장소에 올리지 않는 keystore.properties 에서 읽는다.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val hasReleaseKey = keystoreProps.getProperty("storeFile") != null

android {
    namespace = "com.example.emergency"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.emergency"
        minSdk = 28
        targetSdk = 34
        // 업데이트할 때마다 versionCode 를 1씩 올리고 versionName 을 바꾼다 (CHANGELOG.md 참고)
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}

/**
 * 제출용 APK 만들기: ./gradlew exportReleaseApk
 * → apk/<팀명>_<앱이름>_release.apk  (이름은 gradle.properties 의 submission.* 값)
 */
tasks.register<Copy>("exportReleaseApk") {
    group = "submission"
    description = "서명된 release APK를 apk/ 폴더에 제출용 이름으로 복사한다."
    dependsOn("assembleRelease")
    doFirst {
        check(hasReleaseKey) { "keystore.properties 가 없어 release 서명을 할 수 없습니다." }
    }
    val team = providers.gradleProperty("submission.teamName").get()
    val app = providers.gradleProperty("submission.appName").get()
    from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
    into(rootProject.layout.projectDirectory.dir("apk"))
    rename { "${team}_${app}_release.apk" }
}
