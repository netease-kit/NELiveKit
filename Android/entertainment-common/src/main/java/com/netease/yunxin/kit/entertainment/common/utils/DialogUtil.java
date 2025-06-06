// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.entertainment.common.utils;

import android.app.Activity;
import android.view.*;
import com.netease.yunxin.kit.common.ui.dialog.CommonConfirmDialog;
import com.netease.yunxin.kit.entertainment.common.dialog.*;

public class DialogUtil {
  public static void showAlertDialog(Activity activity, CharSequence content) {
    showAlertDialog(activity, content, null);
  }

  public static void showAlertDialog(Activity activity, CharSequence content, String confirmText) {
    showAlertDialog(activity, content, confirmText, 0);
  }

  public static void showAlertDialog(
      Activity activity, CharSequence content, String confirmText, int countDownTime) {
    showAlertDialog(activity, content, confirmText, countDownTime, null);
  }

  public static void showAlertDialog(
      Activity activity,
      CharSequence content,
      String confirmText,
      int countDownTime,
      ECAlertDialog.DialogCallback callback) {
    if (activity.isFinishing()) {
      return;
    }
    ECAlertDialog dialog = new ECAlertDialog(activity);
    dialog.setDialogContent(content);
    dialog.setConfirmText(confirmText);
    dialog.setDialogCallback(callback);
    dialog.setCountDownTime(countDownTime);
    dialog.show();
  }

  public static void showConfirmDialog(
      Activity activity, String title, String content, CommonConfirmDialog.Callback callback) {
    CommonConfirmDialog.Companion.show(activity, title, content, true, true, callback);
  }

  public static void showConfirmDialog(
      Activity activity,
      String title,
      String content,
      String cancel,
      String ok,
      CommonConfirmDialog.Callback callback) {
    if (activity == null || activity.isFinishing()) {
      return;
    }
    CommonConfirmDialog.Companion.show(activity, title, content, cancel, ok, true, true, callback);
  }

  public static void showECConfirmDialog(
      Activity activity,
      String title,
      String content,
      String cancel,
      String ok,
      ECCommonConfirmDialog.Callback callback) {
    if (activity == null || activity.isFinishing()) {
      return;
    }

    ECCommonConfirmDialog.show(activity, title, content, cancel, ok, true, true, callback);
  }
}
