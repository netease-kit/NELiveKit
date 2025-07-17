// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.repo;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveStreamRoomInfo;
import com.netease.yunxin.kit.livestreamkit.ui.LiveStreamUIConstants;
import com.netease.yunxin.kit.roomkit.api.NEErrorCode;

public class LiveRepo {
  private final String TAG = "LiveRepo";

  public void createLive(
      String title,
      String username,
      String avatar,
      int configId,
      NELiveStreamCallback<NELiveStreamRoomInfo> callback) {

    NECreateLiveRoomParams createRoomParams =
        new NECreateLiveRoomParams(
            title,
            LiveStreamUIConstants.COUNT_SEAT,
            NELiveRoomSeatApplyMode.managerApproval,
            NELiveRoomSeatInviteMode.needAgree,
            configId,
            avatar,
            NELiveType.LIVE_INTERACTION);
    NELiveStreamKit.getInstance()
        .createRoom(
            createRoomParams,
            new NECreateLiveRoomOptions(),
            new NELiveStreamCallback<NELiveStreamRoomInfo>() {
              @Override
              public void onSuccess(@Nullable NELiveStreamRoomInfo roomInfo) {
                ALog.i(TAG, "createRoom success");
                joinLive(
                    username,
                    avatar,
                    NELiveRoomRole.HOST.getValue(),
                    roomInfo,
                    new NELiveStreamCallback<NELiveStreamRoomInfo>() {
                      @Override
                      public void onSuccess(@Nullable NELiveStreamRoomInfo roomInfo) {
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
      NELiveStreamRoomInfo roomInfo,
      NELiveStreamCallback<NELiveStreamRoomInfo> callback) {
    ALog.i(TAG, "joinLive roomInfo = " + roomInfo);
    if (roomInfo == null) {
      callback.onFailure(NEErrorCode.FAILURE, "roomInfo == null");
      return;
    }

    NEJoinLiveStreamRoomParams params = new NEJoinLiveStreamRoomParams(nick, role, roomInfo, null);

    NELiveStreamKit.getInstance()
        .joinRoom(
            params,
            new NELiveStreamCallback<NELiveStreamRoomInfo>() {

              @Override
              public void onSuccess(@Nullable NELiveStreamRoomInfo roomInfo) {
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
