// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.livestreamkit.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.livestreamkit.LiveStreamApplication;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.AppStatusConstant;
import com.netease.yunxin.kit.entertainment.common.AppStatusManager;
import com.netease.yunxin.kit.entertainment.common.Constants;
import com.netease.yunxin.kit.entertainment.common.R;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.entertainment.common.model.NemoAccount;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback;
import com.netease.yunxin.kit.livestreamkit.ui.LiveStreamUIManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {
  private static final String TAG = "SplashActivity";
  private static final long INIT_TIMEOUT_MS = 10000; // 10秒超时
  private Handler mainHandler;
  private boolean isDestroyed = false;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_NORMAL);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    mainHandler = new Handler(Looper.getMainLooper());

    if (!isTaskRoot()) {
      Intent mainIntent = getIntent();
      String action = mainIntent.getAction();
      if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
        finish();
        return;
      }
    }

    handleStartup();
  }

  private void handleStartup() {
    NemoAccount account = UserInfoManager.getUserInfoFromSp();
    if (account == null) {
      gotoLoginPage();
      return;
    }

    // 等待初始化完成
    LiveStreamApplication.waitForInitialization(
        new LiveStreamApplication.InitCallback() {
          @Override
          public void onSuccess() {
            if (!isDestroyed) {
              login(account);
            }
          }

          @Override
          public void onError(int code, String msg) {
            if (!isDestroyed) {
              ALog.e(TAG, "Initialization failed: code=" + code + ", msg=" + msg);
              ToastX.showShortToast(msg);
              gotoLoginPage();
            }
          }
        },
        INIT_TIMEOUT_MS);
  }

  private void login(NemoAccount account) {
    if (isDestroyed) return;

    LiveStreamUIManager.getInstance()
        .login(
            account,
            new NELiveStreamCallback<Void>() {
              @Override
              public void onSuccess(@Nullable Void unused) {
                if (!isDestroyed) {
                  gotoHomePage();
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                if (!isDestroyed) {
                  ToastX.showShortToast(msg == null ? "登录失败" : msg);
                  gotoLoginPage();
                }
              }
            });
  }

  private void gotoLoginPage() {
    if (!isDestroyed) {
      LiveStreamSampleLoginActivity.startLoginActivity(this);
      finish();
    }
  }

  private void gotoHomePage() {
    if (!isDestroyed) {
      Intent intent = new Intent();
      intent.setPackage(getPackageName());
      intent.setAction(Constants.PAGE_ACTION_HOME);
      startActivity(intent);
      finish();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ALog.d(TAG, "onNewIntent: intent -> " + intent.getData());
    setIntent(intent);
  }

  @Override
  protected void onDestroy() {
    isDestroyed = true;
    if (mainHandler != null) {
      mainHandler.removeCallbacksAndMessages(null);
    }
    super.onDestroy();
  }
}
