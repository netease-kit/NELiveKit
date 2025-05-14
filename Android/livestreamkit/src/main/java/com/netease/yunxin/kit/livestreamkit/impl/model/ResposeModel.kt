/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.livestreamkit.impl.model
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomBatchGiftModel
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomMemberVolumeInfo
import com.netease.yunxin.kit.roomkit.api.model.NEMemberVolumeInfo
import java.io.Serializable

class LiveRoomAnchor : Serializable {
    val userUuid: String = ""
    val userName: String? = null
    val icon: String? = null
}

data class LiveRoomLiveModel(
    val roomUuid: String, // 房间Id
    val roomName: String?, // 房间名
    val liveRecordId: Long, // 直播Id
    val userUuid: String,
    val status: Int, // 直播记录是否有效 1: 有效 -1 无效
    val liveType: Int, // 	直播状态
    val live: Int, // 直播标题
    val liveTopic: String, // 直播封面
    val cover: String?, // 	打赏总额
    var rewardTotal: Long?, // 	观众人数
    val audienceCount: Int?, // 	上麦人数
    val onSeatCount: Int?,
    var externalLiveConfig: LiveConfig?,
    var seatUserReward: List<SeatUserReward>?,
    val gameName: String? // 麦上的打赏信息){}, val roomArchiveId: kotlin.String?){}){}
) : Serializable

data class SeatUserReward(
    val userUuid: String,
    val userName: String?,
    val icon: String?,
    val seatIndex: Int,
    val rewardTotal: Int
)

data class LiveConfig(
    val pullHlsUrl: String?,
    val pullRtmpUrl: String?,
    val pullHttpUrl: String?
)

/**
 * 直播间直播信息
 */
@Keep
class LiveRoomInfo : Serializable {
    @SerializedName("anchor")
    lateinit var anchor: LiveRoomAnchor // 主播信息
    @SerializedName("live")
    lateinit var liveModel: LiveRoomLiveModel // 房间信息
    override fun toString(): String {
        return "VoiceRoomInfo(anchor=$anchor, live=$liveModel)"
    }
}

/**
 * 直播主页面列表返回值
 */
class LiveRoomList {

    var pageNum: Int = 0 // 当前页码

    var hasNextPage = false // boolean	是否有下一页

    var list: MutableList<LiveRoomInfo>? = null // 直播房间列表
}

data class Operator(
    val userUuid: String?,
    val userName: String?,
    val icon: String?
)

class AudienceInfoList : Serializable {
    val total: Int = 0
    val list: List<AudienceInfo>? = null
}

class AudienceInfo : Serializable {
    val userUuid: String = ""
    val userName: String? = null
    val icon: String? = null
}

data class AnchorRewardInfo(
    val userUuid: String, // 	用户编号
    val rewardTotal: Long // 	直播打赏总额
) : Serializable

class VoiceRoomGiftModel(
    val rewarderUserUuid: String, // 	打赏者用户编号
    val rewarderUserName: String, // 	打赏者昵称
    val memberTotal: Long, // 	房间人数
    val anchorReward: AnchorRewardInfo, // 	被打赏主播打赏信息
    val giftId: Int // 	礼物编号
)

data class VoiceRoomBatchGiftModel(
    val data: NEVoiceRoomBatchGiftModel
)

internal class VoiceRoomMemberVolumeInfo(
    private val memberVolumeInfo: NEMemberVolumeInfo
) : NEVoiceRoomMemberVolumeInfo {
    override val userUuid: String
        get() = memberVolumeInfo.userUuid
    override val volume: Int
        get() = memberVolumeInfo.volume
}

data class LiveRoomDefaultConfig(
    @SerializedName("topic")
    val topic: String?, // 主题
    @SerializedName("livePicture")
    val livePicture: String?, // 背景图
    val defaultPictures: List<String>? // 默认背景图列表
)

data class StartLiveRoomParam(
    val roomTopic: String,
    val roomName: String,
    val cover: String,
    val liveType: Int,
    val configId: Int = 0,
    val seatCount: Int = 7,
    val seatApplyMode: Int,
    val seatInviteMode: Int,
    val ext: String?
)
