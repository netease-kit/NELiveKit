/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 34
    namespace = "com.netease.yunxin.app.livestreamkit"
    defaultConfig {
        applicationId = "com.netease.yunxin.app.livestreamkit"
        minSdk = 21
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString() // 确保与Java版本一致
    }

    lint {
        disable += "IconDensities"
    }

    packagingOptions {
        jniLibs.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        jniLibs.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }
}

dependencies {
    // jetpack dependencies
    implementation("com.google.android.material:material:1.11.0")

    // xkit dependencies
//    implementation(libs.yunxin.kit.alog)
    implementation(project(":livestreamkit-ui"))
}