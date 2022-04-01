package com.netease.yunxin.app.newlive.live;

import android.content.Context;
import com.netease.yunxin.kit.livekit.NELiveListener;
import com.netease.yunxin.kit.livekit.NELiveCallback;
import com.netease.yunxin.kit.livekit.NELiveKit;
import com.netease.yunxin.kit.livekit.NELiveKitOptions;
import com.netease.yunxin.kit.livekit.VideoFrame;
import com.netease.yunxin.kit.livekit.model.RewardMsg;
import com.netease.yunxin.kit.livekit.model.pk.PkActionMsg;
import com.netease.yunxin.kit.livekit.model.pk.PkEndInfo;
import com.netease.yunxin.kit.livekit.model.pk.PkPunishInfo;
import com.netease.yunxin.kit.livekit.model.pk.PkStartInfo;
import com.netease.yunxin.kit.roomkit.api.NERoomMember;
import com.netease.yunxin.kit.roomkit.api.NERoomTextMessage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import kotlin.Unit;

public class LiveKitManager {
    private NELiveKit liveKit = NELiveKit.getInstance();
    private List<MyLiveListener> myLiveListenerList = new CopyOnWriteArrayList<>();
    private static volatile LiveKitManager singleton;
    private LiveKitManager() {}
    public static LiveKitManager getInstance() {
        if (singleton == null) {
            synchronized (LiveKitManager.class) {
                if (singleton == null) {
                    singleton = new LiveKitManager();
                }
            }
        }
        return singleton;
    }

    public void init(Context context, NELiveKitOptions options, NELiveCallback<Unit> callback){
        liveKit.initialize(context, options, callback);
        liveKit.setLiveListener(new NELiveListener() {

            @Override
            public void onLiveStarted() {
            }
            @Override
            public void onLoginKickedOut() {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onLoginKickedOut();
                }
            }
            @Override
            public void onLiveEnd(int reason) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onLiveEnd(reason);
                }
            }
            @Override
            public void onTextMessageReceived(@NonNull NERoomTextMessage message) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onTextMessageReceived(message);
                }
            }
            @Override
            public void onRewardReceived(@NonNull RewardMsg rewardMsg) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onRewardReceived(rewardMsg);
                }
            }
            @Override
            public int onVideoFrameCallback(@NonNull VideoFrame videoFrame) {
                for(MyLiveListener listener : myLiveListenerList){
                    int result = listener.onVideoFrameCallback(videoFrame);
                    if(result != 0) {
                        return result;
                    }
                }
                return 0;
            }
            @Override
            public void onPKInvited(@NonNull PkActionMsg pkUser) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKInvited(pkUser);
                }
            }
            @Override
            public void onPKAccepted(@NonNull PkActionMsg pkUser) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKAccepted(pkUser);
                }
            }
            @Override
            public void onPKRejected(@NonNull PkActionMsg pkUser) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKRejected(pkUser);
                }
            }
            @Override
            public void onPKCanceled(@NonNull PkActionMsg pkUser) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKCanceled(pkUser);
                }
            }
            @Override
            public void onPKTimeoutCanceled(@NonNull PkActionMsg pkUser) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKTimeoutCanceled(pkUser);
                }
            }
            @Override
            public void onPKStart(@NonNull PkStartInfo startInfo) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKStart(startInfo);
                }
            }
            @Override
            public void onPKPunishStart(@NonNull PkPunishInfo punishInfo) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKPunishStart(punishInfo);
                }
            }
            @Override
            public void onPKEnd(@NonNull PkEndInfo endInfo) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onPKEnd(endInfo);
                }
            }

            @Override
            public void onMembersLeave(@NonNull List<? extends NERoomMember> members) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onMembersLeave(members);
                    listener.onMemberCountChange(liveKit.getMembers());
                }
            }
            @Override
            public void onMembersJoin(@NonNull List<? extends NERoomMember> members) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onMembersJoin(members);
                    listener.onMemberCountChange(liveKit.getMembers());
                }
            }

            @Override
            public void onError(int code) {
                for(MyLiveListener listener : myLiveListenerList){
                    listener.onError(code);
                }
            }
        });
    }

    public void login(String account, String token, NELiveCallback<Unit> callback){
        liveKit.login(account, token, callback);
    }

    public void logout(NELiveCallback<Unit> callback){
        liveKit.logout(callback);
    }

    public NELiveKit getLiveKit() {
        return liveKit;
    }

    public void addLiveListener(MyLiveListener liveListener){
        myLiveListenerList.add(liveListener);
    }

    public void removeLiveListener(MyLiveListener liveListener){
        myLiveListenerList.remove(liveListener);
    }

}
