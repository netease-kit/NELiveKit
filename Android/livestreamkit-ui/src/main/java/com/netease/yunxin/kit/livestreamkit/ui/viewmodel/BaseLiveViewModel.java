// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.livestreamkit.ui.viewmodel;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import com.netease.yunxin.kit.entertainment.common.gift.GiftHelper;
import com.netease.yunxin.kit.entertainment.common.livedata.SingleLiveEvent;
import com.netease.yunxin.kit.entertainment.common.utils.Utils;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamListener;
import com.netease.yunxin.kit.livestreamkit.api.NEVoiceRoomEndReason;
import com.netease.yunxin.kit.livestreamkit.api.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.kit.livestreamkit.ui.error.LiveErrorHandler;
import com.netease.yunxin.kit.livestreamkit.ui.model.SeatEvent;
import com.netease.yunxin.kit.livestreamkit.ui.model.VoiceRoomSeat;
import com.netease.yunxin.kit.livestreamkit.ui.repo.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.LiveStreamUtils;
import com.netease.yunxin.kit.livestreamkit.ui.utils.SeatUtils;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.model.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.*;

/** 房间、麦位业务逻辑 */
public class BaseLiveViewModel extends ViewModel {
  private static final String TAG = "BaseLiveViewModel";
  public static final int CURRENT_SEAT_STATE_IDLE = 0;
  public static final int CURRENT_SEAT_STATE_APPLYING = 1;
  public static final int CURRENT_SEAT_STATE_ON_SEAT = 2;
  public static final int NET_AVAILABLE = 0; // 网络 可用
  public static final int NET_LOST = 1; // 网络不可用
  private static final int AUDIENCE_SEAT_INDEX = 2;

  private List<VoiceRoomSeat> roomSeats = new ArrayList<>();
  MutableLiveData<CharSequence> chatRoomMsgData = new MutableLiveData<>(); // 聊天列表数据
  MutableLiveData<Integer> memberCountData = new MutableLiveData<>(); // 房间人数
  MutableLiveData<NEVoiceRoomEndReason> errorData = new MutableLiveData<>(); // 错误信息
  public MutableLiveData<Boolean> anchorAvatarAnimation = new MutableLiveData<>();
  public MutableLiveData<Boolean> audienceAvatarAnimation = new MutableLiveData<>();
  MutableLiveData<Integer> currentSeatState = new MutableLiveData<>(CURRENT_SEAT_STATE_IDLE);
  MutableLiveData<List<VoiceRoomSeat>> onSeatListData =
      new MutableLiveData<>(LiveStreamUtils.createSeats());
  MutableLiveData<SeatEvent> currentSeatEvent = new SingleLiveEvent<>(); // 当前操作的麦位
  MutableLiveData<Integer> netData = new MutableLiveData<>();
  public MutableLiveData<NEVoiceRoomBatchGiftModel> bachRewardData = new MutableLiveData<>();
  protected LiveRepo liveRepo = new LiveRepo();
  private final NELiveStreamListener listener =
      new NELiveStreamListener() {

        @Override
        public void onReceiveBatchGift(@NonNull NEVoiceRoomBatchGiftModel rewardMsg) {
          super.onReceiveBatchGift(rewardMsg);
          bachRewardData.postValue(rewardMsg);
        }

        @Override
        public void onReceiveTextMessage(@NonNull NERoomChatTextMessage message) {
          String content = message.getText();
          LiveRoomLog.i(TAG, "onReceiveTextMessage :${message.fromNick}");
          chatRoomMsgData.postValue(
              ChatRoomMsgCreator.createText(
                  Utils.getApp(),
                  LiveStreamUtils.isHost(message.getFromUserUuid()),
                  message.getFromNick(),
                  content));
        }

        @Override
        public void onMemberAudioMuteChanged(
            @NonNull NERoomMember member, boolean mute, @Nullable NERoomMember operateBy) {}

        @Override
        public void onMemberJoinRoom(@NonNull List<? extends NERoomMember> members) {
          for (NERoomMember member : members) {
            LiveRoomLog.d(TAG, "onMemberJoinRoom :" + member.getName());
          }
        }

        @Override
        public void onMemberLeaveRoom(@NonNull List<? extends NERoomMember> members) {
          for (NERoomMember member : members) {
            LiveRoomLog.d(TAG, "onMemberLeaveRoom :" + member.getName());
          }
        }

        @Override
        public void onMemberJoinChatroom2(@NonNull List<? extends NERoomMember> members) {
          LiveRoomLog.d(TAG, "onMemberJoinChatroom :" + members.get(0).getName());

          for (NERoomMember member : members) {
            LiveRoomLog.d(TAG, "onMemberJoinRoom :${member.name}");
            if (!LiveStreamUtils.isMySelf(member.getUuid())) {
              chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomEnter(member.getName()));
            }
          }
        }

        @Override
        public void onMemberLeaveChatroom2(@NonNull List<? extends NERoomMember> members) {
          LiveRoomLog.d(TAG, "onMemberLeaveChatroom :" + members.get(0).getName());

          for (NERoomMember member : members) {
            LiveRoomLog.d(TAG, "onMemberJoinRoom :${member.name}");
            if (!LiveStreamUtils.isMySelf(member.getUuid())) {
              chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomExit(member.getName()));
            }
          }
        }

        @Override
        public void onSeatLeave(@NonNull NESeatItem seatItem) {
          LiveRoomLog.d(TAG, "onSeatLeave seatItem" + seatItem);
          if (TextUtils.equals(seatItem.getUser(), SeatUtils.getCurrentUuid())) {
            currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE);
            currentSeatEvent.postValue(
                new SeatEvent(seatItem.getUser(), seatItem.getIndex(), VoiceRoomSeat.Reason.LEAVE));
          }
          buildSeatEventMessage(seatItem.getUserName(), getString(R.string.live_down_seat));
        }

        @Override
        public void onSeatListChanged(@NonNull List<NESeatItem> seatItems) {
          LiveRoomLog.i(TAG, "onSeatListChanged seatItems" + seatItems);
          handleSeatItemListChanged(seatItems);
        }

        @Override
        public void onLivePause() {
          LiveRoomLog.i(TAG, "onLivePause");
        }

        @Override
        public void onLiveResume() {
          LiveRoomLog.i(TAG, "onLiveResume");
        }

        @Override
        public void onRoomEnded(@NonNull NERoomEndReason reason) {
          onLiveRoomEnded(reason);
        }

        @Override
        public void onRtcChannelError(int code) {
          if (code == 30015) {
            errorData.postValue(NEVoiceRoomEndReason.valueOf("END_OF_RTC"));
          }
        }

        @Override
        public void onRtcLocalAudioVolumeIndication(int volume, boolean vadFlag) {
          if (LiveStreamUtils.isCurrentHost()) {
            anchorAvatarAnimation.postValue(volume > 0);
          } else {
            for (VoiceRoomSeat roomSeat : roomSeats) {
              if (LiveStreamUtils.isMySelf(roomSeat.getAccount()) && roomSeat.isOn()) {
                audienceAvatarAnimation.postValue(volume > 0);
              }
            }
          }
        }

        @Override
        public void onRtcRemoteAudioVolumeIndication(
            @NonNull List<NEMemberVolumeInfo> volumes, int totalVolume) {}
      };

  protected void onLiveRoomEnded(NERoomEndReason reason) {}

  public BaseLiveViewModel() {
    init();
  }

  public MutableLiveData<Integer> getNetData() {
    return netData;
  }

  public MutableLiveData<CharSequence> getChatRoomMsgData() {
    return chatRoomMsgData;
  }

  public MutableLiveData<Integer> getMemberCountData() {
    return memberCountData;
  }

  public MutableLiveData<NEVoiceRoomEndReason> getErrorData() {
    return errorData;
  }

  public MutableLiveData<Integer> getCurrentSeatState() {
    return currentSeatState;
  }

  public MutableLiveData<List<VoiceRoomSeat>> getOnSeatListData() {
    return onSeatListData;
  }

  public LiveData<SeatEvent> getCurrentSeatEvent() {
    return currentSeatEvent;
  }

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
          if (!isFirst) {
            LiveRoomLog.i(TAG, "onNetworkAvailable");
            getSeatInfo();
          }
          isFirst = false;
          netData.postValue(NET_AVAILABLE);
        }

        @Override
        public void onDisconnected() {
          LiveRoomLog.i(TAG, "onNetworkUnavailable");
          isFirst = false;
          netData.postValue(NET_LOST);
          LiveErrorHandler.handleNetworkError(XKitUtils.getApplicationContext());
        }

        private boolean isFirst = true;
      };

  public void init() {
    NELiveStreamKit.getInstance().addLiveStreamListener(listener);
    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
    NELiveStreamKit.getInstance().enableAudioVolumeIndication(true, 200);
    GiftHelper.getInstance().init();
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
    NELiveStreamKit.getInstance().removeLiveStreamListener(listener);
    NELiveStreamKit.getInstance().enableAudioVolumeIndication(false, 200);
    GiftHelper.getInstance().clear();
  }

  public void getSeatInfo() {
    NELiveStreamKit.getInstance()
        .getSeatInfo(
            new NELiveStreamCallback<NESeatInfo>() {

              @Override
              public void onSuccess(@Nullable NESeatInfo seatInfo) {
                if (seatInfo != null) {
                  handleSeatItemListChanged(seatInfo.getSeatItems());
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "getSeatInfo failed code = " + code + " msg = " + msg);
              }
            });
  }

  private String getString(@StringRes int resId) {
    return Utils.getApp().getString(resId);
  }

  public boolean isCurrentUserOnSeat() {
    return currentSeatState.getValue() == CURRENT_SEAT_STATE_ON_SEAT;
  }

  private void handleSeatItemListChanged(List<NESeatItem> seatItems) {
    if (seatItems == null) seatItems = Collections.emptyList();
    List<VoiceRoomSeat> seats = SeatUtils.transNESeatItem2VoiceRoomSeat(seatItems);
    String currentUuid = SeatUtils.getCurrentUuid();
    VoiceRoomSeat myAfterSeat = findSeatByAccount(seats, currentUuid);
    if (myAfterSeat != null && myAfterSeat.isOn()) {
      if (isCurrentUserOnSeat()) {
        NELiveStreamKit.getInstance()
            .unmuteMyAudio(
                new NELiveStreamCallback<Unit>() {
                  @Override
                  public void onSuccess(@Nullable Unit unit) {
                    LiveRoomLog.d(TAG, "unmuteMyAudio success");
                  }

                  @Override
                  public void onFailure(int code, @Nullable String msg) {
                    LiveRoomLog.e(TAG, "unmuteMyAudio failed code = " + code + " msg = " + msg);
                    LiveErrorHandler.handleError(code, msg, XKitUtils.getApplicationContext());
                  }
                });
      }
      currentSeatState.postValue(CURRENT_SEAT_STATE_ON_SEAT);
    } else if (myAfterSeat != null && myAfterSeat.getStatus() == VoiceRoomSeat.Status.APPLY) {
      currentSeatState.postValue(CURRENT_SEAT_STATE_APPLYING);
    } else {
      currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE);
    }
    roomSeats = seats;
    onSeatListData.postValue(seats);
  }

  private VoiceRoomSeat findSeatByAccount(List<VoiceRoomSeat> seats, String account) {
    if (seats == null || seats.isEmpty() || account == null) return null;
    for (VoiceRoomSeat seat : seats) {
      if (seat.getMember() != null && TextUtils.equals(seat.getMember().getUuid(), account)) {
        return seat;
      }
    }
    return null;
  }

  private void buildSeatEventMessage(String account, String content) {
    if (!shouldShowSeatEventMessage(account)) return;
    String nick = SeatUtils.getMemberNick(account);
    if (!TextUtils.isEmpty(nick)) {
      chatRoomMsgData.postValue(ChatRoomMsgCreator.createSeatMessage(nick, content));
    }
  }

  private boolean shouldShowSeatEventMessage(String account) {
    return LiveStreamUtils.isMySelf(account) || LiveStreamUtils.isCurrentHost();
  }

  public void joinLive(
      String username,
      String avatar,
      String role,
      NELiveRoomInfo roomInfo,
      NELiveStreamCallback<NELiveRoomInfo> callback) {
    liveRepo.joinLive(
        username,
        avatar,
        role,
        roomInfo,
        new NELiveStreamCallback<NELiveRoomInfo>() {
          @Override
          public void onSuccess(@Nullable NELiveRoomInfo roomInfo) {
            LiveRoomLog.d(TAG, "joinLive success");
            callback.onSuccess(roomInfo);
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {
            LiveRoomLog.e(TAG, "joinLive failed code = " + code + " msg = " + msg);
            LiveErrorHandler.handleError(code, msg, XKitUtils.getApplicationContext());
            callback.onFailure(code, msg);
          }
        });
  }
}
