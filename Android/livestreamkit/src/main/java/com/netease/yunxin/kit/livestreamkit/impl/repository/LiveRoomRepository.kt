/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.repository

import android.content.Context
import com.netease.yunxin.kit.common.network.Response
import com.netease.yunxin.kit.common.network.ServiceCreator
import com.netease.yunxin.kit.livestreamkit.BuildConfig
import com.netease.yunxin.kit.livestreamkit.impl.model.AudienceInfoList
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomDefaultConfig
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomList
import com.netease.yunxin.kit.livestreamkit.impl.model.RequestConnectionResponse
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.impl.repository.ServerConfig
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LiveRoomRepository {
    companion object {
        lateinit var serverConfig: ServerConfig
    }
    private val serviceCreator: ServiceCreator = ServiceCreator()

    private lateinit var liveRoomApi: LiveRoomApi

    fun initialize(context: Context, url: String) {
        serviceCreator.init(
            context,
            url,
            if (BuildConfig.DEBUG) ServiceCreator.LOG_LEVEL_BODY else ServiceCreator.LOG_LEVEL_BASIC,
            NERoomKit.getInstance().deviceId
        )
        val localLanguage = Locale.getDefault().language
        serviceCreator.addHeader(ServiceCreator.ACCEPT_LANGUAGE_KEY, localLanguage)
        liveRoomApi = serviceCreator.create(LiveRoomApi::class.java)
    }

    fun addHeader(key: String, value: String) {
        serviceCreator.addHeader(key, value)
    }

    suspend fun fetchLiveRoomList(
        liveType: Int,
        live: Int,
        pageNum: Int,
        pageSize: Int
    ): Response<LiveRoomList> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveType" to liveType,
            "live" to live,
            "pageNum" to pageNum,
            "pageSize" to pageSize
        )
        liveRoomApi.fetchLiveRoomList(params)
    }

    suspend fun fetchCoLiveRooms(
        liveType: Int,
        liveStatus: List<Int>,
        pageNum: Int,
        pageSize: Int
    ): Response<LiveRoomList> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveType" to liveType,
            "liveStatus" to liveStatus,
            "pageNum" to pageNum,
            "pageSize" to pageSize
        )
        liveRoomApi.fetchCoLiveRooms(params)
    }

    suspend fun createLiveRoom(
        liveTopic: String?,
        cover: String?,
        liveType: Int,
        configId: Int,
        roomName: String?,
        seatCount: Int,
        seatApplyMode: Int,
        seatInviteMode: Int
    ): Response<LiveRoomInfo> =
        withContext(Dispatchers.IO) {
            val params = mapOf<String, Any?>(
                "liveTopic" to liveTopic,
                "cover" to cover,
                "liveType" to liveType,
                "configId" to configId,
                "roomName" to roomName,
                "seatCount" to seatCount,
                "seatApplyMode" to seatApplyMode,
                "seatInviteMode" to seatInviteMode
            )
            liveRoomApi.startLiveRoom(params)
        }

    suspend fun joinedLiveRoom(liveRecordId: Long): Response<Unit> = withContext(Dispatchers.IO) {
        val params = mapOf(
            "liveRecordId" to liveRecordId
        )
        liveRoomApi.joinedLiveRoom(params)
    }

    suspend fun endLiveRoom(liveRecordId: Long): Response<Unit> = withContext(Dispatchers.IO) {
        val params = mapOf(
            "liveRecordId" to liveRecordId
        )
        liveRoomApi.endLiveRoom(params)
    }

    suspend fun pauseLiveRoom(liveRecordId: Long, notifyMessage: String?): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "liveRecordId" to liveRecordId,
            "notifyMessage" to notifyMessage
        )
        liveRoomApi.pauseLive(params)
    }

    suspend fun resumeLiveRoom(liveRecordId: Long, notifyMessage: String?): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "liveRecordId" to liveRecordId,
            "notifyMessage" to notifyMessage
        )
        liveRoomApi.resumeLive(params)
    }

    suspend fun getLiveRoomInfo(liveRecordId: Long): Response<LiveRoomInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "liveRecordId" to liveRecordId
        )
        liveRoomApi.fetchRoomInfo(params)
    }

    suspend fun getDefaultLiveInfo(): Response<LiveRoomDefaultConfig> = withContext(Dispatchers.IO) {
        liveRoomApi.fetchDefaultLiveInfo()
    }

    suspend fun getLiveRoomAudienceList(liveRecordId: Long, page: Int, size: Int): Response<AudienceInfoList> = withContext(
        Dispatchers.IO
    ) {
        liveRoomApi.fetchAudienceList(liveRecordId, page, size)
    }

    suspend fun getLivingRoomInfo(): Response<LiveRoomInfo> = withContext(
        Dispatchers.IO
    ) {
        liveRoomApi.fetchLivingRoomInfo()
    }

    suspend fun batchReward(liveRecordId: Long, giftId: Int, giftCount: Int, userUuids: List<String>): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "liveRecordId" to liveRecordId,
            "giftId" to giftId,
            "giftCount" to giftCount,
            "targets" to userUuids
        )
        liveRoomApi.batchReward(params)
    }

    suspend fun realNameAuthentication(name: String, cardNo: String): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "name" to name,
            "cardNo" to cardNo
        )
        liveRoomApi.realNameAuthentication(params)
    }

    suspend fun requestHostConnection(roomUuids: List<String>, timeout: Long, ext: String?): Response<RequestConnectionResponse> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuids" to roomUuids,
            "timeout" to timeout,
            "ext" to ext
        )
        liveRoomApi.requestHostConnection(params)
    }

    suspend fun cancelRequestHostConnection(roomUuids: List<String>): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuids" to roomUuids
        )
        liveRoomApi.cancelRequestHostConnection(params)
    }

    suspend fun acceptRequestHostConnection(roomUuid: String): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid
        )
        liveRoomApi.acceptRequestHostConnection(params)
    }
    suspend fun rejectRequestHostConnection(roomUuid: String): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid
        )
        liveRoomApi.rejectRequestHostConnection(params)
    }
    suspend fun disconnectHostConnection(): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        liveRoomApi.disconnectHostConnection()
    }
}
