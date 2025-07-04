// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.viewmodel;

import android.text.TextUtils;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomInfo;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.model.*;
import com.netease.yunxin.kit.roomkit.api.service.NEPreviewRoomOptions;
import com.netease.yunxin.kit.roomkit.api.service.NEPreviewRoomParams;
import com.netease.yunxin.kit.roomkit.api.service.NERoomService;
import com.netease.yunxin.kit.roomkit.api.view.NERoomVideoView;
import kotlin.Unit;
import org.jetbrains.annotations.Nullable;

public class AudienceLiveViewModel extends BaseLiveViewModel {
  private static final String TAG = "LiveAudienceViewModel";
  private final MutableLiveData<String> liveRoomName = new MutableLiveData<>("");
  private final MutableLiveData<String> memberCount = new MutableLiveData<>("");
  private final MutableLiveData<NELiveRoomInfo> roomInfoData = new MutableLiveData<>();
  private NEPreviewRoomContext previewRoomContext;
  public static final int LIVE_STATE_FINISH = 0;
  public static final int LIVE_STATE_LIVING = 1;
  private final MutableLiveData<Integer> liveStateData = new MutableLiveData<>(LIVE_STATE_LIVING);

  private final NEPreviewRoomListener previewRoomListener =
      new NEPreviewRoomListener() {
        @Override
        public void onRtcVirtualBackgroundSourceEnabled(boolean enabled, int reason) {
          // Empty implementation
        }

        @Override
        public void onRtcLastmileQuality(int quality) {
          LiveRoomLog.d(TAG, "onRtcLastmileQuality, quality:" + quality);
        }

        @Override
        public void onRtcLastmileProbeResult(NERoomRtcLastmileProbeResult result) {
          LiveRoomLog.d(TAG, "onRtcLastmileProbeResult, result:" + result);
        }
      };

  private final NERoomService roomService = NERoomKit.getInstance().getService(NERoomService.class);

  public void startPreview(NERoomVideoView videoView) {
    if (previewRoomContext != null) {
      previewRoomContext.getPreviewController().startPreview(videoView);
    } else {
      roomService.previewRoom(
          new NEPreviewRoomParams(),
          new NEPreviewRoomOptions(),
          new NECallback<NEPreviewRoomContext>() {
            @Override
            public void onResult(int code, String message, NEPreviewRoomContext data) {
              previewRoomContext = data;
              if (previewRoomContext != null) {
                previewRoomContext.getPreviewController().startPreview(videoView);
              }
            }
          });
    }
  }

  public void stopPreview() {
    if (previewRoomContext != null) {
      previewRoomContext.getPreviewController().stopPreview();
    }
  }

  public void joinLive(String username, String avatar, NELiveRoomInfo roomInfo) {
    joinLive(
        username,
        avatar,
        LiveRoomRole.ROLE_AUDIENCE,
        roomInfo,
        new NELiveStreamCallback<NELiveRoomInfo>() {

          @Override
          public void onSuccess(@Nullable NELiveRoomInfo neLiveRoomInfo) {
            liveStateData.setValue(HostLiveViewModel.LIVE_STATE_LIVING);
            roomInfoData.setValue(neLiveRoomInfo);
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {
            leaveLive();
            liveStateData.postValue(LIVE_STATE_FINISH);
            if (!TextUtils.isEmpty(msg)) {
              ToastX.showShortToast(msg);
            } else {
              ToastX.showShortToast(R.string.live_join_live_error);
            }
          }
        });
  }

  public void leaveLive() {
    NELiveStreamKit.getInstance()
        .leaveRoom(
            new NELiveStreamCallback<Unit>() {
              @Override
              public void onSuccess(@androidx.annotation.Nullable Unit unit) {
                LiveRoomLog.d(TAG, "leaveRoom onSuccess");
                liveStateData.postValue(LIVE_STATE_FINISH);
              }

              @Override
              public void onFailure(int code, @androidx.annotation.Nullable String msg) {
                LiveRoomLog.e(TAG, "leaveRoom failed code:" + code + ",msg:" + msg);
                liveStateData.postValue(LIVE_STATE_FINISH);
              }
            });
  }

  public void switchCamera() {
    if (previewRoomContext != null) {
      previewRoomContext.getPreviewController().switchCamera();
    }
  }

  public MutableLiveData<String> getLiveRoomName() {
    return liveRoomName;
  }

  public MutableLiveData<String> getMemberCount() {
    return memberCount;
  }

  public MutableLiveData<NELiveRoomInfo> getRoomInfoData() {
    return roomInfoData;
  }

  public MutableLiveData<Integer> getLiveStateData() {
    return liveStateData;
  }

  @Override
  protected void onLiveRoomEnded(NERoomEndReason reason) {
    liveStateData.postValue(LIVE_STATE_FINISH);
  }
}
