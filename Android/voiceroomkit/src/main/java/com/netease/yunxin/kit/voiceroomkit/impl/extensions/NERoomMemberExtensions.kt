/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.voiceroomkit.impl.extensions

import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.voiceroomkit.impl.service.MemberPropertyConstants

fun NERoomMember.isAudioBanned(): Boolean {
    return properties[MemberPropertyConstants.CAN_OPEN_MIC_KEY] == MemberPropertyConstants.CAN_OPEN_MIC_VALUE_NO
}
