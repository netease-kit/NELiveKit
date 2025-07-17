/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl

import android.content.Context
import android.text.TextUtils
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.livestreamkit.api.NECreateLiveRoomOptions
import com.netease.yunxin.kit.livestreamkit.api.NECreateLiveRoomParams
import com.netease.yunxin.kit.livestreamkit.api.NEJoinLiveStreamRoomParams
import com.netease.yunxin.kit.livestreamkit.api.NELiveRoomLiveState
import com.netease.yunxin.kit.livestreamkit.api.NELiveRoomRole
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit.Companion.getInstance
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKitConfig
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamListener
import com.netease.yunxin.kit.livestreamkit.api.NELiveType
import com.netease.yunxin.kit.livestreamkit.api.model.NEAudienceInfo
import com.netease.yunxin.kit.livestreamkit.api.model.NEAudienceInfoList
import com.netease.yunxin.kit.livestreamkit.api.model.NECreateLiveRoomDefaultInfo
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomList
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomSeatRequestItem
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveStreamRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.manager.CoHostManager
import com.netease.yunxin.kit.livestreamkit.impl.model.AudienceInfoList
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomDefaultConfig
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.LiveRoomList
import com.netease.yunxin.kit.livestreamkit.impl.model.StartLiveRoomParam
import com.netease.yunxin.kit.livestreamkit.impl.repository.LiveRoomRepository
import com.netease.yunxin.kit.livestreamkit.impl.service.LiveRoomHttpService
import com.netease.yunxin.kit.livestreamkit.impl.service.LiveRoomHttpServiceImpl
import com.netease.yunxin.kit.livestreamkit.impl.service.LiveRoomService
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomLog
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomUtils
import com.netease.yunxin.kit.livestreamkit.impl.utils.ScreenUtil
import com.netease.yunxin.kit.roomkit.api.NECallback2
import com.netease.yunxin.kit.roomkit.api.NEErrorCode
import com.netease.yunxin.kit.roomkit.api.NEPreviewRoomContext
import com.netease.yunxin.kit.roomkit.api.NEPreviewRoomListener
import com.netease.yunxin.kit.roomkit.api.NERoomChatMessage
import com.netease.yunxin.kit.roomkit.api.NERoomEndReason
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.api.NERoomKitOptions
import com.netease.yunxin.kit.roomkit.api.NERoomLanguage
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.model.NEIMServerConfig
import com.netease.yunxin.kit.roomkit.api.model.NERoomCreateAudioEffectOption
import com.netease.yunxin.kit.roomkit.api.model.NERoomCreateAudioMixingOption
import com.netease.yunxin.kit.roomkit.api.model.NERoomKitServerConfig
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcLastmileProbeConfig
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcLastmileProbeResult
import com.netease.yunxin.kit.roomkit.api.model.NEServerConfig
import com.netease.yunxin.kit.roomkit.api.service.NEAuthEvent
import com.netease.yunxin.kit.roomkit.api.service.NEAuthListener
import com.netease.yunxin.kit.roomkit.api.service.NEAuthService
import com.netease.yunxin.kit.roomkit.api.service.NEPreviewRoomOptions
import com.netease.yunxin.kit.roomkit.api.service.NEPreviewRoomParams
import com.netease.yunxin.kit.roomkit.api.service.NESeatInfo
import com.netease.yunxin.kit.roomkit.api.service.NESeatItem
import com.netease.yunxin.kit.roomkit.api.service.NESeatRequestItem
import com.netease.yunxin.kit.roomkit.api.view.NERoomVideoView
import com.netease.yunxin.kit.roomkit.impl.repository.ServerConfig
import com.netease.yunxin.kit.roomkit.impl.utils.CoroutineRunner
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

internal class LiveStreamKitImpl : NELiveStreamKit, CoroutineRunner() {
    private val roomHttpService: LiveRoomHttpService by lazy { LiveRoomHttpServiceImpl }
    private var createLiveRoomInfo: NELiveStreamRoomInfo? = null
    private var joinedRoomInfo: NELiveStreamRoomInfo? = null
    private val myRoomService = LiveRoomService()
    private lateinit var coHostManager: CoHostManager
    private val authListeners: CopyOnWriteArrayList<NEAuthListener> by lazy {
        CopyOnWriteArrayList()
    }
    private lateinit var context: Context
    private var hasLogin: Boolean = false
    private var livePausing: Boolean = false
    private var previewRoomContext: NEPreviewRoomContext? = null
    companion object {
        private const val tag = "NELiveStreamKit"
        private const val ACCEPT_LANGUAGE_KEY = "Accept-Language"
        private const val SERVER_URL_KEY = "serverUrl"
        private const val BASE_URL_KEY = "baseUrl"
        private const val HTTP_PREFIX = "http"
        private const val TEST_URL_VALUE = "test"
        private const val OVER_URL_VALUE = "oversea"
        private const val OVERSEA_SERVER_URL = "https://roomkit-sg.netease.im/"

        // IM 海外环境  https://doc.yunxin.163.com/TM5MzM5Njk/docs/zA5OTg4Njc?platform=android#Android%20%E7%AB%AF
        private const val LINK = "link-sg.netease.im:7000"
        private const val LBS = "https://lbs.netease.im/lbs/conf.jsp"
        private const val NOS_LBS = "http://wannos.127.net/lbs"
        private const val NOS_UPLOADER = "https://nosup-hz1.127.net"
        private const val NOS_DOWNLOADER = "{bucket}-nosdn.netease.im/{object}"
        private const val NOS_UPLOADER_HOST = "nosup-hz1.127.net"
        private const val LANGUAGE_EN = "en"
        private const val LANGUAGE_ZH = "zh"
    }

    override val localMember: NERoomMember?
        get() = myRoomService.getLocalMember()

    override val allMemberList: List<NERoomMember>
        get() {
            return myRoomService.getLocalMember()?.let {
                val list = mutableListOf(it)
                list.addAll(myRoomService.getRemoteMembers())
                list
            } ?: emptyList()
        }

    private var config: NELiveStreamKitConfig? = null
    private var baseUrl: String = ""
    override fun initialize(
        context: Context,
        config: NELiveStreamKitConfig,
        callback: NELiveStreamCallback<Unit>?
    ) {
        LiveRoomLog.logApi("initialize")
        this.context = context
        this.config = config
        ScreenUtil.init(context)
        coHostManager = CoHostManager(myRoomService)
        var realRoomServerUrl = ""
        var isOversea = false
        val realExtras = HashMap<String, Any?>()
        realExtras.putAll(config.extras)
        if (config.extras[SERVER_URL_KEY] != null) {
            val serverUrl: String = config.extras[SERVER_URL_KEY] as String
            baseUrl = config.extras[BASE_URL_KEY] as String
            LiveRoomLog.i(tag, "serverUrl:$serverUrl")
            LiveRoomLog.i(tag, "baseUrl:$baseUrl")
            if (!TextUtils.isEmpty(serverUrl)) {
                when {
                    TEST_URL_VALUE == serverUrl -> {
                        realRoomServerUrl = serverUrl
                    }
                    OVER_URL_VALUE == serverUrl -> {
                        realRoomServerUrl = OVERSEA_SERVER_URL
                        isOversea = true
                    }
                    serverUrl.startsWith(HTTP_PREFIX) -> {
                        realRoomServerUrl = serverUrl
                    }
                }
            }
        }
        realExtras[SERVER_URL_KEY] = realRoomServerUrl
        val serverConfig =
            ServerConfig.selectServer(config.appKey, realRoomServerUrl)
        LiveRoomRepository.serverConfig = serverConfig
        roomHttpService.initialize(context, baseUrl)
        roomHttpService.addHeader("appkey", config.appKey)
        NERoomKit.getInstance()
            .initialize(
                context,
                options = NERoomKitOptions(
                    appKey = config.appKey,
                    extras = realExtras,
                    serverConfig = if (isOversea) {
                        NEServerConfig().apply {
                            imServerConfig = NEIMServerConfig().apply {
                                link = LINK
                                lbs = LBS
                                nosLbs = NOS_LBS
                                nosUploader = NOS_UPLOADER
                                nosDownloader = NOS_DOWNLOADER
                                nosUploaderHost = NOS_UPLOADER_HOST
                                httpsEnabled = true
                            }
                            roomKitServerConfig = NERoomKitServerConfig().apply {
                                roomServer = realRoomServerUrl
                            }
                        }
                    } else {
                        null
                    }
                )
            ) { code, message, _ ->
                if (code == NEErrorCode.SUCCESS) {
                    NERoomKit.getInstance().roomService.previewRoom(
                        NEPreviewRoomParams(),
                        NEPreviewRoomOptions(),
                        object : NECallback2<NEPreviewRoomContext>() {
                            override fun onSuccess(data: NEPreviewRoomContext?) {
                                super.onSuccess(data)
                                previewRoomContext = data
                                previewRoomContext?.addPreviewRoomListener(object :
                                    NEPreviewRoomListener {
                                    override fun onRtcVirtualBackgroundSourceEnabled(
                                        enabled: Boolean,
                                        reason: Int
                                    ) {
                                    }

                                    override fun onRtcLastmileQuality(quality: Int) {
                                    }

                                    override fun onRtcLastmileProbeResult(result: NERoomRtcLastmileProbeResult) {
                                    }
                                })
                            }

                            override fun onError(code: Int, message: String?) {
                                super.onError(code, message)
                                LiveRoomLog.e(tag, "previewRoom error,code:$code,message:$message")
                            }
                        }
                    )
                    callback?.onSuccess(Unit)
                } else {
                    callback?.onFailure(code, message)
                }
            }

        NERoomKit.getInstance().getService(NEAuthService::class.java).addAuthListener(object :
            NEAuthListener {
            override fun onAuthEvent(evt: NEAuthEvent) {
                LiveRoomLog.i(tag, "onAuthEvent evt = $evt")
                hasLogin = (evt == NEAuthEvent.LOGGED_IN || evt == NEAuthEvent.RECONNECTED)
                authListeners.forEach {
                    it.onAuthEvent(
                        evt
                    )
                }
            }
        })

        initRoomServiceListener()
        launch {
            roomHttpService.httpErrorEvents.collect { evt ->
                if (evt.code == NEErrorCode.UNAUTHORIZED ||
                    evt.code == NEErrorCode.INCORRECT_TOKEN
                ) {
                    authListeners.forEach {
                        it.onAuthEvent(NEAuthEvent.LOGGED_OUT)
                    }
                } else if (evt.code == NEErrorCode.TOKEN_EXPIRED) {
                    authListeners.forEach {
                        it.onAuthEvent(NEAuthEvent.ACCOUNT_TOKEN_ERROR)
                    }
                }
            }
        }
    }

    private fun initRoomServiceListener() {
        myRoomService.addListener(object : NELiveStreamListener() {
            override fun onRoomEnded(reason: NERoomEndReason) {
                joinedRoomInfo = null
                createLiveRoomInfo = null
            }

            override fun onLivePause() {
                livePausing = true
            }

            override fun onLiveResume() {
                livePausing = false
            }
        })
    }

    override val isInitialized: Boolean
        get() = NERoomKit.getInstance().isInitialized

    override val isLoggedIn: Boolean
        get() = hasLogin

    override val isLivePausing: Boolean
        get() = livePausing

    override fun login(account: String, token: String, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("login: account = $account,token = $token")
        roomHttpService.addHeader("user", account)
        roomHttpService.addHeader("token", token)
        if (hasLogin) {
            callback?.onSuccess(Unit)
        } else if (NERoomKit.getInstance().getService(NEAuthService::class.java).isLoggedIn) {
            LiveRoomLog.i(tag, "login but isLoggedIn = true")
            callback?.onSuccess(Unit)
        } else {
            NERoomKit.getInstance().getService(NEAuthService::class.java).login(
                account,
                token,
                object : NECallback2<Unit>() {
                    override fun onSuccess(data: Unit?) {
                        LiveRoomLog.i(tag, "login success")
                        roomHttpService.addHeader("user", account)
                        roomHttpService.addHeader("token", token)
                        hasLogin = true
                        callback?.onSuccess(data)
                    }

                    override fun onError(code: Int, message: String?) {
                        LiveRoomLog.e(tag, "login error: code=$code, message=$message")
                        callback?.onFailure(code, message)
                        hasLogin = false
                    }
                }
            )
        }
    }

    override fun logout(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("logout")
        hasLogin = false
        NERoomKit.getInstance().getService(NEAuthService::class.java)
            .logout(object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    LiveRoomLog.i(tag, "logout success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "logout error: code=$code, message=$message")
                    callback?.onFailure(code, message)
                }
            })
    }

    /**
     * 获取房间列表
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param liveState 直播状态 (直播状态) [NELiveRoomLiveState]
     * @param pageNum 页码
     * @param pageSize 页大小,一页包含多少条
     * @param callback 房间列表回调
     *
     */
    override fun fetchLiveRoomList(
        liveState: NELiveRoomLiveState,
        type: Int,
        pageNum: Int,
        pageSize: Int,
        callback: NELiveStreamCallback<NELiveRoomList>?
    ) {
        LiveRoomLog.logApi(
            "getLiveRoomList: liveState=$liveState, pageNum=$pageNum, pageSize=$pageSize"
        )
        roomHttpService.fetchLiveRoomList(
            type,
            liveState.value,
            pageNum,
            pageSize,
            object :
                NetRequestCallback<LiveRoomList> {
                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.e(tag, "getLiveRoomList error: code = $code msg = $msg")
                    callback?.onFailure(code, msg)
                }

                override fun success(info: LiveRoomList?) {
                    LiveRoomLog.i(tag, "getLiveRoomList success info = $info")
                    callback?.onSuccess(
                        info?.let {
                            LiveRoomUtils.liveRoomList2NELiveRoomList(
                                it
                            )
                        }
                    )
                }
            }
        )
    }

    override fun addAuthListener(listener: NEAuthListener) {
        LiveRoomLog.logApi("addAuthListener: listener=$listener")
        authListeners.add(listener)
    }

    override fun removeAuthListener(listener: NEAuthListener) {
        LiveRoomLog.logApi("removeAuthListener: listener=$listener")
        authListeners.remove(listener)
    }

    override fun createRoom(
        params: NECreateLiveRoomParams,
        options: NECreateLiveRoomOptions,
        callback: NELiveStreamCallback<NELiveStreamRoomInfo>?
    ) {
        LiveRoomLog.logApi("createRoom: params=$params")
        val createRoomParam = StartLiveRoomParam(
            roomTopic = params.liveTopic,
            roomName = params.liveTopic,
            cover = params.cover ?: "",
            liveType = params.liveType,
            configId = params.configId,
            seatCount = params.seatCount,
            seatApplyMode = params.seatApplyMode,
            seatInviteMode = params.seatInviteMode
        )
        roomHttpService.startLiveRoom(
            createRoomParam,
            object : NetRequestCallback<LiveRoomInfo> {
                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.e(tag, "createRoom error: code=$code message=$msg")
                    callback?.onFailure(code, msg)
                }

                override fun success(info: LiveRoomInfo?) {
                    createLiveRoomInfo = info?.let {
                        LiveRoomUtils.liveRoomInfo2NELiveRoomInfo(it)
                    }
                    LiveRoomLog.i(tag, "createRoom success info = $info")
                    callback?.onSuccess(
                        info?.let {
                            LiveRoomUtils.liveRoomInfo2NELiveRoomInfo(
                                it
                            )
                        }
                    )
                }
            }
        )
    }

    override fun getCreateRoomDefaultInfo(
        callback: NELiveStreamCallback<NECreateLiveRoomDefaultInfo>
    ) {
        roomHttpService.getDefaultLiveInfo(object :
            NetRequestCallback<LiveRoomDefaultConfig> {
            override fun success(info: LiveRoomDefaultConfig?) {
                LiveRoomLog.i(tag, "getRoomDefault success info = $info")
                callback.onSuccess(
                    info?.let {
                        NECreateLiveRoomDefaultInfo(it.topic, it.livePicture, it.defaultPictures)
                    }
                )
            }

            override fun error(code: Int, msg: String?) {
                LiveRoomLog.e(tag, "getRoomDefault error: code=$code message=$msg")
                callback.onFailure(code, msg)
            }
        })
    }

    override fun getAudienceList(
        page: Int,
        size: Int,
        callback: NELiveStreamCallback<NEAudienceInfoList>
    ) {
        LiveRoomLog.logApi("getAudienceList page:$page,size:$size")
        if (joinedRoomInfo?.liveModel?.liveRecordId == null) {
            LiveRoomLog.e(tag, "liveRecordId==null")
            return
        }

        roomHttpService.getAudienceList(
            joinedRoomInfo?.liveModel?.liveRecordId!!,
            page,
            size,
            object :
                NetRequestCallback<AudienceInfoList> {
                override fun success(info: AudienceInfoList?) {
                    LiveRoomLog.i(tag, "getAudienceList success info = $info")
                    callback.onSuccess(
                        info?.let {
                            NEAudienceInfoList(
                                total = info.total,
                                list = info.list?.map {
                                    NEAudienceInfo(it.userUuid, it.userName, it.icon)
                                }
                            )
                        }
                    )
                }

                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.e(tag, "getAudienceList error: code=$code message=$msg")
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    override fun getLivingRoomInfo(
        callback: NELiveStreamCallback<NELiveStreamRoomInfo>
    ) {
        LiveRoomLog.logApi("getLivingRoomInfo")
        roomHttpService.getLivingRoomInfo(
            object : NetRequestCallback<LiveRoomInfo> {
                override fun success(info: LiveRoomInfo?) {
                    LiveRoomLog.i(tag, "getLivingRoomInfo success info = $info")
                    callback.onSuccess(
                        info?.let {
                            LiveRoomUtils.liveRoomInfo2NELiveRoomInfo(
                                it
                            )
                        }
                    )
                }

                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.i(tag, "getLivingRoomInfo error code = $code msg = $msg")
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    override fun joinRoom(
        params: NEJoinLiveStreamRoomParams,
        callback: NELiveStreamCallback<NELiveStreamRoomInfo>?
    ) {
        LiveRoomLog.logApi("joinRoom")
        myRoomService.joinRoom(
            params.roomInfo.liveModel.roomUuid,
            params.role,
            params.nick,
            params.extraData,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    LiveRoomLog.i(tag, "joinRoom success")
                    joinedRoomInfo = params.roomInfo
                    callback?.onSuccess(
                        params.roomInfo
                    )
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "joinRoom error: code=$code message=$message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun endRoom(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("endRoom")

        val liveRecordId = createLiveRoomInfo?.liveModel?.liveRecordId
            ?: joinedRoomInfo?.liveModel?.liveRecordId

        liveRecordId?.let {
            roomHttpService.stopLiveRoom(
                it,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        LiveRoomLog.i(tag, "stopLiveRoom success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        LiveRoomLog.e(tag, "stopLiveRoom error: code = $code message = $msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "liveRecordId is empty")

        myRoomService.endRoom(object : NECallback2<Unit>() {
            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "endRoom error: code = $code message = $message")
            }

            override fun onSuccess(data: Unit?) {
                LiveRoomLog.i(tag, "endRoom success")
            }
        })

        joinedRoomInfo = null
        createLiveRoomInfo = null
    }

    override fun pauseLive(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("pauseLive")

        val liveRecordId = createLiveRoomInfo?.liveModel?.liveRecordId
            ?: joinedRoomInfo?.liveModel?.liveRecordId

        muteMyAudio(null)
        muteMyVideo(null)

        liveRecordId?.let {
            roomHttpService.pauseLiveRoom(
                it,
                null,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        LiveRoomLog.i(tag, "pauseLiveRoom success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        LiveRoomLog.e(tag, "pauseLiveRoom error: code = $code message = $msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "liveRecordId is empty")
    }

    override fun resumeLive(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("resumeLive")

        unmuteMyAudio(null)
        unmuteMyVideo(null)
        val liveRecordId = createLiveRoomInfo?.liveModel?.liveRecordId
            ?: joinedRoomInfo?.liveModel?.liveRecordId

        liveRecordId?.let {
            roomHttpService.resumeLiveRoom(
                it,
                null,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        LiveRoomLog.i(tag, "resumeLiveRoom success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        LiveRoomLog.e(tag, "resumeLiveRoom error: code = $code message = $msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "liveRecordId is empty")
    }

    override fun leaveRoom(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("leaveRoom")
        myRoomService.leaveRoom(object : NECallback2<Unit>() {
            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "leaveRoom: error code = $code message = $message")
                callback?.onFailure(code, message)
            }

            override fun onSuccess(data: Unit?) {
                LiveRoomLog.i(tag, "leaveRoom success")
                callback?.onSuccess(null)
            }
        })
        joinedRoomInfo = null
    }

    override fun getRoomInfo(liveRecordId: Long, callback: NELiveStreamCallback<NELiveStreamRoomInfo>) {
        roomHttpService.getRoomInfo(
            liveRecordId,
            object : NetRequestCallback<LiveRoomInfo> {
                override fun success(info: LiveRoomInfo?) {
                    callback.onSuccess(
                        info?.let {
                            LiveRoomUtils.liveRoomInfo2NELiveRoomInfo(
                                it
                            )
                        }
                    )
                }

                override fun error(code: Int, msg: String?) {
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    override fun getCurrentRoomInfo(): NELiveStreamRoomInfo? {
        return joinedRoomInfo
    }

    override fun getSeatInfo(callback: NELiveStreamCallback<NESeatInfo>?) {
        LiveRoomLog.logApi("getSeatInfo")
        myRoomService.getSeatInfo(object : NECallback2<NESeatInfo>() {
            override fun onSuccess(data: NESeatInfo?) {
                LiveRoomLog.i(tag, "getSeatInfo success")
                callback?.onSuccess(
                    data
                )
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "getSeatInfo error:code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun getSeatRequestList(
        callback: NELiveStreamCallback<List<NELiveRoomSeatRequestItem>>?
    ) {
        LiveRoomLog.logApi("getSeatRequestList")
        myRoomService.getSeatRequestList(object : NECallback2<List<NESeatRequestItem>>() {
            override fun onSuccess(data: List<NESeatRequestItem>?) {
                LiveRoomLog.i(tag, "getSeatRequestList success")
                callback?.onSuccess(
                    data?.map {
                        LiveRoomUtils.seatRequestItem2NELiveRoomSeatRequestItem(
                            it
                        )
                    }
                )
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "getSeatRequestList error:code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    /**
     * 房主向成员[account]发送上麦邀请，指定位置为[seatIndex]，非管理员执行该操作会失败。
     * @param seatIndex 麦位位置。
     * @param account 麦上的用户ID。
     * @param callback 回调。
     */
    override fun sendSeatInvitation(
        account: String,
        callback: NELiveStreamCallback<Unit>?
    ) {
        LiveRoomLog.logApi("sendSeatInvitation,account:$account")
        myRoomService.sendSeatInvitation(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "sendSeatInvitation onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    /**
     * 房主向成员[account]发送上麦邀请，指定位置为[seatIndex]，非管理员执行该操作会失败。
     * @param seatIndex 麦位位置。
     * @param account 麦上的用户ID。
     * @param callback 回调。
     */
    override fun sendSeatInvitation(
        seatIndex: Int,
        account: String,
        callback: NELiveStreamCallback<Unit>?
    ) {
        LiveRoomLog.logApi("sendSeatInvitation,seatIndex:$seatIndex,account:$account")
        myRoomService.sendSeatInvitation(
            seatIndex,
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "sendSeatInvitation onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    /**
     * 同意上麦邀请。
     * @param callback 回调。
     */
    override fun acceptSeatInvitation(
        callback: NELiveStreamCallback<Unit>?
    ) {
        LiveRoomLog.logApi("sendSeatInvitation")
        myRoomService.acceptSeatInvitation(
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "sendSeatInvitation onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    /**
     * 拒绝上麦邀请。
     * @param callback 回调。
     */
    override fun rejectSeatInvitation(
        callback: NELiveStreamCallback<Unit>?
    ) {
        LiveRoomLog.logApi("sendSeatInvitation")
        myRoomService.rejectSeatInvitation(
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "sendSeatInvitation onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun submitSeatRequest(
        seatIndex: Int,
        exclusive: Boolean,
        callback: NELiveStreamCallback<Unit>?
    ) {
        LiveRoomLog.logApi("submitSeatRequest seatIndex:$seatIndex")
        myRoomService.submitSeatRequest(
            seatIndex,
            exclusive,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "submitSeatRequest onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun submitSeatRequest(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("submitSeatRequest")
        myRoomService.submitSeatRequest(
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "submitSeatRequest onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun cancelSeatRequest(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("cancelSeatRequest")
        myRoomService.cancelSeatRequest(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "cancelSeatRequest onError code:$code")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun approveSeatRequest(account: String, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("approveSeatRequest: account=$account")
        myRoomService.approveSeatRequest(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "approveSeatRequest onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun rejectSeatRequest(account: String, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("rejectSeatRequest: account=$account")
        myRoomService.rejectSeatRequest(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "rejectSeatRequest onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun kickSeat(account: String, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("kickSeat: account=$account")
        myRoomService.kickSeat(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "kickSeat onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun leaveSeat(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("leaveSeat")
        myRoomService.leaveSeat(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "leaveSeat onError code:$code")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun banRemoteAudio(account: String, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("banRemoteAudio: account=$account")
        myRoomService.banRemoteAudio(
            account,
            object : NECallback2<Unit>() {

                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "banRemoteAudio onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun unbanRemoteAudio(account: String, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("unbanRemoteAudio: account=$account")
        myRoomService.unbanRemoteAudio(
            account,
            object : NECallback2<Unit>() {

                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "unbanRemoteAudio onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    /**
     * 打开麦位
     * @param seatIndices 麦位序号
     * @param callback 打开麦位回调
     */
    override fun openSeats(seatIndices: List<Int>, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("openSeat")
        myRoomService.openSeats(
            seatIndices,
            object : NECallback2<Unit>() {

                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "openSeats onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    /**
     * 关闭麦位
     * @param callback 关闭麦位回调
     */
    override fun closeSeats(seatIndices: List<Int>, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("closeSeat")
        myRoomService.closeSeats(
            seatIndices,
            object : NECallback2<Unit>() {

                override fun onSuccess(data: Unit?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "closeSeats onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun sendTextMessage(content: String, callback: NELiveStreamCallback<NERoomChatMessage>?) {
        LiveRoomLog.logApi("sendTextMessage")
        myRoomService.sendTextMessage(
            content,
            object : NECallback2<NERoomChatMessage>() {

                override fun onSuccess(data: NERoomChatMessage?) {
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(tag, "sendTextMessage onError code:$code")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun muteMyAudio(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("muteMyAudio")
        myRoomService.muteMyAudio(object : NECallback2<Unit>() {

            override fun onSuccess(data: Unit?) {
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "muteMyAudio onError code:$code")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun unmuteMyAudio(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("unmuteMyAudio")
        myRoomService.unmuteMyAudio(object : NECallback2<Unit>() {

            override fun onSuccess(data: Unit?) {
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "unmuteMyAudio onError code:$code")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun muteMyVideo(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("muteMyVideo")
        myRoomService.muteMyVideo(object : NECallback2<Unit>() {

            override fun onSuccess(data: Unit?) {
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "muteMyVideo onError code:$code")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun unmuteMyVideo(callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("unmuteMyVideo")
        myRoomService.unmuteMyVideo(object : NECallback2<Unit>() {

            override fun onSuccess(data: Unit?) {
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(tag, "unmuteMyVideo onError code:$code")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun adjustRecordingSignalVolume(volume: Int): Int {
        LiveRoomLog.logApi("adjustRecordingSignalVolume: volume=$volume ")
        return myRoomService.adjustRecordingSignalVolume(volume)
    }

    /**
     * 获取人声音量
     * @return 人声音量
     */
    override fun getRecordingSignalVolume(): Int {
        LiveRoomLog.logApi("getRecordingSignalVolume")
        return myRoomService.getRecordingSignalVolume()
    }

    /**
     * 开始播放音乐文件。
     * 该方法指定本地或在线音频文件来和录音设备采集的音频流进行混音。
     * 支持的音乐文件类型包括 MP3、M4A、AAC、3GP、WMA 和 WAV 格式，支持本地文件或在线 URL。
     * @param option    创建混音任务配置的选项，包括混音任务类型、混音文件全路径或 URL 等，详细信息请参考 audio.NERtcCreateAudioMixingOption。
     */
    override fun startAudioMixing(option: NERoomCreateAudioMixingOption): Int {
        LiveRoomLog.logApi("startAudioMixing")
        return myRoomService.startAudioMixing(option)
    }

    /**
     * 暂停播放音乐文件及混音。
     * @return 0：方法调用成功。其他：方法调用失败
     */
    override fun pauseAudioMixing(): Int {
        LiveRoomLog.logApi("pauseAudioMixing")
        return myRoomService.pauseAudioMixing()
    }

    /**
     * 恢复播放伴奏。
     * 该方法恢复混音，继续播放伴奏。请在房间内调用该方法。
     * @return 0：方法调用成功。其他：方法调用失败
     */
    override fun resumeAudioMixing(): Int {
        LiveRoomLog.logApi("resumeAudioMixing")
        return myRoomService.resumeAudioMixing()
    }

    override fun stopAudioMixing(): Int {
        LiveRoomLog.logApi("stopAudioMixing")
        return myRoomService.stopAudioMixing()
    }

    /**
     * 设置伴奏音量。
     * 该方法调节混音里伴奏的音量大小。 setAudioMixingSendVolume setAudioMixingPlaybackVolume
     * @param volume    伴奏发送音量。取值范围为 0~100。默认 100，即原始文件音量。
     */
    override fun setAudioMixingVolume(volume: Int): Int {
        LiveRoomLog.logApi("startAudioMixing")
        return myRoomService.setAudioMixingVolume(volume)
    }

    /**
     * 获取伴奏音量
     * @return Int
     */
    override fun getAudioMixingVolume(): Int {
        LiveRoomLog.logApi("getAudioMixingVolume")
        return myRoomService.getAudioMixingVolume()
    }

    /**
     * 播放指定音效文件。
     * 该方法播放指定的本地或在线音效文件。
     * 支持的音效文件类型包括 MP3、M4A、AAC、3GP、WMA 和 WAV 格式，支持本地 SD 卡中的文件和在线 URL
     * @param effectId    指定音效的 ID。每个音效均应有唯一的 ID。
     * @param option    音效相关参数，包括混音任务类型、混音文件路径等。
     */
    override fun playEffect(effectId: Int, option: NERoomCreateAudioEffectOption): Int {
        LiveRoomLog.logApi("playEffect")
        return myRoomService.playEffect(effectId, option)
    }

    /**
     * 设置音效音量 setEffectPlaybackVolume setEffectSendVolume
     * @param effectId Int
     * @param volume Int
     * @return 0：方法调用成功。其他：方法调用失败
     */
    override fun setEffectVolume(effectId: Int, volume: Int): Int {
        LiveRoomLog.logApi("setEffectVolume,effectId:$effectId,volume:$volume")
        return myRoomService.setEffectVolume(effectId, volume)
    }

    /**
     * 获取音效音量
     * @return 音效音量
     */
    override fun getEffectVolume(): Int {
        LiveRoomLog.logApi("getEffectVolume")
        return myRoomService.getEffectVolume()
    }

    override fun stopAllEffect(): Int {
        LiveRoomLog.logApi("stopAllEffect")
        return myRoomService.stopAllEffect()
    }

    override fun stopEffect(effectId: Int): Int {
        LiveRoomLog.logApi("stopEffect effectId:$effectId")
        return myRoomService.stopEffect(effectId)
    }

    override fun setPlayingPosition(effectId: Int, position: Long): Int {
        return myRoomService.setPlayingPosition(effectId, position)
    }

    override fun pauseEffect(effectId: Int): Int {
        return myRoomService.pauseEffect(effectId)
    }

    override fun resumeEffect(effectId: Int): Int {
        return myRoomService.resumeEffect(effectId)
    }

    override fun switchCamera(): Int {
        return myRoomService.switchCamera()
    }

    override fun changeMemberRole(userUuid: String, role: String, callback: NELiveStreamCallback<Unit>?) {
        myRoomService.changeMemberRole(
            userUuid,
            role,
            object :
                NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    if (TextUtils.equals(role, NELiveRoomRole.AUDIENCE.value)) {
                        myRoomService.joinRtcChannel(
                            object : NECallback2<Unit>() {
                                override fun onSuccess(data: Unit?) {
                                    LiveRoomLog.i(tag, "joinRtcChannel success")
                                    getInstance().unmuteMyAudio(null)
                                    getInstance().unmuteMyVideo(null)
                                    callback?.onSuccess(data)
                                }

                                override fun onError(code: Int, message: String?) {
                                    LiveRoomLog.e(tag, "joinRtcChannel failed")
                                    getInstance().leaveSeat(null)
                                    callback?.onFailure(code, message)
                                }
                            }
                        )
                    } else if (TextUtils.equals(role, NELiveRoomRole.AUDIENCE_OBSERVER.value)) {
                        callback?.onSuccess(data)
                    } else {
                        callback?.onSuccess(data)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun setupLocalVideoCanvas(videoView: NERoomVideoView?): Int {
        LiveRoomLog.logApi("setupLocalVideoCanvas")
        return myRoomService.setupLocalVideoCanvas(videoView)
    }

    override fun setupRemoteVideoCanvas(videoView: NERoomVideoView?, userUuid: String): Int {
        LiveRoomLog.logApi("setupRemoteVideoCanvas")
        return myRoomService.setupRemoteVideoCanvas(videoView, userUuid)
    }

    override fun enableAudioVolumeIndication(enable: Boolean, interval: Int): Int {
        LiveRoomLog.logApi("enableAudioVolumeIndication")
        return myRoomService.enableAudioVolumeIndication(enable, interval)
    }

    override fun enableEarback(volume: Int): Int {
        LiveRoomLog.logApi("enableEarBack: volume=$volume")
        return myRoomService.enableEarBack(volume)
    }

    override fun disableEarback(): Int {
        LiveRoomLog.logApi("disableEarBack")
        return myRoomService.disableEarBack()
    }

    override fun isEarbackEnable(): Boolean {
        LiveRoomLog.logApi("isEarBackEnable")
        return myRoomService.isEarBackEnable()
    }

    override fun isLocalOnSeat(): Boolean {
        LiveRoomLog.logApi("isLocalOnSeat")
        return myRoomService.isLocalOnSeat()
    }

    override fun getOnSeatList(): List<NESeatItem> {
        return myRoomService.getOnSeatList()
    }

    override fun addLiveStreamListener(listener: NELiveStreamListener) {
        LiveRoomLog.logApi("addLiveRoomListener: listener=$listener")
        myRoomService.addListener(listener)
    }

    override fun removeLiveStreamListener(listener: NELiveStreamListener) {
        LiveRoomLog.logApi("removeLiveRoomListener: listener=$listener")
        myRoomService.removeListener(listener)
    }

    override fun sendBatchGift(
        giftId: Int,
        giftCount: Int,
        userUuids: List<String>,
        callback: NELiveStreamCallback<Unit>?
    ) {
        LiveRoomLog.logApi(
            "sendBatchGift giftId:$giftId,giftCount:$giftCount,userUuids:$userUuids"
        )
        if (joinedRoomInfo?.liveModel?.liveRecordId == null) {
            LiveRoomLog.e(tag, "liveRecordId==null")
            return
        }

        roomHttpService.batchReward(
            joinedRoomInfo?.liveModel?.liveRecordId!!,
            giftId,
            giftCount,
            userUuids,
            object : NetRequestCallback<Unit> {
                override fun success(info: Unit?) {
                    LiveRoomLog.i(tag, "batchReward success")
                    callback?.onSuccess(info)
                }

                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.e(tag, "batchReward error: code = $code message = $msg")
                    callback?.onFailure(code, msg)
                }
            }
        )
    }

    override fun fetchCoLiveRooms(
        pageNum: Int,
        pageSize: Int,
        callback: NELiveStreamCallback<NELiveRoomList>?
    ) {
        LiveRoomLog.logApi(
            "fetchCoLiveRooms: pageNum=$pageNum, pageSize=$pageSize"
        )
        roomHttpService.fetchCoLiveRooms(
            NELiveType.LIVE_INTERACTION,
            listOf(NELiveRoomLiveState.Live.value),
            pageNum,
            pageSize,
            object :
                NetRequestCallback<LiveRoomList> {
                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.e(tag, "getLiveRoomList error: code = $code msg = $msg")
                    callback?.onFailure(code, msg)
                }

                override fun success(info: LiveRoomList?) {
                    LiveRoomLog.i(tag, "getLiveRoomList success info = $info")
                    callback?.onSuccess(
                        info?.let {
                            LiveRoomUtils.liveRoomList2NELiveRoomList(
                                it
                            )
                        }
                    )
                }
            }
        )
    }

    override fun getCoHostManager(): CoHostManager {
        return coHostManager
    }

    override fun authenticate(name: String, cardNo: String, callback: NELiveStreamCallback<Unit>?) {
        LiveRoomLog.logApi("realNameAuthentication name:$name,cardNo:$cardNo")
        roomHttpService.realNameAuthentication(
            name,
            cardNo,
            object : NetRequestCallback<Unit> {
                override fun success(info: Unit?) {
                    LiveRoomLog.i(tag, "realNameAuthentication success")
                    callback?.onSuccess(info)
                }

                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.e(tag, "realNameAuthentication error: code = $code message = $msg")
                    callback?.onFailure(code, msg)
                }
            }
        )
    }

    override fun startLastmileProbeTest(config: NERoomRtcLastmileProbeConfig): Int {
        if (previewRoomContext == null || previewRoomContext?.previewController == null) {
            LiveRoomLog.e(tag, "startLastmileProbeTest failed,config:$config")
            return NEErrorCode.FAILURE
        }
        LiveRoomLog.i(tag, "startLastmileProbeTest,config:$config")
        return previewRoomContext!!.previewController.startLastmileProbeTest(
            config
        )
    }

    override fun stopLastmileProbeTest(): Int {
        if (previewRoomContext == null || previewRoomContext?.previewController == null) {
            LiveRoomLog.e(tag, "stopLastmileProbeTest failed")
            return NEErrorCode.FAILURE
        }
        LiveRoomLog.i(tag, "stopLastmileProbeTest")
        return previewRoomContext!!.previewController.stopLastmileProbeTest()
    }

    override fun uploadLog() {
        LiveRoomLog.i(tag, "uploadLog")
        return NERoomKit.getInstance().uploadLog(null)
    }

    override fun switchLanguage(language: NERoomLanguage): Int {
        when (language) {
            NERoomLanguage.AUTOMATIC -> {
                val localLanguage = Locale.getDefault().language
                roomHttpService.addHeader(
                    ACCEPT_LANGUAGE_KEY,
                    if (!localLanguage.contains(LANGUAGE_ZH)) LANGUAGE_EN else LANGUAGE_ZH
                )
                return NERoomKit.getInstance().switchLanguage(NERoomLanguage.AUTOMATIC)
            }
            NERoomLanguage.CHINESE -> {
                roomHttpService.addHeader(ACCEPT_LANGUAGE_KEY, LANGUAGE_ZH)
                return NERoomKit.getInstance().switchLanguage(NERoomLanguage.CHINESE)
            }
            NERoomLanguage.ENGLISH -> {
                roomHttpService.addHeader(ACCEPT_LANGUAGE_KEY, LANGUAGE_EN)
                return NERoomKit.getInstance().switchLanguage(NERoomLanguage.ENGLISH)
            }
            else -> return NEErrorCode.FAILURE
        }
    }
}
