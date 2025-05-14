// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.repo;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.LiveConstants;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomInfo;
import com.netease.yunxin.kit.livestreamkit.ui.LiveStreamUIConstants;
import com.netease.yunxin.kit.roomkit.api.NEErrorCode;

public class LiveRepo {
  private final String TAG = "LiveRepo";

  public void createLive(
      String title,
      String username,
      String avatar,
      int configId,
      NELiveStreamCallback<NELiveRoomInfo> callback) {

    NECreateLiveRoomParams createVoiceRoomParams =
        new NECreateLiveRoomParams(
            title,
            username,
            LiveStreamUIConstants.COUNT_SEAT,
            NELiveRoomSeatApplyMode.managerApproval,
            NELiveRoomSeatInviteMode.needAgree,
            configId,
            avatar,
            NELiveType.LIVE_INTERACTION,
            null);
    NELiveStreamKit.getInstance()
        .createRoom(
            createVoiceRoomParams,
            new NECreateLiveRoomOptions(),
            new NELiveStreamCallback<NELiveRoomInfo>() {
              @Override
              public void onSuccess(@Nullable NELiveRoomInfo roomInfo) {
                ALog.i(TAG, "createRoom success");
                joinLive(
                    username,
                    avatar,
                    LiveConstants.ROLE_HOST,
                    roomInfo,
                    new NELiveStreamCallback<NELiveRoomInfo>() {
                      @Override
                      public void onSuccess(@Nullable NELiveRoomInfo roomInfo) {
                        if (callback != null) {
                          callback.onSuccess(roomInfo);
                        }
                      }

                      @Override
                      public void onFailure(int code, @Nullable String msg) {
                        if (callback != null) {
                          callback.onFailure(code, msg);
                        }
                      }
                    });
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });
  }

  public void joinLive(
      String nick,
      String avatar,
      String role,
      NELiveRoomInfo roomInfo,
      NELiveStreamCallback<NELiveRoomInfo> callback) {
    ALog.i(TAG, "joinLive roomInfo = " + roomInfo);
    if (roomInfo == null) {
      callback.onFailure(NEErrorCode.FAILURE, "roomInfo == null");
      return;
    }

    NELiveStreamKit.getInstance()
        .joinRoom(
            nick,
            avatar,
            role,
            roomInfo,
            new NELiveStreamCallback<NELiveRoomInfo>() {

              @Override
              public void onSuccess(@Nullable NELiveRoomInfo roomInfo) {
                ALog.i(TAG, "joinRoom success");
                if (callback != null) {
                  callback.onSuccess(roomInfo);
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "joinRoom failed code = " + code + " msg = " + msg);
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });
  }
}
