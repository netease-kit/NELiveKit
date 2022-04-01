/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.app.newlive.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.netease.yunxin.app.newlive.beauty.BeautyControl
import com.netease.yunxin.app.newlive.chatroom.ChatRoomMsgCreator
import com.netease.yunxin.app.newlive.live.LiveKitManager
import com.netease.yunxin.app.newlive.live.MyLiveListener
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.livekit.VideoFrame
import com.netease.yunxin.kit.livekit.model.ErrorInfo
import com.netease.yunxin.kit.livekit.model.LiveInfo
import com.netease.yunxin.kit.livekit.model.RewardMsg
import com.netease.yunxin.kit.livekit.model.pk.*
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.NERoomTextMessage

/**
 * viewModel for [AnchorBaseLiveActivity]
 */
class LiveBaseViewModel : ViewModel() {

    companion object {
        const val LOG_TAG = "LiveBaseViewModel"
    }

    var beautyControl: BeautyControl? = null

    val errorData = MutableLiveData<ErrorInfo>()

    val chatRoomMsgData = MutableLiveData<CharSequence>()

    val userAccountData = MutableLiveData<Int>()

    val rewardData = MutableLiveData<RewardMsg>()

    val audioEffectFinishData = MutableLiveData<Int>()

    val audioMixingFinishData = MutableLiveData<Boolean>()

    val audienceData = MutableLiveData<List<NERoomMember>>()

    val kickedOutData = MutableLiveData<Boolean>()

    var liveInfo: LiveInfo? = null

    var liveListener = object : MyLiveListener() {
        override fun onTextMessageReceived(message: NERoomTextMessage) {
            val content = message.text
            ALog.d(LOG_TAG,"onRecvRoomTextMsg :${message.fromAccount}")
            chatRoomMsgData.postValue(ChatRoomMsgCreator.createText(isAnchor(message.fromAccount), message.fromAccount, content))
        }
        override fun onRewardReceived(rewardMsg: RewardMsg) {
            rewardData.value = rewardMsg
        }

        override fun onLiveStarted() {
        }

        override fun onVideoFrameCallback(videoFrame: VideoFrame): Int {
            return beautyControl?.onDrawFrame(
                videoFrame.data, videoFrame.textureId, videoFrame.width,
                videoFrame.height)?: 0
        }

        override fun onMembersJoin(members: List<NERoomMember>) {
            for(member in members) {
                ALog.d(LOG_TAG, "onUserEntered :${member.name}")
                chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomEnter(member.uuid))
            }
        }
        override fun onMembersLeave(members: List<NERoomMember>) {
            for(member in members) {
                ALog.d(LOG_TAG, "onUserLeft :$member.name")
                chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomExit(member.uuid))

            }
        }

        override fun onMemberCountChange(members: MutableList<NERoomMember>?) {
            userAccountData.postValue(members?.size?: 0)
            audienceData.postValue(members)
        }

        override fun onLoginKickedOut() {
            kickedOutData.postValue(true)
        }

        override fun onLiveEnd(reason: Int) {

        }

        override fun onError(code: Int) {
            errorData.postValue(ErrorInfo(code = -1, msg = "直播错误", serious = true))
        }
    }

    fun refreshLiveInfo(liveInfo: LiveInfo) {
        this.liveInfo = liveInfo
    }

    private fun isAnchor(fromAccount : String): Boolean{
        return liveInfo?.anchor?.userUuid == fromAccount
    }

    fun init() {
        LiveKitManager.getInstance().addLiveListener(liveListener)
    }

    fun destroy(){
        LiveKitManager.getInstance().removeLiveListener(liveListener)
    }
}