/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 34
    namespace = "com.netease.yunxin.kit.voiceroomkit"
    buildFeatures {
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString() // 确保与Java版本一致
    }
}

dependencies {
    // androidx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("androidx.core:core-ktx:1.7.0")
    api("com.google.code.gson:gson:2.10.1")

    // xkit
    implementation("com.netease.yunxin.kit.common:common:1.3.3")
    implementation("com.netease.yunxin.kit.common:common-network:1.1.8")
    api("com.netease.yunxin.kit.room:roomkit:1.38.0")
    api("com.netease.yunxin.kit:alog:1.1.0")
    implementation("com.netease.yunxin.kit.core:corekit:1.4.7")

}