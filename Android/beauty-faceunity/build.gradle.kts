/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.library")
}

android {
    compileSdk = 34
    namespace = "com.netease.yunxin.kit.beauty.faceunity"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.netease.yunxin.kit:alog:1.0.9")
    implementation("com.netease.yunxin:nertc-base:5.8.15")
    implementation("com.netease.yunxin.kit.common:common:1.7.0")
    implementation("com.netease.yunxin.kit.common:common-ui:1.7.0")
    implementation(project(":entertainment-common"))
    api("com.faceunity:core_face_all:8.6.0")
    api("com.faceunity:model_face_all:8.6.0")
}
