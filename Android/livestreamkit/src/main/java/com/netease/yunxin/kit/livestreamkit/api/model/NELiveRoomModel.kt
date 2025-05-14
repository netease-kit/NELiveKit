/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.api.model

data class NEAudienceInfoList(
    val total: Int = 0,
    val list: List<NEAudienceInfo>? = null
)

data class NEAudienceInfo(
    val userUuid: String,
    var userName: String?,
    var icon: String?
)
