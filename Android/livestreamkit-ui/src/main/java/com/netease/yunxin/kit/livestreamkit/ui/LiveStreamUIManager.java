// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui;

import android.app.Application;
import androidx.annotation.Nullable;
import com.faceunity.nama.FURenderer;
import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.neliveplayer.sdk.model.NESDKConfig;
import com.netease.yunxin.kit.entertainment.common.AppStatusManager;
import com.netease.yunxin.kit.entertainment.common.model.NemoAccount;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import kotlin.Unit;
import org.jetbrains.annotations.*;

public class LiveStreamUIManager {
  private static final String TAG = "LiveStreamUIManager";
  private static volatile LiveStreamUIManager mInstance;
  private Application application;
  private String appkey;
  private String serverUrl;
  private String baseUrl;
  private final Set<NEAuthListener> roomAuthListeners = new HashSet<>();

  private LiveStreamUIManager() {}

  public static LiveStreamUIManager getInstance() {
    if (null == mInstance) {
      synchronized (LiveStreamUIManager.class) {
        if (mInstance == null) {
          mInstance = new LiveStreamUIManager();
        }
      }
    }
    return mInstance;
  }

  public void initialize(
      Application application,
      String appKey,
      Map<String, String> extras,
      NELiveStreamCallback<Unit> callback) {
    this.application = application;
    this.appkey = appKey;
    this.serverUrl = extras.get("serverUrl");
    this.baseUrl = extras.get("baseUrl");
    AppStatusManager.getInstance().init(application);
    FURenderer.getInstance().init(application);
    NESDKConfig config = new NESDKConfig();
    config.dataUploadListener =
        new NELivePlayer.OnDataUploadListener() {
          @Override
          public boolean onDataUpload(String s, String s1) {
            LiveRoomLog.d(TAG, "stream url is $s, detail data is $s1");
            return true;
          }

          @Override
          public boolean onDocumentUpload(
              String s, Map<String, String> map, Map<String, String> map1) {
            LiveRoomLog.d(TAG, "stream url is $s, detail data is $s1");
            return true;
          }
        };
    NELivePlayer.init(application, config);

    NELiveStreamKit.getInstance()
        .initialize(
            application,
            new NELiveStreamKitConfig(appKey, extras),
            new NELiveStreamCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                LiveRoomLog.d(TAG, "NELiveStreamKit init success");
                if (callback != null) {
                  callback.onSuccess(unit);
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.d(TAG, "NELiveStreamKit failed code = " + code + " msg = " + msg);
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });

    NELiveStreamKit.getInstance()
        .addAuthListener(
            new NEAuthListener() {
              @Override
              public void onAuthEvent(@NotNull NEAuthEvent evt) {
                for (NEAuthListener listener : roomAuthListeners) {
                  listener.onAuthEvent(evt);
                }
              }
            });
  }

  public void login(NemoAccount nemoAccount, NELiveStreamCallback<Void> callback) {
    NELiveStreamKit.getInstance()
        .login(
            nemoAccount.userUuid,
            nemoAccount.userToken,
            new NELiveStreamCallback<Unit>() {

              @Override
              public void onSuccess(@Nullable Unit unit) {
                LiveRoomLog.d(TAG, "NEVoiceRoomKit login success");
                UserInfoManager.setUserInfo(
                    nemoAccount.userUuid,
                    nemoAccount.userToken,
                    nemoAccount.imToken,
                    nemoAccount.userName,
                    nemoAccount.icon,
                    nemoAccount.mobile);
                UserInfoManager.saveUserInfoToSp(nemoAccount);
                if (callback != null) {
                  callback.onSuccess(null);
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "login failed code = " + code + ", msg = " + msg);
                UserInfoManager.clearUserInfo();
                if (callback != null) {
                  callback.onFailure(code, "login failed code = " + code + ", msg = " + msg);
                }
              }
            });
  }

  public void addAuthListener(NEAuthListener listener) {
    roomAuthListeners.add(listener);
  }

  public void removeAuthListener(NEAuthListener listener) {
    roomAuthListeners.remove(listener);
  }
}
