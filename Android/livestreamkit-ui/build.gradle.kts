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
    namespace = "com.netease.yunxin.kit.livestreamkit.ui"
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString() // 确保与Java版本一致
    }
}

dependencies {
    // jetpack dependencies
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // third party dependencies
    implementation("com.airbnb.android:lottie:6.3.0")
    // xkit dependencies
    api("com.netease.nimlib:neliveplayer:3.5.0")
    api("com.netease:gslb:1.1.4")
    api("com.netease.yunxin.kit.common:common-ui:1.3.9")
    api("com.netease.yunxin.kit.common:common-network:1.1.8")
    api("com.netease.yunxin.kit.common:common-image:1.2.0")
    implementation("com.netease.yunxin.kit.core:corekit:1.5.1")
    api(project(":livestreamkit"))
    api(project(":entertainment-common"))
    api(project(":beauty-faceunity"))
}
