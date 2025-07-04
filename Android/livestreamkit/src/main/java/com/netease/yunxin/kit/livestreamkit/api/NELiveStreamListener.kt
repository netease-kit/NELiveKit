/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.livestreamkit.api

import android.net.Uri
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomBatchGiftModel
import com.netease.yunxin.kit.roomkit.api.NERoomCaptionMessage
import com.netease.yunxin.kit.roomkit.api.NERoomChatMessage
import com.netease.yunxin.kit.roomkit.api.NERoomChatTextMessage
import com.netease.yunxin.kit.roomkit.api.NERoomEndReason
import com.netease.yunxin.kit.roomkit.api.NERoomListener
import com.netease.yunxin.kit.roomkit.api.NERoomLiveState
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.NERoomRole
import com.netease.yunxin.kit.roomkit.api.NEValueCallback
import com.netease.yunxin.kit.roomkit.api.model.NEAudioOutputDevice
import com.netease.yunxin.kit.roomkit.api.model.NEChatroomState
import com.netease.yunxin.kit.roomkit.api.model.NEMemberVolumeInfo
import com.netease.yunxin.kit.roomkit.api.model.NERoomCloudRecordState
import com.netease.yunxin.kit.roomkit.api.model.NERoomConnectType
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcLastmileProbeResult
import com.netease.yunxin.kit.roomkit.api.model.NEYidunAntiSpamRes
import com.netease.yunxin.kit.roomkit.api.service.NESeatEventListener

open class NELiveStreamListener : NESeatEventListener(), NERoomListener {

    open fun onRemoteMemberJoinRoom(members: List<NERoomMember>) {
    }

    open fun onRemoteMemberLeaveRoom(members: List<NERoomMember>) {
    }

    open fun onLocalMemberJoinRtcChannel() {}

    open fun onRemoteMemberJoinRtcChannel(members: List<NERoomMember>) {}

    open fun onLocalMemberLeaveRtcChannel() {}

    open fun onRemoteMemberLeaveRtcChannel(members: List<NERoomMember>) {}

    open fun onMemberJoinChatroom2(members: List<NERoomMember>) {
    }

    open fun onMemberLeaveChatroom2(members: List<NERoomMember>) {
    }

    open fun onReceiveTextMessage(message: NERoomChatTextMessage) {
    }

    open fun onReceiveBatchGift(giftModel: NEVoiceRoomBatchGiftModel) {}

    open fun onLivePause() {}

    open fun onLiveResume() {}

    override fun onRoomNameChanged(roomName: String) {
    }

    override fun onRoomMaxMembersChanged(maxMembers: Int) {
    }

    override fun onRoomExtChanged(roomExt: String?) {
    }

    override fun onRoomPropertiesChanged(properties: Map<String, String>) {
    }

    override fun onRoomPropertiesDeleted(properties: Map<String, String>) {
    }

    override fun onMemberRoleChanged(member: NERoomMember, oldRole: NERoomRole, newRole: NERoomRole) {
    }

    override fun onMemberNameChanged(member: NERoomMember, name: String, operateBy: NERoomMember?) {
    }

    override fun onMemberExtChanged(member: NERoomMember, ext: String?) {
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

    override fun onRoomBlacklistStateChanged(isEnabled: Boolean) {
    }

    override fun onRoomAnnotationEnableChanged(isEnabled: Boolean, operateBy: NERoomMember?) {
    }

    override fun onMemberJoinRtcChannel(members: List<NERoomMember>) {
    }

    override fun onMemberLeaveRtcChannel(members: List<NERoomMember>) {
    }

    override fun onChatroomStateChange(state: NEChatroomState) {
    }

    override fun onRtcChannelError(code: Int) {}
    override fun onRtcChannelDisconnect(channel: String?, reason: Int) {
    }

    override fun onAudioEffectFinished(effectId: Int) {
    }

    override fun onAudioMixingStateChanged(reason: Int) {
    }

    override fun onRtcRemoteAudioVolumeIndication(volumes: List<NEMemberVolumeInfo>, totalVolume: Int) {
    }

    override fun onRtcLocalAudioVolumeIndication(volume: Int, vadFlag: Boolean) {
    }

    override fun onRtcAudioOutputDeviceChanged(device: NEAudioOutputDevice) {
    }

    override fun onRtcRecvSEIMsg(uuid: String, seiMsg: String) {
    }

    override fun onAudioEffectTimestampUpdate(effectId: Long, timeStampMS: Long) {
    }

    override fun onAudioHasHowling(flag: Boolean) {
    }

    override fun onMemberJoinChatroom(members: List<NERoomMember>) {
    }

    override fun onMemberLeaveChatroom(members: List<NERoomMember>) {
    }

    override fun onMemberAudioMuteChanged(member: NERoomMember, mute: Boolean, operateBy: NERoomMember?) {
    }

    override fun onMemberAudioConnectStateChanged(member: NERoomMember, connected: Boolean) {
    }

    override fun onMemberVideoMuteChanged(member: NERoomMember, mute: Boolean, operateBy: NERoomMember?) {
    }

    override fun onMemberScreenShareStateChanged(member: NERoomMember, isSharing: Boolean, operateBy: NERoomMember?) {
    }

    override fun onMemberSystemAudioShareStateChanged(
        member: NERoomMember,
        isSharing: Boolean,
        operateBy: NERoomMember?
    ) {
    }

    override fun onReceiveChatroomMessages(messages: List<NERoomChatMessage>) {
    }

    override fun onChatroomMessageAttachmentProgress(messageUuid: String, transferred: Long, total: Long) {
    }

    override fun onAntiSpamMessageIntercepted(messageUuid: String, antiSpamRes: NEYidunAntiSpamRes) {
    }

    override fun onMemberWhiteboardStateChanged(member: NERoomMember, isSharing: Boolean, operateBy: NERoomMember?) {
    }

    override fun onWhiteboardError(code: Int, message: String) {
    }

    override fun onRoomLiveStateChanged(state: NERoomLiveState) {
    }

    override fun onWhiteboardShowFileChooser(types: Array<String>, callback: NEValueCallback<Array<Uri>?>) {
    }

    override fun onRoomRemainingSecondsRenewed(remainingSeconds: Long) {
    }

    override fun onRoomConnectStateChanged(state: NERoomConnectType) {
    }

    override fun onRoomChatBanStateChanged(banned: Boolean, notifyExt: String?, operateBy: NERoomMember?) {
    }

    override fun onRoomAudioBanStateChanged(banned: Boolean, notifyExt: String?, operateBy: NERoomMember?) {
    }

    override fun onRoomVideoBanStateChanged(banned: Boolean, notifyExt: String?, operateBy: NERoomMember?) {
    }

    override fun onMemberChatBanStateChanged(
        member: NERoomMember,
        banned: Boolean,
        duration: Long,
        notifyExt: String?,
        operateBy: NERoomMember?
    ) {
    }

    override fun onMemberAudioBanStateChanged(
        member: NERoomMember,
        banned: Boolean,
        duration: Long,
        notifyExt: String?,
        operateBy: NERoomMember?
    ) {
    }

    override fun onMemberVideoBanStateChanged(
        member: NERoomMember,
        banned: Boolean,
        duration: Long,
        notifyExt: String?,
        operateBy: NERoomMember?
    ) {
    }

    override fun onMemberAddToBlacklist(userUuid: String, notifyExt: String?, operateBy: NERoomMember?) {
    }

    override fun onMemberRemoveFromBlacklist(userUuid: String, notifyExt: String?, operateBy: NERoomMember?) {
    }

    override fun onRoomCloudRecordStateChanged(state: NERoomCloudRecordState, operateBy: NERoomMember?) {
    }

    override fun onMemberSIPInviteStateChanged(member: NERoomMember, operateBy: NERoomMember?) {
    }

    override fun onMemberAppInviteStateChanged(member: NERoomMember, operateBy: NERoomMember?) {
    }

    override fun onReceiveCaptionMessages(channel: String?, captionMessages: List<NERoomCaptionMessage>) {
    }

    override fun onCaptionStateChanged(state: Int, code: Int, message: String?) {
    }

    override fun onRtcVirtualBackgroundSourceEnabled(enabled: Boolean, reason: Int) {
    }

    override fun onRtcLastmileQuality(quality: Int) {
    }

    override fun onRtcLastmileProbeResult(result: NERoomRtcLastmileProbeResult) {
    }
}
