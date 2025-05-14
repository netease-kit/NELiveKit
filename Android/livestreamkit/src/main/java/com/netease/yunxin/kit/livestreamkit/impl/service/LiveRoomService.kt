/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.service

import android.net.Uri
import android.text.TextUtils
import com.google.gson.JsonObject
import com.netease.yunxin.kit.common.utils.NetworkUtils
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamListener
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomMember
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomMemberVolumeInfo
import com.netease.yunxin.kit.livestreamkit.impl.model.VoiceRoomBatchGiftModel
import com.netease.yunxin.kit.livestreamkit.impl.model.VoiceRoomMember
import com.netease.yunxin.kit.livestreamkit.impl.model.VoiceRoomMemberVolumeInfo
import com.netease.yunxin.kit.livestreamkit.impl.utils.GsonUtils
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomLog
import com.netease.yunxin.kit.roomkit.api.NECallback
import com.netease.yunxin.kit.roomkit.api.NECallback2
import com.netease.yunxin.kit.roomkit.api.NEErrorCode
import com.netease.yunxin.kit.roomkit.api.NERoomBuiltinRole
import com.netease.yunxin.kit.roomkit.api.NERoomChatEventType
import com.netease.yunxin.kit.roomkit.api.NERoomChatMessage
import com.netease.yunxin.kit.roomkit.api.NERoomChatNotificationMessage
import com.netease.yunxin.kit.roomkit.api.NERoomChatTextMessage
import com.netease.yunxin.kit.roomkit.api.NERoomContext
import com.netease.yunxin.kit.roomkit.api.NERoomEndReason
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.api.NERoomListener
import com.netease.yunxin.kit.roomkit.api.NERoomListenerAdapter
import com.netease.yunxin.kit.roomkit.api.NERoomLiveState
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.NERoomRole
import com.netease.yunxin.kit.roomkit.api.NEUnitCallback
import com.netease.yunxin.kit.roomkit.api.NEValueCallback
import com.netease.yunxin.kit.roomkit.api.model.NEAudioOutputDevice
import com.netease.yunxin.kit.roomkit.api.model.NEMemberVolumeInfo
import com.netease.yunxin.kit.roomkit.api.model.NERoomConnectType
import com.netease.yunxin.kit.roomkit.api.model.NERoomCreateAudioEffectOption
import com.netease.yunxin.kit.roomkit.api.model.NERoomCreateAudioMixingOption
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioStreamType
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcClientRole
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcLastmileProbeResult
import com.netease.yunxin.kit.roomkit.api.service.NEJoinRoomOptions
import com.netease.yunxin.kit.roomkit.api.service.NEJoinRoomParams
import com.netease.yunxin.kit.roomkit.api.service.NERoomService
import com.netease.yunxin.kit.roomkit.api.service.NESeatEventListener
import com.netease.yunxin.kit.roomkit.api.service.NESeatInfo
import com.netease.yunxin.kit.roomkit.api.service.NESeatItem
import com.netease.yunxin.kit.roomkit.api.service.NESeatItemStatus
import com.netease.yunxin.kit.roomkit.api.service.NESeatRequestItem
import com.netease.yunxin.kit.roomkit.api.view.NERoomVideoView
import com.netease.yunxin.kit.roomkit.impl.model.RoomCustomMessages

internal class LiveRoomService {

    private var currentRoomContext: NERoomContext? = null
    private val listeners = ArrayList<NELiveStreamListener>()
    private var roomListener: NERoomListener? = null
    private var seatListener: NESeatEventListener? = null
    private var isEarBackEnable: Boolean = false
    private var onSeatItems: List<NESeatItem>? = null
    var isLocalOnSeat = false
    private var recordingSignalVolume: Int = 100
    private var audioMixingVolume: Int = 100
    private var effectVolume: Int = 100

    private val networkStateListener: NetworkUtils.NetworkStateListener =
        object : NetworkUtils.NetworkStateListener {
            private var isFirst = true
            override fun onConnected(networkType: NetworkUtils.NetworkType?) {
                LiveRoomLog.d(TAG, "onNetwork available isFirst = $isFirst")
                if (!isFirst) {
                    getSeatInfo(object : NECallback2<NESeatInfo>() {
                        override fun onSuccess(data: NESeatInfo?) {
                            super.onSuccess(data)
                            data?.let {
                                handleSeatListItemChanged(data.seatItems)
                                listeners.forEach {
                                    it.onSeatListChanged(
                                        data.seatItems
                                    )
                                }
                            }
                        }
                    })
                }
                isFirst = false
            }

            override fun onDisconnected() {
                LiveRoomLog.d(TAG, "onNetwork unavailable")
                isFirst = false
            }
        }

    companion object {
        private const val TAG = "VoiceRoomService"
        private const val ERROR_MSG_ROOM_NOT_EXISTS = "Room not exists"
        private const val ERROR_MSG_MEMBER_NOT_EXISTS = "Member not exists"
        private const val ERROR_MSG_MEMBER_AUDIO_BANNED = "Member audio banned"
        const val TYPE_BATCH_GIFT = 1005 // 批量礼物
        const val TYPE_LIVE_PAUSE = 1105 // 暂停直播
        const val TYPE_LIVE_RESUME = 1106 // 恢复直播
    }

    fun getLocalMember(): NERoomMember? {
        return currentRoomContext?.localMember
    }

    fun getRemoteMembers(): List<NERoomMember> {
        return currentRoomContext?.remoteMembers ?: emptyList()
    }

    fun getMember(account: String) = currentRoomContext?.getMember(account)?.let {
        mapMember(it)
    }

    fun isEarBackEnable() = isEarBackEnable

    fun joinRoom(
        roomUuid: String,
        role: String,
        userName: String,
        avatar: String?,
        extraData: Map<String, String>?,
        callback: NECallback2<Unit>
    ) {
        val neJoinRoomParams: NEJoinRoomParams?
        if (extraData != null) {
            neJoinRoomParams = NEJoinRoomParams(
                roomUuid = roomUuid,
                userName = userName,
                avatar = avatar,
                role = role,
                initialProperties = extraData
            )
        } else {
            neJoinRoomParams = NEJoinRoomParams(
                roomUuid = roomUuid,
                userName = userName,
                avatar = avatar,
                role = role
            )
        }
        NERoomKit.getInstance().getService(NERoomService::class.java).joinRoom(
            neJoinRoomParams,
            NEJoinRoomOptions(),
            object : NECallback2<NERoomContext>() {
                override fun onSuccess(data: NERoomContext?) {
                    LiveRoomLog.d(TAG, "joinRoom roomUuid = $roomUuid success")
                    currentRoomContext = data!!
                    addRoomListener()
                    addSeatListener()
                    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener)
                    if (role != NERoomBuiltinRole.OBSERVER) {
                        if (role == "host") {
                            currentRoomContext?.rtcController?.setClientRole(
                                NERoomRtcClientRole.BROADCASTER
                            )
                        } else {
                            currentRoomContext?.rtcController?.setClientRole(
                                NERoomRtcClientRole.AUDIENCE
                            )
                        }
                        joinRtcChannel(object : NECallback2<Unit>() {
                            override fun onSuccess(data: Unit?) {
                                LiveRoomLog.d(TAG, "joinRtcChannel roomUuid = $roomUuid success")
                                joinChatroomChannel(object : NECallback2<Unit>() {
                                    override fun onSuccess(data: Unit?) {
                                        LiveRoomLog.d(
                                            TAG,
                                            "joinChatroomChannel roomUuid = $roomUuid success"
                                        )
                                        unmuteMyAudio(EmptyCallback)
                                        unmuteMyVideo(EmptyCallback)
                                        callback.onSuccess(data)
                                    }

                                    override fun onError(code: Int, message: String?) {
                                        LiveRoomLog.e(
                                            TAG,
                                            "joinChatroomChannel roomUuid = $roomUuid error code = $code message = $message"
                                        )

                                        leaveRtcChannel(null)
                                        callback.onError(code, message)
                                    }
                                })
                            }

                            override fun onError(code: Int, message: String?) {
                                LiveRoomLog.e(
                                    TAG,
                                    "joinRtcChannel failed roomUuid = $roomUuid error code = $code message = $message"
                                )
                                currentRoomContext?.leaveRoom(object : NECallback2<Unit?>() {})
                                callback.onError(code, message)
                            }
                        })
                    } else {
                        joinChatroomChannel(object : NECallback2<Unit>() {
                            override fun onSuccess(data: Unit?) {
                                LiveRoomLog.d(
                                    TAG,
                                    "joinChatroomChannel roomUuid = $roomUuid success"
                                )
                                callback.onSuccess(data)
                            }

                            override fun onError(code: Int, message: String?) {
                                LiveRoomLog.e(
                                    TAG,
                                    "joinChatroomChannel roomUuid = $roomUuid error code = $code message = $message"
                                )
                                callback.onError(code, message)
                            }
                        })
                    }
                }

                override fun onError(code: Int, message: String?) {
                    LiveRoomLog.e(
                        TAG,
                        "joinRoom roomUuid = $roomUuid error code = $code message = $message"
                    )
                    callback.onResult(code, message, null)
                }
            }
        )
    }

    fun joinRtcChannel(callback: NECallback<Unit>? = null) {
        if (currentRoomContext == null) {
            callback?.onResult(NEErrorCode.FAILURE, "", null)
            return
        }
        currentRoomContext?.rtcController?.setParameters("key_auto_subscribe_video", true)
        currentRoomContext?.rtcController?.joinRtcChannel(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                LiveRoomLog.d(TAG, "joinRtcChannel success")
                callback?.onResult(NEErrorCode.SUCCESS, "", null)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(TAG, "joinRtcChannel error code = $code message = $message")
                callback?.onResult(code, message, null)
            }
        })
    }

    fun leaveRtcChannel(callback: NECallback<Unit>? = null) {
        if (currentRoomContext == null) {
            callback?.onResult(NEErrorCode.FAILURE, "", null)
            return
        }
        currentRoomContext?.rtcController?.leaveRtcChannel(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                LiveRoomLog.d(TAG, "leaveRtcChannel success")
                callback?.onResult(NEErrorCode.SUCCESS, "", null)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(TAG, "leaveRtcChannel error code = $code message = $message")
                callback?.onResult(code, message, null)
            }
        })
    }

    fun joinChatroomChannel(callback: NECallback2<Unit>) {
        currentRoomContext?.chatController?.joinChatroom(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                LiveRoomLog.d(TAG, "joinChatroomChannel success")
                callback.onResult(NEErrorCode.SUCCESS, "", null)
            }

            override fun onError(code: Int, message: String?) {
                LiveRoomLog.e(TAG, "joinChatroomChannel error code = $code message = $message")
                callback.onError(code, message)
            }
        })
    }

    /**
     * 移除监听 --- 离开房间，结束房间
     */
    private fun removeListener() {
        LiveRoomLog.d(TAG, "removeRoomListener,roomListener:$roomListener")
        LiveRoomLog.d(TAG, "removeSeatListener,seatListener:$seatListener")
        roomListener?.apply { currentRoomContext?.removeRoomListener(roomListener!!) }
        seatListener?.apply {
            currentRoomContext?.seatController?.removeSeatListener(seatListener!!)
        }
        NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener)
    }

    fun leaveRoom(callback: NECallback2<Unit>) {
        currentRoomContext?.leaveRoom(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                callback.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                callback.onError(code, message)
            }
        })
        removeListener()
        isEarBackEnable = false
        currentRoomContext = null
        onSeatItems = null
    }

    fun endRoom(callback: NECallback<Unit>) {
        currentRoomContext?.endRoom(true, callback)
        removeListener()
        isEarBackEnable = false
        currentRoomContext = null
        onSeatItems = null
    }

    fun sendTextMessage(content: String, callback: NECallback2<NERoomChatMessage>) {
        currentRoomContext?.chatController?.sendBroadcastTextMessage(content, callback)
            ?: callback.onError(
                NEErrorCode.FAILURE,
                ERROR_MSG_ROOM_NOT_EXISTS
            )
    }

    fun kickMemberOut(userUuid: String, callback: NECallback2<Unit>) {
        currentRoomContext?.kickMemberOut(userUuid, false, callback)
            ?: callback.onError(
                NEErrorCode.FAILURE,
                ERROR_MSG_ROOM_NOT_EXISTS
            )
    }

    fun muteMyAudio(callback: NECallback2<Unit>) {
        val context = currentRoomContext
        if (context == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_ROOM_NOT_EXISTS)
            return
        }

        context.rtcController.muteMyAudio(
            true,
            object : NEUnitCallback() {
                override fun onError(code: Int, message: String?) {
                    callback.onError(code, message)
                }

                override fun onSuccess() {
                    callback.onSuccess(Unit)
                }
            }
        )
    }

    fun unmuteMyAudio(callback: NECallback2<Unit>) {
        val context = currentRoomContext
        if (context == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_ROOM_NOT_EXISTS)
            return
        }
        if (mapMember(context.localMember).isAudioBanned) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_MEMBER_AUDIO_BANNED)
            return
        }

        context.rtcController.unmuteMyAudio(object : NEUnitCallback() {
            override fun onError(code: Int, message: String?) {
                callback.onError(code, message)
            }
            override fun onSuccess() {
                callback.onSuccess(Unit)
            }
        })
    }

    fun muteMyVideo(callback: NECallback2<Unit>) {
        val context = currentRoomContext
        if (context == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_ROOM_NOT_EXISTS)
            return
        }

        context.rtcController.muteMyVideo(
            object : NEUnitCallback() {
                override fun onError(code: Int, message: String?) {
                    callback.onError(code, message)
                }

                override fun onSuccess() {
                    callback.onSuccess(Unit)
                }
            }
        )
    }

    fun unmuteMyVideo(callback: NECallback2<Unit>) {
        val context = currentRoomContext
        if (context == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_ROOM_NOT_EXISTS)
            return
        }
        if (mapMember(context.localMember).isAudioBanned) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_MEMBER_AUDIO_BANNED)
            return
        }

        context.rtcController.unmuteMyVideo(object : NEUnitCallback() {
            override fun onError(code: Int, message: String?) {
                callback.onError(code, message)
            }
            override fun onSuccess() {
                callback.onSuccess(Unit)
            }
        })
    }

    fun banRemoteAudio(userId: String, callback: NECallback2<Unit>) {
        val context = currentRoomContext
        if (context == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_ROOM_NOT_EXISTS)
            return
        }
        val member = context.getMember(userId)
        if (member == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_MEMBER_NOT_EXISTS)
            return
        }
        context.updateMemberProperty(
            userId,
            MemberPropertyConstants.CAN_OPEN_MIC_KEY,
            MemberPropertyConstants.CAN_OPEN_MIC_VALUE_NO,
            callback
        )
    }

    fun unbanRemoteAudio(userId: String, callback: NECallback2<Unit>) {
        val context = currentRoomContext
        if (context == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_ROOM_NOT_EXISTS)
            return
        }
        val member = context.getMember(userId)
        if (member == null) {
            callback.onError(NEErrorCode.FAILURE, ERROR_MSG_MEMBER_NOT_EXISTS)
            return
        }
        if (member.properties[MemberPropertyConstants.CAN_OPEN_MIC_KEY]
            == MemberPropertyConstants.CAN_OPEN_MIC_VALUE_NO
        ) {
            context.updateMemberProperty(
                userId,
                MemberPropertyConstants.CAN_OPEN_MIC_KEY,
                MemberPropertyConstants.CAN_OPEN_MIC_VALUE_YES,
                callback
            )
        } else {
            callback.onSuccess(Unit)
        }
    }

    fun enableEarBack(volume: Int): Int {
        val result = currentRoomContext?.rtcController?.enableEarBack(volume) ?: NEErrorCode.FAILURE
        if (result == 0) {
            isEarBackEnable = true
        }
        return result
    }

    fun adjustRecordingSignalVolume(volume: Int): Int {
        val result = currentRoomContext?.rtcController?.adjustRecordingSignalVolume(volume)
            ?: NEErrorCode.FAILURE
        if (result == NEErrorCode.SUCCESS) {
            recordingSignalVolume = volume
        }
        return result
    }

    fun getRecordingSignalVolume(): Int {
        LiveRoomLog.logApi("getRecordingSignalVolume")
        return recordingSignalVolume
    }

    fun adjustPlayMusicVolume(effectId: Int, volume: Int): Int {
        return currentRoomContext?.rtcController?.setEffectPlaybackVolume(effectId, volume)
            ?: NEErrorCode.FAILURE
    }

    fun disableEarBack(): Int {
        val result = currentRoomContext?.rtcController?.disableEarBack() ?: NEErrorCode.FAILURE
        if (result == 0) {
            isEarBackEnable = false
        }
        return result
    }

    fun getSeatInfo(callback: NECallback2<NESeatInfo>) {
        currentRoomContext?.seatController?.getSeatInfo(callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun getSeatRequestList(callback: NECallback2<List<NESeatRequestItem>>) {
        currentRoomContext?.seatController?.getSeatRequestList(callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun sendSeatInvitation(user: String, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.sendSeatInvitation(user, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun sendSeatInvitation(seatIndex: Int, user: String, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.sendSeatInvitation(seatIndex, user, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun acceptSeatInvitation(callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.acceptSeatInvitation(callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun rejectSeatInvitation(callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.rejectSeatInvitation(callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun submitSeatRequest(seatIndex: Int, exclusive: Boolean, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.submitSeatRequest(seatIndex, exclusive, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun submitSeatRequest(callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.submitSeatRequest(callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun cancelSeatRequest(callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.cancelSeatRequest(callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun leaveSeat(callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.leaveSeat(callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun approveSeatRequest(user: String, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.approveSeatRequest(user, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun rejectSeatRequest(user: String, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.rejectSeatRequest(user, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun kickSeat(user: String, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.kickSeat(user, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun openSeats(seatIndices: List<Int>, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.openSeats(seatIndices, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun closeSeats(seatIndices: List<Int>, callback: NECallback2<Unit>) {
        currentRoomContext?.seatController?.closeSeats(seatIndices, callback) ?: callback.onError(
            NEErrorCode.FAILURE,
            "roomContext is null"
        )
    }

    fun startAudioMixing(option: NERoomCreateAudioMixingOption): Int {
        return currentRoomContext?.rtcController?.startAudioMixing(
            NERoomCreateAudioMixingOption(
                option.path,
                option.loopCount,
                option.sendEnabled,
                option.sendVolume,
                option.playbackEnabled,
                option.playbackVolume,
                0,
                NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain
            )
        ) ?: NEErrorCode.FAILURE
    }

    fun pauseAudioMixing(): Int {
        return currentRoomContext?.rtcController?.pauseAudioMixing() ?: NEErrorCode.FAILURE
    }

    fun resumeAudioMixing(): Int {
        return currentRoomContext?.rtcController?.resumeAudioMixing() ?: NEErrorCode.FAILURE
    }

    fun stopAudioMixing(): Int {
        return currentRoomContext?.rtcController?.stopAudioMixing() ?: NEErrorCode.FAILURE
    }

    fun setAudioMixingVolume(volume: Int): Int {
        val sendResult = currentRoomContext?.rtcController?.setAudioMixingSendVolume(volume) ?: NEErrorCode.FAILURE
        val playbackResult = currentRoomContext?.rtcController?.setAudioMixingPlaybackVolume(volume) ?: NEErrorCode.FAILURE
        if (sendResult == NEErrorCode.SUCCESS && playbackResult == NEErrorCode.SUCCESS) {
            audioMixingVolume = volume
            return NEErrorCode.SUCCESS
        }
        return NEErrorCode.FAILURE
    }

    fun getAudioMixingVolume(): Int {
        return audioMixingVolume
    }

    fun playEffect(effectId: Int, option: NERoomCreateAudioEffectOption): Int {
        return currentRoomContext?.rtcController?.playEffect(
            effectId,
            NERoomCreateAudioEffectOption(
                option.path,
                option.loopCount,
                option.sendEnabled,
                option.sendVolume,
                option.playbackEnabled,
                option.playbackVolume,
                option.startTimestamp,
                option.progressInterval,
                if (option.sendWithAudioType == NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain)NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain else NERoomRtcAudioStreamType.NERtcAudioStreamTypeSub
            )
        ) ?: NEErrorCode.FAILURE
    }

    fun setEffectVolume(effectId: Int, volume: Int): Int {
        val sendResult = currentRoomContext?.rtcController?.setEffectSendVolume(effectId, volume)
        val playbackResult = currentRoomContext?.rtcController?.setEffectPlaybackVolume(
            effectId,
            volume
        ) ?: NEErrorCode.FAILURE
        if (sendResult == NEErrorCode.SUCCESS && playbackResult == NEErrorCode.SUCCESS) {
            effectVolume = volume
            return NEErrorCode.SUCCESS
        }
        return NEErrorCode.FAILURE
    }

    fun getEffectVolume(): Int {
        return effectVolume
    }

    fun stopAllEffect(): Int {
        return currentRoomContext?.rtcController?.stopAllEffects() ?: NEErrorCode.FAILURE
    }
    fun stopEffect(effectId: Int): Int {
        return currentRoomContext?.rtcController?.stopEffect(effectId) ?: NEErrorCode.FAILURE
    }

    fun removeListener(listener: NELiveStreamListener) {
        listeners.remove(listener)
        LiveRoomLog.d(TAG, "removeListener,listeners.size:" + listeners.size)
    }

    fun addListener(listener: NELiveStreamListener) {
        listeners.add(listener)
        LiveRoomLog.d(TAG, "addListener,listeners.size:" + listeners.size)
    }

    private fun mapMember(member: NERoomMember): NEVoiceRoomMember {
        return VoiceRoomMember(member)
    }

    private fun mapMemberVolumeInfo(memberVolumeInfo: NEMemberVolumeInfo): NEVoiceRoomMemberVolumeInfo {
        return VoiceRoomMemberVolumeInfo(memberVolumeInfo)
    }

    private fun addRoomListener() {
        roomListener = object : RoomListenerWrapper() {

            override fun onMemberRoleChanged(member: NERoomMember, oldRole: NERoomRole, newRole: NERoomRole) {
                listeners.forEach {
                    it.onMemberRoleChanged(member, oldRole, newRole)
                }
            }

            override fun onRtcChannelError(code: Int) {
                LiveRoomLog.e(TAG, "onRtcChannelError code = $code")
                listeners.forEach {
                    it.onRtcChannelError(code)
                }
            }

            override fun onMemberJoinRoom(members: List<NERoomMember>) {
                listeners.forEach {
                    it.onMemberJoinRoom(members)
                }
            }

            override fun onMemberLeaveRoom(members: List<NERoomMember>) {
                listeners.forEach {
                    it.onMemberLeaveRoom(members)
                }
            }

            override fun onMemberJoinChatroom(members: List<NERoomMember>) {
                listeners.forEach {
                    it.onMemberJoinChatroom(members)
                }
            }

            override fun onMemberLeaveChatroom(members: List<NERoomMember>) {
                listeners.forEach {
                    it.onMemberLeaveChatroom(members)
                }
            }

            override fun onMemberJoinRtcChannel(members: List<NERoomMember>) {
                listeners.forEach {
                    it.onMemberJoinRtcChannel(members)
                }
            }

            override fun onMemberLeaveRtcChannel(members: List<NERoomMember>) {
                listeners.forEach {
                    it.onMemberLeaveRtcChannel(members)
                }
            }

            override fun onRoomEnded(reason: NERoomEndReason) {
                listeners.forEach {
                    it.onRoomEnded(reason)
                }
            }

            override fun onAudioEffectFinished(effectId: Int) {
                listeners.forEach {
                    it.onAudioEffectFinished(effectId)
                }
            }

            override fun onAudioEffectTimestampUpdate(effectId: Long, timeStampMS: Long) {
                listeners.forEach {
                    it.onAudioEffectTimestampUpdate(effectId, timeStampMS)
                }
            }

            override fun onRtcLocalAudioVolumeIndication(volume: Int, vadFlag: Boolean) {
                listeners.forEach {
                    it.onRtcLocalAudioVolumeIndication(volume, vadFlag)
                }
            }
            override fun onRtcRemoteAudioVolumeIndication(
                volumes: List<NEMemberVolumeInfo>,
                totalVolume: Int
            ) {
                listeners.forEach {
                    it.onRtcRemoteAudioVolumeIndication(volumes, totalVolume)
                }
            }

            override fun onRtcAudioOutputDeviceChanged(device: NEAudioOutputDevice) {
                listeners.forEach {
                    it.onRtcAudioOutputDeviceChanged(device)
                }
            }

            override fun onMemberAudioMuteChanged(
                member: NERoomMember,
                mute: Boolean,
                operateBy: NERoomMember?
            ) {
                listeners.forEach {
                    it.onMemberAudioMuteChanged(member, mute, operateBy)
                }
            }

            override fun onReceiveChatroomMessages(messages: List<NERoomChatMessage>) {
                messages.forEach {
                    if (it is NERoomChatTextMessage) {
                        listeners.forEach { listener ->
                            listener.onReceiveTextMessage(it)
                        }
                    } else if (it is NERoomChatNotificationMessage) {
                        if (it.members != null) {
                            if (it.eventType == NERoomChatEventType.ENTER) {
                                listeners.forEach { listener ->
                                    listener.onMemberJoinChatroom2(it.members!!)
                                }
                            } else if (it.eventType == NERoomChatEventType.EXIT) {
                                listeners.forEach { listener ->
                                    listener.onMemberLeaveChatroom2(it.members!!)
                                }
                            }
                        }
                    } else if (it is RoomCustomMessages) {
                        when (getType(it.attachStr)) {
                            TYPE_BATCH_GIFT -> {
                                val result = GsonUtils.fromJson(
                                    it.attachStr,
                                    VoiceRoomBatchGiftModel::class.java
                                )
                                listeners.forEach { listener ->
                                    LiveRoomLog.i(
                                        TAG,
                                        "onReceiveBatchGift customAttachment:${it.attachStr}"
                                    )
                                    listener.onReceiveBatchGift(result.data)
                                }
                            }
                            TYPE_LIVE_PAUSE -> {
                                listeners.forEach { listener ->
                                    LiveRoomLog.i(
                                        TAG,
                                        "onLivePause customAttachment:${it.attachStr}"
                                    )
                                    listener.onLivePause()
                                }
                            }

                            TYPE_LIVE_RESUME -> {
                                listeners.forEach { listener ->
                                    LiveRoomLog.i(
                                        TAG,
                                        "onLiveResume customAttachment:${it.attachStr}"
                                    )
                                    listener.onLiveResume()
                                }
                            }
                        }
                    }
                }
            }

            override fun onChatroomMessageAttachmentProgress(
                messageUuid: String,
                transferred: Long,
                total: Long
            ) {
            }

            override fun onRoomConnectStateChanged(state: NERoomConnectType) {
            }

            override fun onAudioMixingStateChanged(reason: Int) {
                LiveRoomLog.d(TAG, "onAudioMixingStateChanged,reason:$reason")
                listeners.forEach {
                    it.onAudioMixingStateChanged(reason)
                }
            }
        }
        currentRoomContext?.addRoomListener(roomListener!!)
        LiveRoomLog.d(TAG, "addRoomListener,roomListener:$roomListener")
    }

    private fun addSeatListener() {
        seatListener = object : NESeatEventListener() {
            override fun onSeatInvitationReceived(seatIndex: Int, user: String, operateBy: String) {
                LiveRoomLog.d(
                    TAG,
                    "onSeatInvitationReceived seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
                listeners.forEach {
                    it.onSeatInvitationReceived(seatIndex, user, operateBy)
                }
            }

            override fun onSeatInvitationAccepted(
                seatIndex: Int,
                user: String,
                isAutoAgree: Boolean
            ) {
                LiveRoomLog.d(
                    TAG,
                    "onSeatInvitationAccepted seatIndex = $seatIndex user = $user isAutoAgree = $isAutoAgree"
                )
                listeners.forEach {
                    it.onSeatInvitationAccepted(seatIndex, user, isAutoAgree)
                }
            }

            override fun onSeatRequestApproved(
                seatIndex: Int,
                user: String,
                operateBy: String,
                isAutoAgree: Boolean
            ) {
                LiveRoomLog.d(
                    TAG,
                    "onSeatRequestApproved seatIndex = $seatIndex user = $user operateBy = $operateBy isAutoAgree = $isAutoAgree"
                )
                listeners.forEach {
                    it.onSeatRequestApproved(seatIndex, user, operateBy, isAutoAgree)
                }
            }

            override fun onSeatRequestCancelled(seatIndex: Int, user: String) {
                LiveRoomLog.d(TAG, "onSeatRequestCancelled seatIndex = $seatIndex user = $user")
                listeners.forEach {
                    it.onSeatRequestCancelled(seatIndex, user)
                }
            }

            override fun onSeatInvitationCancelled(
                seatIndex: Int,
                user: String,
                operateBy: String
            ) {
                LiveRoomLog.d(
                    TAG,
                    "onSeatInvitationCancelled seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
                listeners.forEach {
                    it.onSeatInvitationCancelled(seatIndex, user, operateBy)
                }
            }

            override fun onSeatInvitationRejected(seatIndex: Int, user: String) {
                LiveRoomLog.d(TAG, "onSeatInvitationRejected seatIndex = $seatIndex user = $user")
                listeners.forEach {
                    it.onSeatInvitationRejected(seatIndex, user)
                }
            }

            override fun onSeatKicked(seatIndex: Int, user: String, operateBy: String) {
                LiveRoomLog.d(
                    TAG,
                    "onSeatKicked seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
                listeners.forEach {
                    it.onSeatKicked(seatIndex, user, operateBy)
                }
            }

            override fun onSeatLeave(seatIndex: Int, user: String) {
                LiveRoomLog.d(
                    TAG,
                    "onSeatLeave seatIndex = $seatIndex user = $user,member:${currentRoomContext?.getMember(
                        user
                    )}"
                )

                listeners.forEach {
                    it.onSeatLeave(seatIndex, user)
                }
            }

            override fun onSeatListChanged(seatItems: List<NESeatItem>) {
                LiveRoomLog.d(TAG, "onSeatListChanged seatItems = $seatItems")
                handleSeatListItemChanged(seatItems)
                listeners.forEach {
                    it.onSeatListChanged(
                        seatItems
                    )
                }
            }

            override fun onSeatRequestRejected(seatIndex: Int, user: String, operateBy: String) {
                LiveRoomLog.d(
                    TAG,
                    "onSeatRequestRejected seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
                listeners.forEach {
                    it.onSeatRequestRejected(seatIndex, user, operateBy)
                }
            }

            override fun onSeatRequestSubmitted(seatIndex: Int, user: String) {
                LiveRoomLog.d(TAG, "onSeatRequestSubmitted seatIndex = $seatIndex user = $user")

                listeners.forEach {
                    it.onSeatRequestSubmitted(seatIndex, user)
                }
            }

            override fun onSeatManagerAdded(managers: List<String>) {
                LiveRoomLog.d(TAG, "onSeatManagerAdded managers = $managers")
            }

            override fun onSeatManagerRemoved(managers: List<String>) {
                LiveRoomLog.d(TAG, "onSeatManagerRemoved managers = $managers")
            }
        }

        currentRoomContext?.seatController?.addSeatListener(seatListener!!)
        LiveRoomLog.d(TAG, "addSeatListener,seatListener:$seatListener")
    }

    private fun handleSeatListItemChanged(seatItems: List<NESeatItem>) {
        LiveRoomLog.d(TAG, "handleSeatListItemChanged,seatItems:${seatItems.size}")
        isLocalOnSeat = isCurrentOnSeat(seatItems)
        currentRoomContext?.rtcController?.setClientRole(
            if (isLocalOnSeat) NERoomRtcClientRole.BROADCASTER else NERoomRtcClientRole.AUDIENCE
        )

        val context = currentRoomContext ?: return
        val myUuid = context.localMember.uuid
        val old = onSeatItems?.firstOrNull { it.user == myUuid }
        val now = seatItems.firstOrNull { it.user == myUuid }
        if ((old == null || old.status != NESeatItemStatus.TAKEN) && now != null && now.status == NESeatItemStatus.TAKEN) {
            unmuteMyAudio(EmptyCallback)
        }
        onSeatItems = seatItems
    }

    private fun isCurrentOnSeat(seatItems: List<NESeatItem>): Boolean {
        var currentOnSeat = false
        seatItems.forEach {
            if ((it.status == NESeatItemStatus.TAKEN || it.status == NESeatItemStatus.ON_TAKEN) &&
                TextUtils.equals(currentRoomContext?.localMember?.uuid, it.user)
            ) {
                currentOnSeat = true
            }
        }
        return currentOnSeat
    }

    fun getOnSeatList(): List<NESeatItem> {
        val onSeatList = ArrayList<NESeatItem>()
        onSeatItems?.forEach {
            if (it.status == NESeatItemStatus.TAKEN) {
                onSeatList.add(it)
            }
        }
        return onSeatList
    }

    private fun syncLocalAudioState(mute: Boolean) {
        currentRoomContext?.rtcController?.setRecordDeviceMute(mute)
    }

    private fun getType(json: String): Int? {
        val jsonObject: JsonObject = GsonUtils.fromJson(
            json,
            JsonObject::class.java
        )
        return jsonObject["type"]?.asInt
    }

    fun setPlayingPosition(effectId: Int, position: Long): Int {
        if (currentRoomContext == null) {
            return NEErrorCode.FAILURE
        }
        return currentRoomContext!!.rtcController.setEffectPosition(
            effectId,
            position
        )
    }

    fun pauseEffect(effectId: Int): Int {
        if (currentRoomContext == null) {
            return NEErrorCode.FAILURE
        }
        return currentRoomContext!!.rtcController.pauseEffect(effectId)
    }

    fun resumeEffect(effectId: Int): Int {
        if (currentRoomContext == null) {
            return NEErrorCode.FAILURE
        }
        return currentRoomContext!!.rtcController.resumeEffect(effectId)
    }

    fun switchCamera(): Int {
        if (currentRoomContext == null) {
            return NEErrorCode.FAILURE
        }
        return currentRoomContext!!.rtcController.switchCamera()
    }

    fun changeMemberRole(userUuid: String, role: String, callback: NECallback<Unit>) {
        if (currentRoomContext == null) {
            callback.onResult(NEErrorCode.FAILURE, "", null)
        }
        currentRoomContext!!.changeMemberRole(userUuid, role, callback)
    }

    fun setupLocalVideoCanvas(videoView: NERoomVideoView?): Int {
        if (currentRoomContext == null) {
            return NEErrorCode.FAILURE
        }
        return currentRoomContext!!.rtcController.setupLocalVideoCanvas(videoView)
    }

    fun setupRemoteVideoCanvas(videoView: NERoomVideoView?, userUuid: String): Int {
        if (currentRoomContext == null) {
            return NEErrorCode.FAILURE
        }
        return currentRoomContext!!.rtcController.setupRemoteVideoCanvas(videoView, userUuid)
    }

    fun enableAudioVolumeIndication(enable: Boolean, interval: Int): Int {
        if (currentRoomContext == null) {
            return NEErrorCode.FAILURE
        }
        return currentRoomContext!!.rtcController.enableAudioVolumeIndication(enable, interval)
    }
}

internal open class RoomListenerWrapper : NERoomListenerAdapter() {
    override fun onRoomPropertiesChanged(properties: Map<String, String>) {
    }

    override fun onRoomPropertiesDeleted(properties: Map<String, String>) {
    }

    override fun onMemberRoleChanged(
        member: NERoomMember,
        oldRole: NERoomRole,
        newRole: NERoomRole
    ) {
    }

    override fun onMemberNameChanged(member: NERoomMember, name: String, operateBy: NERoomMember?) {
    }

    override fun onMemberPropertiesChanged(member: NERoomMember, properties: Map<String, String>) {
    }

    override fun onMemberPropertiesDeleted(member: NERoomMember, properties: Map<String, String>) {
    }

    override fun onMemberJoinRoom(members: List<NERoomMember>) {
    }

    override fun onMemberLeaveRoom(members: List<NERoomMember>) {
    }

    override fun onRoomEnded(reason: NERoomEndReason) {
    }

    override fun onRoomLockStateChanged(isLocked: Boolean) {
    }

    override fun onMemberJoinRtcChannel(members: List<NERoomMember>) {
    }

    override fun onMemberLeaveRtcChannel(members: List<NERoomMember>) {
    }

    override fun onRtcChannelError(code: Int) {
    }

    override fun onRtcRecvSEIMsg(uuid: String, seiMsg: String) {
    }

    override fun onRtcRemoteAudioVolumeIndication(volumes: List<NEMemberVolumeInfo>, totalVolume: Int) {
    }

    override fun onRtcLocalAudioVolumeIndication(volume: Int, vadFlag: Boolean) {
    }

    override fun onRtcAudioOutputDeviceChanged(device: NEAudioOutputDevice) {
    }

    override fun onMemberJoinChatroom(members: List<NERoomMember>) {
    }

    override fun onMemberLeaveChatroom(members: List<NERoomMember>) {
    }

    override fun onMemberAudioMuteChanged(
        member: NERoomMember,
        mute: Boolean,
        operateBy: NERoomMember?
    ) {
    }

    override fun onMemberVideoMuteChanged(
        member: NERoomMember,
        mute: Boolean,
        operateBy: NERoomMember?
    ) {
    }

    override fun onMemberScreenShareStateChanged(
        member: NERoomMember,
        isSharing: Boolean,
        operateBy: NERoomMember?
    ) {
    }

    override fun onReceiveChatroomMessages(messages: List<NERoomChatMessage>) {
    }

    override fun onAudioEffectFinished(effectId: Int) {
    }

    override fun onAudioEffectTimestampUpdate(effectId: Long, timeStampMS: Long) {
    }

    override fun onAudioMixingStateChanged(reason: Int) {
    }

    override fun onChatroomMessageAttachmentProgress(
        messageUuid: String,
        transferred: Long,
        total: Long
    ) {
    }

    override fun onMemberWhiteboardStateChanged(
        member: NERoomMember,
        isSharing: Boolean,
        operateBy: NERoomMember?
    ) {
    }

    override fun onWhiteboardShowFileChooser(
        types: Array<String>,
        callback: NEValueCallback<Array<Uri>?>
    ) {
    }

    override fun onRoomRemainingSecondsRenewed(remainingSeconds: Long) {
    }

    override fun onRoomConnectStateChanged(state: NERoomConnectType) {
    }

    override fun onWhiteboardError(code: Int, message: String) {
    }

    override fun onRoomLiveStateChanged(state: NERoomLiveState) {
    }

    override fun onRtcVirtualBackgroundSourceEnabled(enabled: Boolean, reason: Int) {
    }

    override fun onRtcLastmileQuality(quality: Int) {
    }

    override fun onRtcLastmileProbeResult(result: NERoomRtcLastmileProbeResult) {
    }
}

internal object MemberPropertyConstants {
    // 成员是否可以开启麦克风。如果值为 [CAN_OPEN_MIC_VALUE_NO]，表示不能开启麦克风。
    const val CAN_OPEN_MIC_KEY = "canOpenMic"
    const val CAN_OPEN_MIC_VALUE_NO = "0"
    const val CAN_OPEN_MIC_VALUE_YES = "1"
}

internal object EmptyCallback : NECallback2<Unit>()
