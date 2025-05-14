// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.entertainment.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.*;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.entertainment.common.R;

public class ECAlertDialog extends Dialog {
  private final Handler handler = new Handler(Looper.getMainLooper());
  private TextView positiveBtn;
  private TextView titleTv;
  private TextView contentTv;
  private String confirmText;
  private CharSequence title;
  private CharSequence content;
  private float titleSize;
  private float contentSize;
  private int countDownTime;

  public ECAlertDialog(@NonNull Context context) {
    super(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.gravity = Gravity.CENTER;
      wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
      wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      window.setAttributes(wlp);
      window.setBackgroundDrawableResource(android.R.color.transparent);
    }
    setContentView(R.layout.dialog_ec_alert);
    initView();
  }

  private void initView() {
    positiveBtn = findViewById(R.id.tv_dialog_positive);
    positiveBtn.setOnClickListener(
        v -> {
          if (dialogCallback != null) {
            dialogCallback.onConfirm(this);
          }
          dismiss();
        });

    positiveBtn.setText(getConfirmText());

    titleTv = findViewById(R.id.tv_dialog_title);
    if (TextUtils.isEmpty(getTitle())) {
      titleTv.setVisibility(View.GONE);
    } else {
      titleTv.setText(getTitle());
      titleTv.setTextSize(titleSize);
      titleTv.setVisibility(View.VISIBLE);
    }

    contentTv = findViewById(R.id.tv_dialog_content);
    if (TextUtils.isEmpty(getContent())) {
      contentTv.setVisibility(View.GONE);
    } else {
      contentTv.setText(getContent());
      contentTv.setTextSize(contentSize);
      contentTv.setVisibility(View.VISIBLE);
    }

    if (countDownTime > 0) {
      startCountdown();
    }
  }

  public void setDialogCallback(DialogCallback dialogCallback) {
    this.dialogCallback = dialogCallback;
  }

  public void setConfirmText(String text) {
    confirmText = text;
  }

  public void setDialogTitle(CharSequence text) {
    title = text;
  }

  public void setDialogContent(CharSequence text) {
    content = text;
  }

  public void setCountDownTime(int countDownTime) {
    this.countDownTime = countDownTime;
  }

  public void setTitleTextSize(int titleSize) {
    this.titleSize = titleSize;
  }

  public void setContentTextSize(int contentSize) {
    this.contentSize = contentSize;
  }

  private String getConfirmText() {
    if (!TextUtils.isEmpty(confirmText)) {
      return confirmText;
    } else {
      return getContext().getString(R.string.app_sure);
    }
  }

  private CharSequence getTitle() {
    if (!TextUtils.isEmpty(title)) {
      return title;
    } else {
      return "";
    }
  }

  private CharSequence getContent() {
    if (!TextUtils.isEmpty(content)) {
      return content;
    } else {
      return "";
    }
  }

  private DialogCallback dialogCallback;

  public interface DialogCallback {
    void onConfirm(Dialog dialog);
  }

  private void startCountdown() {
    handler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            if (countDownTime > 0) {
              positiveBtn.setText(
                  String.format(
                      XKitUtils.getApplicationContext()
                          .getString(R.string.voiceroom_i_know_with_countdown),
                      countDownTime));
              countDownTime--;
              positiveBtn.setEnabled(false);
              handler.postDelayed(this, 1000);
            } else {
              positiveBtn.setText(
                  String.format(
                      XKitUtils.getApplicationContext().getString(R.string.voiceroom_i_know),
                      countDownTime));
              positiveBtn.setEnabled(true);
            }
          }
        },
        0);
  }
}
