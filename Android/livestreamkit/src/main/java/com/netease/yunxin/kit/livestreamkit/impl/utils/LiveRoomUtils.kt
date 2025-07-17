/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.utils

import android.text.TextUtils
import com.google.gson.JsonObject
import com.netease.yunxin.kit.livestreamkit.api.NELiveRoomRole
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveConfig
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomAnchor
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomBatchSeatUserReward
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomList
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomLiveModel
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomSeatRequestItem
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveStreamRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomList
import com.netease.yunxin.kit.livestreamkit.impl.model.SeatUserReward
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.service.NESeatRequestItem

internal object LiveRoomUtils {

    fun getLocalUuid(): String? {
        return NELiveStreamKit.getInstance().localMember?.uuid
    }

    fun getLocalRoomUuid(): String? {
        return NELiveStreamKit.getInstance().getCurrentRoomInfo()?.liveModel?.roomUuid
    }

    fun isLocal(uuid: String?): Boolean {
        return NELiveStreamKit.getInstance().localMember != null &&
            TextUtils.equals(NELiveStreamKit.getInstance().localMember!!.uuid, uuid)
    }

    fun isHost(uuid: String?): Boolean {
        val member: NERoomMember =
            getMember(uuid)
                ?: return false
        return TextUtils.equals(member.role.name, NELiveRoomRole.HOST.value)
    }

    fun getMember(uuid: String?): NERoomMember? {
        val allMemberList = NELiveStreamKit.getInstance().allMemberList
        for (i in allMemberList.indices) {
            val member = allMemberList[i]
            if (TextUtils.equals(member.uuid, uuid)) {
                return member
            }
        }
        return null
    }

    fun liveRoomInfo2NELiveRoomInfo(liveRoomInfo: LiveRoomInfo): NELiveStreamRoomInfo {
        return NELiveStreamRoomInfo(
            NELiveRoomAnchor(
                liveRoomInfo.anchor.userUuid,
                liveRoomInfo.anchor.userName,
                liveRoomInfo.anchor.icon
            ),
            NELiveRoomLiveModel(
                liveRoomInfo.liveModel.roomUuid,
                liveRoomInfo.liveModel.roomName,
                liveRoomInfo.liveModel.liveRecordId,
                liveRoomInfo.liveModel.userUuid,
                liveRoomInfo.liveModel.status,
                liveRoomInfo.liveModel.liveType,
                liveRoomInfo.liveModel.live,
                liveRoomInfo.liveModel.liveTopic,
                liveRoomInfo.liveModel.cover,
                liveRoomInfo.liveModel.rewardTotal,
                liveRoomInfo.liveModel.audienceCount,
                liveRoomInfo.liveModel.onSeatCount,
                liveRoomInfo.liveModel.externalLiveConfig?.let {
                    NELiveConfig(
                        liveRoomInfo.liveModel.externalLiveConfig?.pullHlsUrl,
                        liveRoomInfo.liveModel.externalLiveConfig?.pullRtmpUrl,
                        liveRoomInfo.liveModel.externalLiveConfig?.pullHlsUrl
                    )
                },
                liveRoomInfo.liveModel.connectionStatus,
                liveRoomInfo.liveModel.seatUserReward?.map { seatUserReward2NESeatUserReward(it) },
                liveRoomInfo.liveModel.gameName
            )
        )
    }

    private fun seatUserReward2NESeatUserReward(seatUserReward: SeatUserReward): NELiveRoomBatchSeatUserReward {
        return NELiveRoomBatchSeatUserReward(
            seatUserReward.userUuid,
            seatUserReward.userName,
            seatUserReward.icon,
            seatUserReward.seatIndex,
            seatUserReward.rewardTotal
        )
    }

    fun liveRoomList2NELiveRoomList(liveRoomList: LiveRoomList): NELiveRoomList {
        return NELiveRoomList(
            liveRoomList.pageNum,
            liveRoomList.hasNextPage,
            liveRoomList.list?.map {
                liveRoomInfo2NELiveRoomInfo(it)
            }
        )
    }

    fun seatRequestItem2NELiveRoomSeatRequestItem(seatRequestItem: NESeatRequestItem): NELiveRoomSeatRequestItem {
        return NELiveRoomSeatRequestItem(
            seatRequestItem.index,
            seatRequestItem.user,
            seatRequestItem.userName,
            seatRequestItem.icon
        )
    }

    fun getType(json: String): Int? {
        val jsonObject: JsonObject = GsonUtils.fromJson(
            json,
            JsonObject::class.java
        )
        return jsonObject["type"]?.asInt
    }

    fun getData(json: String): String? {
        val jsonObject: JsonObject = GsonUtils.fromJson(
            json,
            JsonObject::class.java
        )
        return jsonObject["data"]?.toString()
    }
}

/**
 * 操作者
 * @property userUuid 用户id
 * @property userName 用户名
 * @property icon 头像
 * @constructor
 */
data class NEOperator(
    val userUuid: String?,
    val userName: String?,
    val icon: String?
)
