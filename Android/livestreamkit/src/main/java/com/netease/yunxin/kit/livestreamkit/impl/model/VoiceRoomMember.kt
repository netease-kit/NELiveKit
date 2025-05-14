/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.model

import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomMember
import com.netease.yunxin.kit.livestreamkit.impl.service.MemberPropertyConstants
import com.netease.yunxin.kit.roomkit.api.NERoomMember

internal class VoiceRoomMember(
    private val roomMember: NERoomMember
) : NEVoiceRoomMember {
    override val account: String
        get() = roomMember.uuid

    override val name: String
        get() = roomMember.name

    override val role: String
        get() = roomMember.role.name

    override val isAudioOn: Boolean
        get() = roomMember.isAudioOn

    override val isAudioBanned: Boolean
        get() = roomMember.properties[MemberPropertyConstants.CAN_OPEN_MIC_KEY] == MemberPropertyConstants.CAN_OPEN_MIC_VALUE_NO

    override val avatar: String?
        get() = roomMember.avatar

    override val initialProperties: Map<String, String>
        get() = roomMember.properties

    override fun toString(): String {
        return "VoiceRoomMember(roomMember=$roomMember, account='$account', name='$name', role='$role', isAudioOn=$isAudioOn, isAudioBanned=$isAudioBanned, avatar=$avatar，initialProperties=$initialProperties)"
    }

    // override val isInRtcChannel: Boolean
    //     get() = roomMember.isInRtcChannel
}
