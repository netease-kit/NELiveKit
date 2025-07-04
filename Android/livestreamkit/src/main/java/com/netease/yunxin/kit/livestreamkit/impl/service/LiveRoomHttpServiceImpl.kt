/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.service

import android.content.Context
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.common.network.Request
import com.netease.yunxin.kit.livestreamkit.impl.model.AudienceInfoList
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomDefaultConfig
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomList
import com.netease.yunxin.kit.livestreamkit.impl.model.RequestConnectionResponse
import com.netease.yunxin.kit.livestreamkit.impl.model.StartLiveRoomParam
import com.netease.yunxin.kit.livestreamkit.impl.repository.LiveRoomRepository
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomLog
import com.netease.yunxin.kit.roomkit.api.NEErrorCode
import com.netease.yunxin.kit.roomkit.api.NEErrorMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object LiveRoomHttpServiceImpl : LiveRoomHttpService {

    private const val TAG = "VoiceRoomHttpServiceImpl"

    private var liveRoomRepository = LiveRoomRepository()

    private var liveRoomScope: CoroutineScope? = null

    init {
        liveRoomScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun initialize(context: Context, url: String) {
        liveRoomRepository.initialize(context, url)
    }

    override fun addHeader(key: String, value: String) {
        liveRoomRepository.addHeader(key, value)
    }

    override fun fetchLiveRoomList(
        type: Int,
        live: Int,
        pageNum: Int,
        pageSize: Int,
        callback: NetRequestCallback<LiveRoomList>
    ) {
        liveRoomScope?.launch {
            Request.request(
                {
                    liveRoomRepository.fetchLiveRoomList(type, live, pageNum, pageSize)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun fetchCoLiveRooms(
        type: Int,
        liveStatus: List<Int>,
        pageNum: Int,
        pageSize: Int,
        callback: NetRequestCallback<LiveRoomList>
    ) {
        liveRoomScope?.launch {
            Request.request(
                {
                    liveRoomRepository.fetchCoLiveRooms(type, liveStatus, pageNum, pageSize)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    /**
     *  创建房间
     *
     */
    override fun startLiveRoom(
        param: StartLiveRoomParam,
        callback: NetRequestCallback<LiveRoomInfo>
    ) {
        liveRoomScope?.launch {
            Request.request(
                {
                    liveRoomRepository.createLiveRoom(
                        param.roomTopic,
                        param.cover,
                        param.liveType,
                        param.configId,
                        param.roomName,
                        param.seatCount,
                        param.seatApplyMode,
                        param.seatInviteMode
                    )
                },
                success = {
                    it?.let {
                        callback.success(it)
                    }
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    /**
     * 获取房间 信息
     */
    override fun getRoomInfo(liveRecordId: Long, callback: NetRequestCallback<LiveRoomInfo>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.getLiveRoomInfo(liveRecordId) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    /**
     * 结束 语聊房房间
     */
    override fun stopLiveRoom(liveRecodeId: Long, callback: NetRequestCallback<Unit>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.endLiveRoom(liveRecodeId) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    /**
     * 暂停房间
     */
    override fun pauseLiveRoom(liveRecodeId: Long, notifyMessage: String?, callback: NetRequestCallback<Unit>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.pauseLiveRoom(liveRecodeId, notifyMessage) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    /**
     * 暂停房间
     */
    override fun resumeLiveRoom(liveRecodeId: Long, notifyMessage: String?, callback: NetRequestCallback<Unit>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.resumeLiveRoom(liveRecodeId, notifyMessage) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getDefaultLiveInfo(callback: NetRequestCallback<LiveRoomDefaultConfig>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.getDefaultLiveInfo() },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getAudienceList(
        liveRecodeId: Long,
        page: Int,
        size: Int,
        callback: NetRequestCallback<AudienceInfoList>
    ) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.getLiveRoomAudienceList(liveRecodeId, page, size) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getLivingRoomInfo(callback: NetRequestCallback<LiveRoomInfo>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.getLivingRoomInfo() },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun batchReward(
        liveRecodeId: Long,
        giftId: Int,
        giftCount: Int,
        userUuids: List<String>,
        callback: NetRequestCallback<Unit>
    ) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.batchReward(liveRecodeId, giftId, giftCount, userUuids) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun realNameAuthentication(name: String, cardNo: String, callback: NetRequestCallback<Unit>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.realNameAuthentication(name, cardNo) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun requestHostConnection(roomUuids: List<String>, timeoutSeconds: Long, ext: String?, callback: NetRequestCallback<RequestConnectionResponse>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.requestHostConnection(roomUuids, timeoutSeconds, ext) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun cancelRequestHostConnection(roomUuids: List<String>, callback: NetRequestCallback<Unit>) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.cancelRequestHostConnection(roomUuids) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun acceptRequestHostConnection(
        roomUuid: String,
        callback: NetRequestCallback<Unit>
    ) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.acceptRequestHostConnection(roomUuid) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun rejectRequestHostConnection(
        roomUuid: String,
        callback: NetRequestCallback<Unit>
    ) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.rejectRequestHostConnection(roomUuid) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun disconnectHostConnection(
        callback: NetRequestCallback<Unit>
    ) {
        liveRoomScope?.launch {
            Request.request(
                { liveRoomRepository.disconnectHostConnection() },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun reportHttpErrorEvent(error: HttpErrorReporter.ErrorEvent) {
        if (error.code != NEErrorCode.SUCCESS) {
            LiveRoomLog.e(TAG, "report http error: $error")
        }
        httpErrorEvents.value = error
    }

    override val httpErrorEvents =
        MutableStateFlow(HttpErrorReporter.ErrorEvent(NEErrorCode.SUCCESS, NEErrorMsg.SUCCESS, "0"))

    fun destroy() {
        liveRoomScope?.cancel()
        liveRoomScope = null
    }
}
