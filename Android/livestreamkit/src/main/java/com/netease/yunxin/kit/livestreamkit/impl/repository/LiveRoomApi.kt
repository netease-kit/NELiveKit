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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LiveRoomApi {

    /**
     * 获取语聊房房间列表
     */
    @POST("nemo/entertainmentLive/live/list")
    suspend fun getLiveRoomList(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<LiveRoomList>

    /**
     * 创建语聊房 房间
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
    suspend fun getRoomInfo(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<LiveRoomInfo>

    @GET("nemo/entertainmentLive/live/getDefaultLiveInfo")
    suspend fun getDefaultLiveInfo(): Response<LiveRoomDefaultConfig>

    @GET("nemo/entertainmentLive/live/audience/list")
    suspend fun getAudienceList(
        @Query("liveRecordId") liveRecordId: Long, @Query("page") page: Int,
        @Query(
            "size"
        ) size: Int = 20
    ): Response<AudienceInfoList>

    @GET("nemo/entertainmentLive/live/ongoing")
    suspend fun getLivingRoomInfo(): Response<LiveRoomInfo>

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
