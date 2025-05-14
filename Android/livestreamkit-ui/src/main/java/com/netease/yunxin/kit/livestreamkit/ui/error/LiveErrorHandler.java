// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.error;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.roomkit.api.*;

public class LiveErrorHandler {
  private static final String TAG = "LiveErrorHandler";

  public static void handleError(int code, @Nullable String msg, @NonNull Context context) {
    ALog.e(TAG, "Error occurred: code=" + code + ", msg=" + msg);

    // Network related errors
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(R.string.live_net_disconnected);
      return;
    }

    // Handle specific error codes
    switch (code) {
      case NEErrorCode.FAILURE:
        ToastX.showShortToast(R.string.live_error);
        break;
      default:
        if (msg != null && !msg.isEmpty()) {
          ToastX.showShortToast(msg);
        } else {
          ToastX.showShortToast(R.string.live_net_disconnected);
        }
        break;
    }
  }

  public static void handleNetworkError(@NonNull Context context) {
    ToastX.showShortToast(R.string.live_net_disconnected);
  }

  public static void handleRoomError(@NonNull Context context, @NonNull String errorMsg) {
    ToastX.showShortToast(errorMsg);
  }

  public static void handleJoinError(@NonNull Context context) {
    ToastX.showShortToast(R.string.live_join_live_error);
  }

  public static void handleCreateError(@NonNull Context context) {
    ToastX.showShortToast(R.string.live_start_live_error);
  }
}
