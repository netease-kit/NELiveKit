/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.impl.manager

import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.livestreamkit.api.LiveRoomRole
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamListener
import com.netease.yunxin.kit.livestreamkit.impl.Constants
import com.netease.yunxin.kit.livestreamkit.impl.model.ConnectionUser
import com.netease.yunxin.kit.livestreamkit.impl.model.RequestConnectionResponse
import com.netease.yunxin.kit.livestreamkit.impl.service.LiveRoomHttpService
import com.netease.yunxin.kit.livestreamkit.impl.service.LiveRoomHttpServiceImpl
import com.netease.yunxin.kit.livestreamkit.impl.service.LiveRoomService
import com.netease.yunxin.kit.livestreamkit.impl.state.CoHostState
import com.netease.yunxin.kit.livestreamkit.impl.utils.GsonUtils
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomLog
import com.netease.yunxin.kit.livestreamkit.impl.utils.LiveRoomUtils
import com.netease.yunxin.kit.roomkit.api.NECallback2
import com.netease.yunxin.kit.roomkit.api.NERoomChatMessage
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.impl.model.RoomCustomMessages
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 主播和主播连线管理
 */
class CoHostManager(val roomService: LiveRoomService) {

    companion object {
        private const val TAG = "CoHostManager"
    }

    private val roomHttpService: LiveRoomHttpService by lazy { LiveRoomHttpServiceImpl }
    var coHostState: CoHostState = CoHostState()

    private val coHostListeners: CopyOnWriteArrayList<NECoHostListener> by lazy {
        CopyOnWriteArrayList()
    }

    init {
        roomService.addListener(object : NELiveStreamListener() {
            override fun onRemoteMemberJoinRtcChannel(members: List<NERoomMember>) {
                handleMemberJoin(members)
            }

            override fun onRemoteMemberLeaveRtcChannel(members: List<NERoomMember>) {
                handleMemberLeave(members)
            }

            override fun onRemoteMemberLeaveRoom(members: List<NERoomMember>) {
                handleMemberLeave(members)
            }

            override fun onReceiveChatroomMessages(messages: List<NERoomChatMessage>) {
                messages.forEach {
                    if (it is RoomCustomMessages) {
                        LiveRoomLog.i(
                            TAG,
                            "onReceiveChatroomMessages ${it.attachStr}"
                        )
                        when (LiveRoomUtils.getType(it.attachStr)) {
                            Constants.TYPE_LIVE_CO_REQUEST_RECEIVE -> {
                                val result = GsonUtils.fromJson(
                                    LiveRoomUtils.getData(it.attachStr),
                                    ConnectionRequestReceivedModel::class.java
                                )
                                handleConnectionRequestReceived(result)
                            }

                            Constants.TYPE_LIVE_CO_REQUEST_CANCEL -> {
                                val result = GsonUtils.fromJson(
                                    LiveRoomUtils.getData(it.attachStr),
                                    ConnectionUser::class.java
                                )

                                handleConnectionRequestCancelled(result)
                            }

                            Constants.TYPE_LIVE_CO_REQUEST_ACCEPT -> {
                                val result = GsonUtils.fromJson(
                                    LiveRoomUtils.getData(it.attachStr),
                                    ConnectionRequestAcceptModel::class.java
                                )

                                handleConnectionRequestAccepted(result)
                            }

                            Constants.TYPE_LIVE_CO_REQUEST_REJECT -> {
                                val result = GsonUtils.fromJson(
                                    LiveRoomUtils.getData(it.attachStr),
                                    ConnectionUser::class.java
                                )

                                handleConnectionRequestReject(result)
                            }

                            Constants.TYPE_LIVE_CO_HOST_DISCONNECT -> {
                                val result = GsonUtils.fromJson(
                                    LiveRoomUtils.getData(it.attachStr),
                                    ConnectionDisconnectModel::class.java
                                )

                                handleConnectionDisconnect(result)
                            }

                            Constants.TYPE_LIVE_CO_REQUEST_TIMEOUT -> {
                                val result = GsonUtils.fromJson(
                                    LiveRoomUtils.getData(it.attachStr),
                                    ConnectionRequestTimeoutModel::class.java
                                )

                                handleConnectionRequestTimeout(result)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun handleMemberJoin(members: List<NERoomMember>) {
        val joinedList = mutableListOf<ConnectionUser>()
        members.forEach {
            if (it.role.name == LiveRoomRole.ROLE_INVITE_HOST) {
                it.relayInfo?.let { relayInfo ->
                    joinedList.add(
                        ConnectionUser(relayInfo.fromRoomUuid, it.uuid, it.name, it.avatar)
                    )
                }
            }
        }
        coHostState.connectedUserList.addAll(joinedList)
        val leavedList = emptyList<ConnectionUser>()
        notifyConnectionUserListChanged(joinedList, leavedList)
    }

    private fun handleMemberLeave(members: List<NERoomMember>) {
        val leavedList = mutableListOf<ConnectionUser>()
        members.forEach { member ->
            LiveRoomLog.i(TAG, "onRemoteMemberLeaveRtcChannel member: $member")
            if (member.role.name == LiveRoomRole.ROLE_INVITE_HOST) {
                // 如果members中，有存在在coHostState.connectedUserList中的成员，则移除
                coHostState.connectedUserList.get().firstOrNull { user ->
                    user.userUuid == member.uuid // 假设是通过 uuid 匹配用户
                }?.let { removedUser ->
                    roomService.stopChannelMediaRelay(removedUser.roomUuid)
                    coHostState.connectedUserList.remove(removedUser)
                    leavedList.add(removedUser)
                }
            }
        }
        val joinedList = emptyList<ConnectionUser>()
        notifyConnectionUserListChanged(joinedList, leavedList)
    }

    private fun notifyConnectionUserListChanged(joinedList: List<ConnectionUser> = emptyList(), leavedList: List<ConnectionUser> = emptyList()) {
        LiveRoomLog.i(
            TAG,
            "onConnectionUserListChanged connectionUsers: ${coHostState.connectedUserList.get()} joinedList: $joinedList leavedList: $leavedList"
        )
        coHostListeners.forEach { listener ->
            listener.onConnectionUserListChanged(
                coHostState.connectedUserList.get(),
                joinedList,
                leavedList
            )
        }
    }

    private fun handleConnectionRequestReceived(result: ConnectionRequestReceivedModel) {
        LiveRoomLog.i(
            TAG,
            "handleConnectionRequestReceived inviter:${result.inviter} inviteeList: ${result.inviteeList}"
        )
        coHostListeners.forEach { listener ->
            listener.onConnectionRequestReceived(result.inviter, result.inviteeList, result.ext)
        }
    }

    private fun handleConnectionRequestCancelled(result: ConnectionUser) {
        LiveRoomLog.i(
            TAG,
            "handleConnectionRequestCancelled inviter: $result"
        )

        coHostListeners.forEach { listener ->
            listener.onConnectionRequestCancelled(result)
        }
    }

    private fun handleConnectionRequestAccepted(result: ConnectionRequestAcceptModel) {
        LiveRoomLog.i(
            TAG,
            "handleConnectionRequestAccepted invitee:$result"
        )
        result.inviteeList.forEach {
            roomService.startChannelMediaRelay(
                it.roomUuid,
                object : NECallback2<Unit>() {
                    override fun onSuccess(data: Unit?) {
                        LiveRoomLog.i(TAG, "startChannelMediaRelay success")
                    }

                    override fun onError(code: Int, message: String?) {
                        LiveRoomLog.e(
                            TAG,
                            "startChannelMediaRelay failed code: $code message: $message"
                        )
                    }
                }
            )
        }

        coHostListeners.forEach { listener ->
            result.inviteeList.forEach {
                listener.onConnectionRequestAccept(it)
            }
        }
    }

    private fun handleConnectionRequestReject(result: ConnectionUser) {
        LiveRoomLog.i(
            TAG,
            "handleConnectionRequestReject invitee: $result"
        )
        coHostListeners.forEach { listener ->
            listener.onConnectionRequestReject(result)
        }
    }

    private fun handleConnectionDisconnect(result: ConnectionDisconnectModel) {
        LiveRoomLog.i(
            TAG,
            "handleConnectionDisconnect operator: ${result.operator}, connectedList: ${result.connectedList}, ext: ${result.ext}"
        )

        val leavedList = mutableListOf<ConnectionUser>()
        var removedUser: ConnectionUser? = null
        coHostState.connectedUserList.get().forEach {
            if (it.userUuid == result.operator.userUuid) {
                removedUser = it
            }
        }
        removedUser?.let {
            leavedList.add(it)
        }

        notifyConnectionUserListChanged(leavedList = leavedList)
    }

    private fun handleConnectionRequestTimeout(result: ConnectionRequestTimeoutModel) {
        LiveRoomLog.i(
            TAG,
            "handleConnectionRequestTimeout inviter: ${result.inviter}, invitee: ${result.inviteeList}"
        )
        coHostListeners.forEach { listener ->
            listener.onConnectionRequestTimeout(result.inviter, result.inviteeList, result.ext)
        }
    }

    /*
     * 添加监听器
     */
    fun addListener(listener: NECoHostListener) {
        coHostListeners.add(listener)
    }

    /**
     * 移除监听器
     */
    fun removeListener(listener: NECoHostListener) {
        coHostListeners.remove(listener)
    }

    /**
     * 请求主播连线
     * @param roomUuid 房间id
     * @param timeoutSeconds 超时时间
     * @param callback 回调
     */
    fun requestConnection(roomUuid: String, timeoutSeconds: Long, ext: String?, callback: NELiveStreamCallback<Unit>) {
        LiveRoomLog.i(
            TAG,
            "requestConnection roomUuid: $roomUuid timeoutSeconds: $timeoutSeconds ext: $ext"
        )

        roomHttpService.requestHostConnection(
            listOf(roomUuid),
            timeoutSeconds,
            ext,
            object : NetRequestCallback<RequestConnectionResponse> {

                override fun success(info: RequestConnectionResponse?) {
                    LiveRoomLog.i(TAG, "requestHostConnection success")
                    info?.let {
                        callback.onSuccess(null)
                    }
                }

                override fun error(code: Int, msg: String?) {
                    LiveRoomLog.e(TAG, "requestHostConnection failed code: $code message: $msg")
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    /**
     * 取消请求主播连线
     * @param roomUuid 房间id
     * @param callback 回调
     */
    fun cancelConnectionRequest(roomUuid: String, callback: NELiveStreamCallback<Unit>) {
        LiveRoomLog.i(
            TAG,
            "cancelRequest roomUuid: $roomUuid"
        )
        roomHttpService.cancelRequestHostConnection(
            listOf(roomUuid),
            object : NetRequestCallback<Unit> {

                override fun success(info: Unit?) {
                    callback.onSuccess(null)
                }

                override fun error(code: Int, msg: String?) {
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    /**
     * 接受主播连线邀请
     * @param roomUuid 房间id
     * @param callback 回调
     */
    fun acceptConnection(roomUuid: String, callback: NELiveStreamCallback<Unit>) {
        LiveRoomLog.i(TAG, "accept roomUuid: $roomUuid")
        roomHttpService.acceptRequestHostConnection(
            roomUuid,
            object : NetRequestCallback<Unit> {
                override fun success(info: Unit?) {
                    roomService.startChannelMediaRelay(
                        roomUuid,
                        object : NECallback2<Unit>() {
                            override fun onSuccess(data: Unit?) {
                                callback.onSuccess(null)
                            }

                            override fun onError(code: Int, message: String?) {
                                callback.onFailure(code, message)
                            }
                        }
                    )
                }

                override fun error(code: Int, msg: String?) {
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    /**
     * 拒绝主播连线邀请
     * @param roomUuid 房间id
     * @param callback 回调
     */
    fun rejectConnection(roomUuid: String, callback: NELiveStreamCallback<Unit>) {
        LiveRoomLog.i(
            TAG,
            "reject roomUuid: $roomUuid"
        )
        roomHttpService.rejectRequestHostConnection(
            roomUuid,
            object : NetRequestCallback<Unit> {

                override fun success(info: Unit?) {
                    callback.onSuccess(null)
                }

                override fun error(code: Int, msg: String?) {
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    /**
     * 退出房间连线
     * 调用该接口会退出房间连线状态，仅限已连线的状态下调用。
     */
    fun disconnect() {
        LiveRoomLog.i(
            TAG,
            "disconnect"
        )
        roomHttpService.disconnectHostConnection(object : NetRequestCallback<Unit> {

            override fun success(info: Unit?) {
            }

            override fun error(code: Int, msg: String?) {
            }
        })
        val leavedList = mutableListOf<ConnectionUser>()
        leavedList.addAll(coHostState.connectedUserList.get())
        coHostState.connectedUserList.get().forEach {
            roomService.stopChannelMediaRelay(
                it.roomUuid,
                object : NECallback2<Unit>() {
                    override fun onSuccess(data: Unit?) {
                        LiveRoomLog.i(TAG, "stopChannelMediaRelay success")
                    }

                    override fun onError(code: Int, message: String?) {
                        LiveRoomLog.e(
                            TAG,
                            "stopChannelMediaRelay failed code:$code message:$message"
                        )
                    }
                }
            )
        }
        coHostState.connectedUserList.clear()
        notifyConnectionUserListChanged(leavedList = leavedList)
    }
}

/**
 * 连线邀请信息
 * @param inviter 邀请者信息。
 * @param inviteeList 被邀请连线的用户列表。
 * @param ext 透传信息。
 */
data class ConnectionRequestReceivedModel(
    var inviter: ConnectionUser,
    var inviteeList: List<ConnectionUser>,
    var ext: String? = null
)

/**
 * 连线邀请接受信息
 * @param inviteeList 被邀请连线的用户列表。
 */
data class ConnectionRequestAcceptModel(
    var inviteeList: List<ConnectionUser>
)

/**
 * 连线结束信息
 * @param operator 操作者信息。
 * @param connectedList 被邀请连线的用户列表。
 * @param ext 透传信息。
 */
data class ConnectionDisconnectModel(
    var operator: ConnectionUser,
    var connectedList: List<ConnectionUser>,
    var ext: String? = null
)

/**
 * 连线邀请超时信息
 * @param inviter 邀请者信息。
 * @param inviteeList 被邀请连线的用户列表。
 * @param ext 透传信息。
 */
data class ConnectionRequestTimeoutModel(
    var inviter: ConnectionUser,
    var inviteeList: List<ConnectionUser>,
    var ext: String? = null
)

interface NECoHostListener {
    /**
     * 收到连线用户列表发生变化
     *
     * @param connectedList 已连线的用户列表。
     * @param joinedList 新加入连线的用户列表。
     * @param leavedList 退出连线的用户列表。
     */
    fun onConnectionUserListChanged(connectedList: List<ConnectionUser>, joinedList: List<ConnectionUser>, leavedList: List<ConnectionUser>)

    /**
     * 接收端收到连线邀请的回调
     *
     * @param inviter 邀请者信息。
     * @param inviteeList 被邀请连线的用户列表。
     * @param ext 透传信息。
     */
    fun onConnectionRequestReceived(inviter: ConnectionUser, inviteeList: List<ConnectionUser>, ext: String?)

    /**
     * 邀请取消回调
     *
     * @param inviter 邀请者信息。
     */
    fun onConnectionRequestCancelled(inviter: ConnectionUser)

    /**
     * 邀请被接受回调
     *
     * @param invitee 被邀请者的用户信息。
     */
    fun onConnectionRequestAccept(invitee: ConnectionUser)

    /**
     * 邀请被拒绝回调
     *
     * @param invitee 被邀请者的用户信息。
     */
    fun onConnectionRequestReject(invitee: ConnectionUser)

    /**
     * 邀请超时回调
     *
     * @param inviter 邀请者信息。
     * @param inviteeList 被邀请连线的用户列表。
     * @param ext 透传信息。
     */
    fun onConnectionRequestTimeout(inviter: ConnectionUser, inviteeList: List<ConnectionUser>, ext: String?)
}
