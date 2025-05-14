// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view;

import android.app.Activity;
import android.content.Context;
import android.text.*;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.gift.GiftDialog2;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.NEAudienceInfo;
import com.netease.yunxin.kit.livestreamkit.api.model.NEAudienceInfoList;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.ChatRoomMoreDialog;
import com.netease.yunxin.kit.livestreamkit.ui.helper.AudioPlayHelper;
import com.netease.yunxin.kit.livestreamkit.ui.utils.*;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.model.*;
import java.util.List;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

public abstract class BaseLivingView extends BaseView {
  private final String TAG = "BasicLivingView";
  private static final int UPDATE_AUDIENCE_INTERVAL = 5000; // 5秒更新一次
  protected static final int MORE_ITEM_SWITCH_CAMERA = 0;
  protected static final int MORE_ITEM_MICRO_PHONE = 1;

  protected static final int MORE_ITEM_SMALL_WINDOW = 2;

  protected List<ChatRoomMoreDialog.MoreItem> moreItems;
  protected ChatRoomMoreDialog moreItemsDialog;
  private GiftDialog2 giftDialog;
  protected AudioPlayHelper audioPlay;

  private boolean isDestroyed = false;

  protected final Runnable updateAudienceRunnable =
      new Runnable() {
        @Override
        public void run() {
          if (!isDestroyed) {
            updateAudienceList();
            handler.postDelayed(this, UPDATE_AUDIENCE_INTERVAL);
          }
        }
      };

  public BaseLivingView(@NonNull Context context) {
    super(context);
  }

  public BaseLivingView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public BaseLivingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private final NELiveStreamListener roomListener =
      new NELiveStreamListener() {

        @Override
        public void onSeatRequestSubmitted(int seatIndex, @NonNull String user) {
          onLivingSeatRequestSubmitted();
        }

        @Override
        public void onSeatRequestApproved(
            int seatIndex, @NonNull String user, @NonNull String operateBy, boolean isAutoAgree) {
          if (TextUtils.equals(user, LiveStreamUtils.getLocalAccount())) {
            onLocalSeatRequestApproved();
          } else {
            onRemoteSeatRequestApproved(seatIndex, user, operateBy);
          }
        }

        @Override
        public void onSeatRequestCancelled(int seatIndex, @NonNull String user) {
          onLivingSeatRequestCancelled();
        }

        @Override
        public void onSeatRequestRejected(
            int seatIndex, @NonNull String user, @NonNull String operateBy) {
          if (TextUtils.equals(user, LiveStreamUtils.getLocalAccount())) {
            onLocalSeatRequestRejected();
          } else {
            onRemoteSeatRequestRejected();
          }
        }

        @Override
        public void onSeatInvitationReceived(
            int seatIndex, @NonNull String user, @NonNull String operateBy) {
          onLivingSeatInvitationReceived(seatIndex, user, operateBy);
        }

        @Override
        public void onSeatInvitationAccepted(
            int seatIndex, @NonNull String user, boolean isAutoAgree) {
          onLivingSeatInvitationAccepted(seatIndex, user, isAutoAgree);
        }

        @Override
        public void onSeatInvitationRejected(int seatIndex, @NonNull String user) {
          onLivingSeatInvitationRejected(seatIndex, user);
        }

        @Override
        public void onSeatKicked(int seatIndex, @NotNull String user, @NotNull String operateBy) {
          if (TextUtils.equals(user, LiveStreamUtils.getLocalAccount())) {
            onLocalSeatKicked();
          } else {
            onRemoteSeatKicked(seatIndex, user, operateBy);
          }
        }

        @Override
        public void onLivePause() {
          onLivingPause();
        }

        @Override
        public void onLiveResume() {
          onLivingResume();
        }

        @Override
        public void onMemberAudioMuteChanged(
            @NotNull NERoomMember member, boolean mute, @Nullable NERoomMember operateBy) {
          onLivingMemberAudioMuteChanged(member, mute, operateBy);
        }

        @Override
        public void onRtcAudioOutputDeviceChanged(@NonNull NEAudioOutputDevice device) {
          onLivingAudioOutputDeviceChanged(device);
        }
      };

  protected void onRemoteSeatKicked(
      int seatIndex, @NotNull String user, @NotNull String operateBy) {}

  protected void onLocalSeatKicked() {}

  protected abstract void onLivingResume();

  protected abstract void onLivingPause();

  protected ChatRoomMoreDialog.OnItemClickListener onMoreItemClickListener =
      (dialog, itemView, item) -> {
        switch (item.id) {
          case MORE_ITEM_SWITCH_CAMERA:
            if (NELiveStreamKit.getInstance().isLocalOnSeat()) {
              LiveStreamUtils.skipFrame = 5;
              NELiveStreamKit.getInstance().switchCamera();
            } else {
              ToastX.showShortToast(R.string.current_state_can_not_switch_camera);
            }

            break;

          case MORE_ITEM_MICRO_PHONE:
            {
              if (NELiveStreamKit.getInstance().getOnSeatList() != null
                  && NELiveStreamKit.getInstance().getOnSeatList().size() > 1) {
                ToastX.showShortToast(R.string.current_state_can_not_pause_living);
              } else {
                if (item.enable) {
                  NELiveStreamKit.getInstance().pauseLive(null);
                } else {
                  NELiveStreamKit.getInstance().resumeLive(null);
                }
              }
              break;
            }
        }
        return true;
      };

  @Override
  protected void initView() {
    createMoreItems();
    audioPlay = new AudioPlayHelper(getContext());
    audioPlay.checkMusicFiles();
  }

  protected abstract void createMoreItems();

  protected int enableEarBack(boolean enable) {
    if (enable) {
      return NELiveStreamKit.getInstance().enableEarback(100);
    } else {
      return NELiveStreamKit.getInstance().disableEarback();
    }
  }

  @Override
  protected void addObserver() {
    NELiveStreamKit.getInstance().addLiveStreamListener(roomListener);
  }

  @Override
  protected void removeObserver() {
    NELiveStreamKit.getInstance().removeLiveStreamListener(roomListener);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    stopUpdateAudienceList();
    handler.removeCallbacksAndMessages(null);
    isDestroyed = true;
    if (audioPlay != null) {
      audioPlay.destroy();
    }
  }

  protected void onLivingMemberAudioMuteChanged(
      NERoomMember member, boolean mute, NERoomMember operateBy) {}

  protected void onLivingSeatRequestSubmitted() {}

  protected void onLocalSeatRequestApproved() {}

  protected void onRemoteSeatRequestApproved(int seatIndex, String user, String operateBy) {}

  protected void onLivingSeatRequestCancelled() {}

  protected void onLocalSeatRequestRejected() {}

  protected void onRemoteSeatRequestRejected() {}

  protected void onLivingSeatInvitationReceived(
      int seatIndex, @NonNull String user, @NonNull String operateBy) {}

  protected void onLivingSeatInvitationAccepted(
      int seatIndex, @NonNull String user, boolean isAutoAgree) {}

  protected void onLivingSeatInvitationRejected(int seatIndex, @NonNull String user) {}

  protected abstract List<ChatRoomMoreDialog.MoreItem> getMoreItems();

  protected void onLivingAudioOutputDeviceChanged(NEAudioOutputDevice device) {}

  /** 显示调音台 */
  public abstract void showChatRoomMixerDialog();

  protected void showSendGiftDialog() {
    GiftDialog2 giftDialog = new GiftDialog2((Activity) getContext());
    giftDialog.show(
        NELiveStreamKit.getInstance().getCurrentRoomInfo().getLiveModel().getRoomUuid(),
        (giftId, giftCount, userUuids) ->
            NELiveStreamKit.getInstance()
                .sendBatchGift(
                    giftId,
                    giftCount,
                    userUuids,
                    new NELiveStreamCallback<Unit>() {
                      @Override
                      public void onSuccess(@Nullable Unit unit) {}

                      @Override
                      public void onFailure(int code, @Nullable String msg) {
                        ToastX.showShortToast(getContext().getString(R.string.live_reward_failed));
                      }
                    }));
  }

  private void updateAudienceList() {
    NELiveStreamKit.getInstance()
        .getAudienceList(
            1,
            3,
            new NELiveStreamCallback<NEAudienceInfoList>() {
              @Override
              public void onSuccess(@Nullable NEAudienceInfoList audienceList) {
                if (audienceList != null && !isDestroyed) {
                  List<NEAudienceInfo> members = audienceList.getList();
                  updateAudienceAvatars(audienceList);
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "getAudienceList failed, code: " + code + ", msg: " + msg);
              }
            });
  }

  protected void startUpdateAudienceList() {
    handler.post(updateAudienceRunnable);
  }

  protected void stopUpdateAudienceList() {
    handler.removeCallbacks(updateAudienceRunnable);
  }

  protected abstract void updateAudienceAvatars(NEAudienceInfoList audienceList);
}
