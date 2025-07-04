/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.service

import android.content.Context
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.livestreamkit.impl.model.AudienceInfoList
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomDefaultConfig
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomList
import com.netease.yunxin.kit.livestreamkit.impl.model.RequestConnectionResponse
import com.netease.yunxin.kit.livestreamkit.impl.model.StartLiveRoomParam
import kotlinx.coroutines.flow.Flow

interface HttpErrorReporter {

    /**
     * 网络错误事件
     * @property code 错误码
     * @property msg 信息
     * @property requestId 请求id
     * @constructor
     */
    data class ErrorEvent(
        val code: Int,
        val msg: String?,
        val requestId: String
    )

    fun reportHttpErrorEvent(error: ErrorEvent)

    val httpErrorEvents: Flow<ErrorEvent>
}

/**
 * 语聊房 服务端接口对应service
 */
interface LiveRoomHttpService : HttpErrorReporter {

    fun initialize(context: Context, url: String)

    fun addHeader(key: String, value: String)

    fun fetchLiveRoomList(
        type: Int,
        live: Int,
        pageNum: Int,
        pageSize: Int,
        callback:
        NetRequestCallback<LiveRoomList>
    )

    fun fetchCoLiveRooms(
        type: Int,
        liveStatus: List<Int>,
        pageNum: Int,
        pageSize: Int,
        callback:
        NetRequestCallback<LiveRoomList>
    )

    /**
     * 创建一个语聊房房间
     *
     */
    fun startLiveRoom(param: StartLiveRoomParam, callback: NetRequestCallback<LiveRoomInfo>)

    /**
     * 获取房间 信息
     */
    fun getRoomInfo(liveRecordId: Long, callback: NetRequestCallback<LiveRoomInfo>)

    /**
     * 结束语聊房房间
     */
    fun stopLiveRoom(liveRecodeId: Long, callback: NetRequestCallback<Unit>)

    /**
     * 暂停直播间
     */
    fun pauseLiveRoom(liveRecodeId: Long, notifyMessage: String?, callback: NetRequestCallback<Unit>)

    /**
     * 恢复直播间
     */
    fun resumeLiveRoom(liveRecodeId: Long, notifyMessage: String?, callback: NetRequestCallback<Unit>)

    fun getDefaultLiveInfo(callback: NetRequestCallback<LiveRoomDefaultConfig>)

    /**
     * 查询当前用户未结束的直播详情
     */
    fun getLivingRoomInfo(callback: NetRequestCallback<LiveRoomInfo>)

    fun getAudienceList(
        liveRecodeId: Long,
        page: Int,
        size: Int,
        callback: NetRequestCallback<AudienceInfoList>
    )

    fun batchReward(
        liveRecodeId: Long,
        giftId: Int,
        giftCount: Int,
        userUuids: List<String>,
        callback: NetRequestCallback<Unit>
    )

    fun realNameAuthentication(name: String, cardNo: String, callback: NetRequestCallback<Unit>)

    fun requestHostConnection(
        roomUuids: List<String>,
        timeoutSeconds: Long,
        ext: String?,
        callback: NetRequestCallback<RequestConnectionResponse>
    )

    fun cancelRequestHostConnection(
        roomUuids: List<String>,
        callback: NetRequestCallback<Unit>
    )

    fun acceptRequestHostConnection(
        roomUuid: String,
        callback: NetRequestCallback<Unit>
    )

    fun rejectRequestHostConnection(
        roomUuid: String,
        callback: NetRequestCallback<Unit>
    )

    fun disconnectHostConnection(
        callback: NetRequestCallback<Unit>
    )
}
