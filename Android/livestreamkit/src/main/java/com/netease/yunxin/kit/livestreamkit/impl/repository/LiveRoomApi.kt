/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.livestreamkit.impl.repository

import com.netease.yunxin.kit.common.network.Response
import com.netease.yunxin.kit.livestreamkit.impl.model.AudienceInfoList
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomDefaultConfig
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomList
import com.netease.yunxin.kit.livestreamkit.impl.model.RequestConnectionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LiveRoomApi {

    /**
     * 获取直播房房间列表
     */
    @POST("nemo/entertainmentLive/live/list")
    suspend fun fetchLiveRoomList(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<LiveRoomList>

    /**
     * 获取连麦房间列表
     */
    @POST("nemo/entertainmentLive/live/available_connection_list")
    suspend fun fetchCoLiveRooms(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<LiveRoomList>

    /**
     * 创建直播房房间
     */
    @POST("nemo/entertainmentLive/live/createLiveV3")
    suspend fun startLiveRoom(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<LiveRoomInfo>

    /**
     * 加入成功后上报给服务器
     */
    @POST("nemo/entertainmentLive/live/joinedLiveRoom")
    suspend fun joinedLiveRoom(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 结束 语聊房 房间
     */
    @POST("nemo/entertainmentLive/live/destroyLive")
    suspend fun endLiveRoom(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    @POST("nemo/entertainmentLive/live/info")
    suspend fun fetchRoomInfo(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<LiveRoomInfo>

    @GET("nemo/entertainmentLive/live/getDefaultLiveInfo")
    suspend fun fetchDefaultLiveInfo(): Response<LiveRoomDefaultConfig>

    @GET("nemo/entertainmentLive/live/audience/list")
    suspend fun fetchAudienceList(
        @Query("liveRecordId") liveRecordId: Long, @Query("page") page: Int,
        @Query(
            "size"
        ) size: Int = 20
    ): Response<AudienceInfoList>

    @GET("nemo/entertainmentLive/live/ongoing")
    suspend fun fetchLivingRoomInfo(): Response<LiveRoomInfo>

    /**
     * 批量打赏
     */
    @POST("nemo/entertainmentLive/live/batch/reward")
    suspend fun batchReward(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 实名认证
     */
    @POST("nemo/entertainmentLive/real-name-authentication")
    suspend fun realNameAuthentication(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 申请主播连麦
     */
    @POST("nemo/entertainmentLive/live/request_connection")
    suspend fun requestHostConnection(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<RequestConnectionResponse>

    /**
     * 取消主播连麦申请
     */
    @POST("nemo/entertainmentLive/live/cancel_connection")
    suspend fun cancelRequestHostConnection(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 接受主播连麦
     */
    @POST("nemo/entertainmentLive/live/accept_connection")
    suspend fun acceptRequestHostConnection(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 拒绝主播连麦
     */
    @POST("nemo/entertainmentLive/live/reject_connection")
    suspend fun rejectRequestHostConnection(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 结束主播连麦
     */
    @POST("nemo/entertainmentLive/live/disconnect_connection")
    suspend fun disconnectHostConnection(): Response<Unit>

    /**
     * 直播暂停
     */
    @POST("nemo/entertainmentLive/live/pauseLive")
    suspend fun pauseLive(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    /**
     * 直播恢复
     */
    @POST("nemo/entertainmentLive/live/resumeLive")
    suspend fun resumeLive(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>
}
