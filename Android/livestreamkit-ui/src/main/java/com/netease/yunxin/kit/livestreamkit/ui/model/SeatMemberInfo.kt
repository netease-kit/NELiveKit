/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.ui.model

import com.netease.yunxin.kit.livestreamkit.ui.view.SeatView
import java.io.Serializable
import java.util.*

class SeatMemberInfo(
    /**
     * 麦位信息
     */
    val seatInfo: SeatView.SeatInfo,
    /**
     * 用户rtc信息
     */
//    var avRoomUser: AvRoomUser? = null,

    /**
     * 是否是自己
     */
    var isSelf: Boolean = false
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SeatMemberInfo?
        return seatInfo.uuid == that?.seatInfo?.uuid
    }

    override fun hashCode(): Int {
        return Objects.hash(seatInfo.uuid)
    }
}
