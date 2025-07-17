/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.api.model

import java.io.Serializable

/**
 * 房间信息
 * @property anchor 主播信息
 * @property liveModel 直播模式
 * @constructor
 */
data class NELiveStreamRoomInfo(
    val anchor: NELiveRoomAnchor,
    val liveModel: NELiveRoomLiveModel
) : Serializable

/**
 * 主播信息
 * @property account 账号
 * @property nick 昵称
 * @property avatar 头像
 * @constructor
 */
data class NELiveRoomAnchor(
    val account: String,
    val nick: String?,
    val avatar: String?
) : Serializable

/**
 * 直播模式
 * @property roomUuid 房间Id
 * @property roomName 房间名
 * @property liveRecordId 直播Id
 * @property userUuId String
 * @property status 直播记录是否有效 1: 有效 -1 无效
 * @property liveType Int
 * @property live 直播状态
 * @property liveTopic 直播标题
 * @property cover 直播封面
 * @property rewardTotal 打赏总额
 * @property audienceCount 观众人数
 * @property onSeatCount 上麦人数
 * @property liveConfig 拉流配置
 * @property seatUserReward 麦上的打赏信息
 * @constructor
 */
data class NELiveRoomLiveModel(
    val roomUuid: String,
    val roomName: String?,
    val liveRecordId: Long,
    val userUuId: String,
    val status: Int,
    val liveType: Int,
    val live: Int,
    val liveTopic: String,
    val cover: String?,
    var rewardTotal: Long?,
    val audienceCount: Int?,
    val onSeatCount: Int?,
    var liveConfig: NELiveConfig?,
    var connectionStatus: Int? = 0, // 0 空闲 1 申请中 2 已接受 3 已拒绝 4 已取消 6 已断开 7 已连线 8 已超时
    var seatUserReward: List<NELiveRoomBatchSeatUserReward>?,
    val gameName: String?
) : Serializable

/**
 * 打赏详情
 * @property userUuid 麦上用户uuid
 * @property userName 麦上用户名称
 * @property icon 麦上用户头像
 * @property seatIndex 麦位
 * @property rewardTotal 打赏
 */
data class NELiveRoomBatchSeatUserReward(
    val userUuid: String,
    val userName: String?,
    val icon: String?,
    val seatIndex: Int,
    val rewardTotal: Int
) : Serializable

/**
 * 打赏用户
 * @property userUuid 用户uuid
 * @property userName 用户名称
 * @property icon 用户头像
 */
data class NELiveRoomBatchRewardTarget(
    val userUuid: String,
    val userName: String?,
    val icon: String?
)

data class NELiveConfig(
    val pullHlsUrl: String?,
    val pullRtmpUrl: String?,
    val pullHttpUrl: String?
) : Serializable
