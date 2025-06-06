// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view.audience;

import android.content.*;
import android.text.*;
import android.util.*;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.entertainment.common.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.livestreamkit.ui.viewmodel.*;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.*;
import kotlin.*;

public class BaseAudienceContentView extends BaseView {
  private static final String TAG = "BaseAudienceContentView";
  protected CDNStreamTextureView cdnStreamTextureView;
  protected AudienceLiveViewModel audienceViewModel;
  protected NELiveRoomInfo roomInfo;

  public BaseAudienceContentView(@NonNull Context context) {
    super(context);
  }

  public BaseAudienceContentView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public BaseAudienceContentView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private final NELiveStreamListener streamListener =
      new NELiveStreamListener() {

        @Override
        public void onSeatRequestApproved(
            int seatIndex,
            @NonNull String account,
            @NonNull String operateBy,
            boolean isAutoAgree) {
          LiveRoomLog.i(TAG, "onSeatRequestApproved seatIndex = " + seatIndex);
          if (TextUtils.equals(account, LiveStreamUtils.getLocalAccount())) {
            onLocalSeatRequestApproved();
          }
        }

        @Override
        public void onSeatInvitationAccepted(
            int seatIndex, @NonNull String account, boolean isAutoAgree) {
          if (TextUtils.equals(account, LiveStreamUtils.getLocalAccount())) {
            onLocalSeatInvitationAccepted();
          }
        }

        @Override
        public void onSeatListChanged(@NonNull List<NESeatItem> seatItems) {
          LiveRoomLog.i(TAG, "onSeatListChanged seatItems = " + seatItems);
          onLivingSeatListChanged(seatItems);
        }

        @Override
        public void onSeatLeave(int seatIndex, @NonNull String account) {
          LiveRoomLog.i(TAG, "onSeatLeave seatIndex = " + seatIndex);
          if (TextUtils.equals(account, LiveStreamUtils.getLocalAccount())) {
            onLocalUserLeaveSeat();
          } else {
            onRemoteUserLeaveSeat(account);
          }
        }

        @Override
        public void onSeatKicked(
            int seatIndex, @NonNull String account, @NonNull String operateBy) {
          LiveRoomLog.i(TAG, "onSeatKicked seatIndex = " + seatIndex);
          if (TextUtils.equals(account, LiveStreamUtils.getLocalAccount())) {
            onLocalUserLeaveSeat();
          } else {
            onRemoteUserLeaveSeat(account);
          }
        }

        @Override
        public void onMemberJoinRoom(@NonNull List<? extends NERoomMember> members) {
          LiveRoomLog.i(TAG, "onMemberJoinRoom members = " + members);
          for (NERoomMember member : members) {}
        }

        @Override
        public void onMemberJoinRtcChannel(@NonNull List<? extends NERoomMember> members) {
          for (NERoomMember member : members) {
            if (TextUtils.equals(member.getUuid(), LiveStreamUtils.getLocalAccount())) {
              onLocalUserJoinSeat();
            } else {
              onRemoteUserJoinSeat(member);
            }
          }
        }

        @Override
        public void onMemberLeaveRtcChannel(@NonNull List<? extends NERoomMember> members) {
          for (NERoomMember member : members) {
            if (!TextUtils.equals(member.getUuid(), LiveStreamUtils.getLocalAccount())) {
              onRemoteUserLeaveSeat(member.getUuid());
            }
          }
        }

        @Override
        public void onMemberRoleChanged(
            @NonNull NERoomMember member,
            @NonNull NERoomRole oldRole,
            @NonNull NERoomRole newRole) {
          LiveRoomLog.i(TAG, "onMemberRoleChanged account = " + member.getUuid());
          if (TextUtils.equals(member.getUuid(), LiveStreamUtils.getLocalAccount())
              && newRole.getName().equals(LiveConstants.ROLE_AUDIENCE_ON_SEAT)) {
            NELiveStreamKit.getInstance()
                .joinRtcChannel(
                    new NECallback2<Unit>() {
                      @Override
                      public void onSuccess(@Nullable Unit data) {
                        LiveRoomLog.i(TAG, "joinRtcChannel success");
                        NELiveStreamKit.getInstance().unmuteMyAudio(null);
                        NELiveStreamKit.getInstance().unmuteMyVideo(null);
                      }

                      @Override
                      public void onError(int code, @Nullable String message) {
                        LiveRoomLog.i(TAG, "joinRtcChannel failed");
                        NELiveStreamKit.getInstance().leaveSeat(null);
                      }
                    });
          }
        }
      };

  protected void onLocalUserJoinSeat() {}

  protected void onRemoteUserJoinSeat(NERoomMember member) {}

  protected void onLocalUserLeaveSeat() {
    showCdnView();
  }

  protected void onRemoteUserLeaveSeat(String uuid) {}

  @Override
  protected void initView() {
    audienceViewModel =
        new ViewModelProvider((AppCompatActivity) mContext).get(AudienceLiveViewModel.class);
    cdnStreamTextureView = new CDNStreamTextureView(getContext());
    addView(
        cdnStreamTextureView,
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
  }

  public void setRoomInfo(NELiveRoomInfo roomInfo) {
    this.roomInfo = roomInfo;
    cdnStreamTextureView.prepare();
    if (roomInfo != null
        && roomInfo.getLiveModel().getLiveConfig() != null
        && roomInfo.getLiveModel().getLiveConfig().getPullRtmpUrl() != null)
      cdnStreamTextureView.startPlay(roomInfo.getLiveModel().getLiveConfig().getPullRtmpUrl());
    cdnStreamTextureView.setLinkingSeats(true);
  }

  @Override
  protected void addObserver() {
    NELiveStreamKit.getInstance().addLiveStreamListener(streamListener);
  }

  @Override
  protected void removeObserver() {
    NELiveStreamKit.getInstance().removeLiveStreamListener(streamListener);
  }

  protected void showCdnView() {
    cdnStreamTextureView.setVisibility(VISIBLE);
  }

  protected void onLocalSeatRequestApproved() {}

  protected void onLocalSeatInvitationAccepted() {}

  protected void onLivingSeatListChanged(List<NESeatItem> seatItems) {}
}
