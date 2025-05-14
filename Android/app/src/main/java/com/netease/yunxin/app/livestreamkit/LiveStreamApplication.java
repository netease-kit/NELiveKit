// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.livestreamkit;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.livestreamkit.config.AppConfig;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.AppStatusManager;
import com.netease.yunxin.kit.entertainment.common.utils.IconFontUtil;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.ui.LiveStreamUIManager;
import java.util.HashMap;
import java.util.Map;
import kotlin.*;
import org.jetbrains.annotations.*;

public class LiveStreamApplication extends Application {
  private static final String TAG = "LiveStreamApplication";
  private static volatile boolean isInitialized = false;
  private static volatile boolean isInitializing = false;
  private static volatile boolean hasError = false;
  private static volatile String errorMsg;
  private static volatile int errorCode;
  private static final Handler mainHandler = new Handler(Looper.getMainLooper());
  private static InitCallback pendingCallback = null;

  public interface InitCallback {
    void onSuccess();

    void onError(int code, String msg);
  }

  public static synchronized void waitForInitialization(
      @NonNull InitCallback callback, long timeoutMs) {
    if (isInitialized) {
      mainHandler.post(callback::onSuccess);
    } else if (hasError) {
      mainHandler.post(() -> callback.onError(errorCode, errorMsg));
    } else {
      pendingCallback = callback;
      if (timeoutMs > 0) {
        mainHandler.postDelayed(
            () -> {
              synchronized (LiveStreamApplication.class) {
                if (pendingCallback == callback && !isInitialized && !hasError) {
                  hasError = true;
                  errorCode = -1;
                  errorMsg = "Initialization timeout";
                  callback.onError(-1, "Initialization timeout");
                  pendingCallback = null;
                }
              }
            },
            timeoutMs);
      }
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    initApp();
  }

  private void initApp() {
    // 基础组件初始化
    ALog.init(this, ALog.LEVEL_ALL);
    AppConfig.init(this);
    AppStatusManager.getInstance().init(this);
    initAuth();
    IconFontUtil.getInstance().init(this);

    // LiveStreamUIManager 初始化
    Map<String, String> extras = new HashMap<>();
    extras.put("serverUrl", AppConfig.getNERoomServerUrl());
    extras.put("baseUrl", AppConfig.getBaseUrl());

    isInitializing = true;
    LiveStreamUIManager.getInstance()
        .initialize(
            this,
            AppConfig.getAppKey(),
            extras,
            new NELiveStreamCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                ALog.i(TAG, "LiveStreamUIManager initialized successfully");
                synchronized (LiveStreamApplication.class) {
                  isInitialized = true;
                  isInitializing = false;
                  if (pendingCallback != null) {
                    mainHandler.post(pendingCallback::onSuccess);
                    pendingCallback = null;
                  }
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(
                    TAG,
                    "LiveStreamUIManager initialization failed: code=" + code + ", msg=" + msg);
                synchronized (LiveStreamApplication.class) {
                  hasError = true;
                  errorCode = code;
                  errorMsg = msg;
                  isInitializing = false;
                  if (pendingCallback != null) {
                    mainHandler.post(() -> pendingCallback.onError(code, msg));
                    pendingCallback = null;
                  }
                }
              }
            });
  }

  private void initAuth() {
    ALog.i(TAG, "initAuth");
  }
}
