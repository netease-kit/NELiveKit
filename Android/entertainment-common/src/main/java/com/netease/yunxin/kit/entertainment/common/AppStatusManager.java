// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.entertainment.common;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.*;
import com.netease.yunxin.kit.alog.*;
import com.netease.yunxin.kit.common.utils.*;
import java.util.*;

public class AppStatusManager {
  private static final String TAG = "AppStatusManager";
  //默认被初始化状态，被系统回收(强杀)状态
  public int mAppStatus = AppStatusConstant.STATUS_FORCE_KILLED;
  private int activityReferences = 0;
  private boolean isActivityChangingConfigurations = false;
  private boolean isAppForeground = false;
  private final List<AppForegroundStateCallback> callbacks = new ArrayList<>();

  private int activeCount;

  private AppStatusManager() {}

  private static final class InstanceHolder {
    public static final AppStatusManager instance = new AppStatusManager();
  }

  public static AppStatusManager getInstance() {
    return InstanceHolder.instance;
  }

  public int getAppStatus() {
    return mAppStatus;
  }

  public void setAppStatus(int appStatus) {
    this.mAppStatus = appStatus;
  }

  public void setActiveCount(int activeCount) {
    this.activeCount = activeCount;
  }

  public int getActiveCount() {
    return activeCount;
  }

  public void init(Application application) {
    application.registerActivityLifecycleCallbacks(
        new Application.ActivityLifecycleCallbacks() {
          @Override
          public void onActivityCreated(
              @NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

          @Override
          public void onActivityStarted(@NonNull Activity activity) {
            if (++activityReferences == 1 && !isActivityChangingConfigurations) {
              // App enters foreground
              isAppForeground = true;
              onAppForeground(activity.getApplication());
            }
          }

          @Override
          public void onActivityResumed(@NonNull Activity activity) {}

          @Override
          public void onActivityPaused(@NonNull Activity activity) {}

          @Override
          public void onActivityStopped(@NonNull Activity activity) {
            isActivityChangingConfigurations = activity.isChangingConfigurations();
            if (--activityReferences == 0 && !isActivityChangingConfigurations) {
              // App enters background
              isAppForeground = false;
              onAppBackground(activity.getApplication());
            }
          }

          @Override
          public void onActivitySaveInstanceState(
              @NonNull Activity activity, @NonNull Bundle outState) {}

          @Override
          public void onActivityDestroyed(@NonNull Activity activity) {}
        });
  }

  private void onAppForeground(Application application) {
    ALog.i(TAG, "Application enters foreground");
    if (ProcessUtils.isMainProcess(application)) {
      for (AppForegroundStateCallback callback : callbacks) {
        callback.onAppForeground();
      }
    }
  }

  private void onAppBackground(Application application) {
    ALog.i(TAG, "Application enters background");
    if (ProcessUtils.isMainProcess(application)) {
      for (AppForegroundStateCallback callback : callbacks) {
        callback.onAppBackground();
      }
    }
  }

  public void addCallback(AppForegroundStateCallback callback) {
    if (callback != null && !callbacks.contains(callback)) {
      callbacks.add(callback);
    }
  }

  public void removeCallback(AppForegroundStateCallback callback) {
    callbacks.remove(callback);
  }

  public boolean isAppForeground() {
    return isAppForeground;
  }

  public interface AppForegroundStateCallback {
    void onAppForeground();

    void onAppBackground();
  }
}
