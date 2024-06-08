plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("maven-publish")
}

android {
    namespace = "com.kinestex.kinestexsdkkotlin"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"]) // 'release' component should be created by the Android library plugin

                groupId = "com.github.KinesteX"
                artifactId = "kinestexsdkkotlin"
                version = "1.0.5"

                pom {
                    name.set("KinesteX SDK Kotlin")
                    description.set("A Kotlin SDK for KinesteX")
                    url.set("https://github.com/KinesteX/KinesteX-SDK-Kotlin")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("kinestex")
                            name.set("KinesteX Team")
                            email.set("support@kinestex.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/KinesteX/KinesteX-SDK-Kotlin.git")
                        developerConnection.set("scm:git:ssh://github.com:KinesteX/KinesteX-SDK-Kotlin.git")
                        url.set("https://github.com/KinesteX/KinesteX-SDK-Kotlin")
                    }
                }
            }
        }
        repositories {
            maven {
                url = uri("https://jitpack.io")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
