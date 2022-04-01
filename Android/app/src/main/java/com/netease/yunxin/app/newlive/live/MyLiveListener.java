package com.netease.yunxin.app.newlive.live;

import com.netease.yunxin.kit.livekit.NELiveListener;
import com.netease.yunxin.kit.livekit.VideoFrame;
import com.netease.yunxin.kit.livekit.model.RewardMsg;
import com.netease.yunxin.kit.livekit.model.pk.PkActionMsg;
import com.netease.yunxin.kit.livekit.model.pk.PkEndInfo;
import com.netease.yunxin.kit.livekit.model.pk.PkPunishInfo;
import com.netease.yunxin.kit.livekit.model.pk.PkStartInfo;
import com.netease.yunxin.kit.roomkit.api.NERoomMember;
import com.netease.yunxin.kit.roomkit.api.NERoomTextMessage;

import java.util.List;

import androidx.annotation.NonNull;

public class MyLiveListener implements NELiveListener {

    @Override
    public void onTextMessageReceived(@NonNull NERoomTextMessage message) {
    }
    @Override
    public void onRewardReceived(@NonNull RewardMsg rewardMsg) {
    }
    @Override
    public int onVideoFrameCallback(@NonNull VideoFrame videoFrame) {
        return 0;
    }
    @Override
    public void onPKInvited(@NonNull PkActionMsg pkUser) {
    }
    @Override
    public void onPKAccepted(@NonNull PkActionMsg pkUser) {
    }
    @Override
    public void onPKRejected(@NonNull PkActionMsg pkUser) {
    }
    @Override
    public void onPKCanceled(@NonNull PkActionMsg pkUser) {
    }
    @Override
    public void onPKTimeoutCanceled(@NonNull PkActionMsg pkUser) {
    }
    @Override
    public void onPKStart(@NonNull PkStartInfo startInfo) {
    }
    @Override
    public void onPKPunishStart(@NonNull PkPunishInfo punishInfo) {
    }
    @Override
    public void onPKEnd(@NonNull PkEndInfo endInfo) {
    }

    @Override
    public void onLiveEnd(int reason) {
    }
    @Override
    public void onLoginKickedOut() {
    }
    @Override
    public void onMembersJoin(@NonNull List<? extends NERoomMember> members) {
    }
    @Override
    public void onMembersLeave(@NonNull List<? extends NERoomMember> members) {
    }

    public void onMemberCountChange(List<NERoomMember> members){

    }

    @Override
    public void onLiveStarted() {
    }
    @Override
    public void onError(int code) {
    }
}
