// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.entertainment.common.dialog;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import com.netease.yunxin.kit.alog.*;
import com.netease.yunxin.kit.entertainment.common.R;

public class ChoiceDialog extends Dialog {
  protected Activity activity;
  protected View rootView;

  protected String titleStr;
  protected String contentStr;
  protected String positiveStr;
  protected String negativeStr;
  protected View.OnClickListener positiveListener;
  protected View.OnClickListener negativeListener;

  public ChoiceDialog(@NonNull Activity activity) {
    super(activity, R.style.CommonDialog);
    this.activity = activity;
    rootView = LayoutInflater.from(getContext()).inflate(contentLayoutId(), null);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(rootView);
    //fix one plus not show when resume from background
    getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  protected @LayoutRes int contentLayoutId() {
    return R.layout.live_view_dialog_choice_layout;
  }

  /** 页面渲染 */
  protected void renderRootView(View rootView) {
    if (rootView == null) {
      return;
    }
    TextView tvTitle = rootView.findViewById(R.id.tv_dialog_title);
    tvTitle.setText(titleStr);

    TextView tvContent = rootView.findViewById(R.id.tv_dialog_content);
    tvContent.setText(contentStr);
    tvContent.setVisibility(
        (contentStr != null && !contentStr.isEmpty()) ? View.VISIBLE : View.GONE);

    TextView tvPositive = rootView.findViewById(R.id.tv_dialog_positive);
    tvPositive.setText(positiveStr);
    tvPositive.setOnClickListener(
        v -> {
          dismiss();
          if (positiveListener != null) {
            positiveListener.onClick(v);
          }
        });

    TextView tvNegative = rootView.findViewById(R.id.tv_dialog_negative);
    tvNegative.setText(negativeStr);
    tvNegative.setOnClickListener(
        v -> {
          dismiss();
          if (negativeListener != null) {
            negativeListener.onClick(v);
          }
        });
  }

  public ChoiceDialog setTitle(String title) {
    this.titleStr = title;
    return this;
  }

  public ChoiceDialog setContent(String content) {
    this.contentStr = content;
    return this;
  }

  public ChoiceDialog setPositiveButton(String positive, View.OnClickListener listener) {
    this.positiveStr = positive;
    this.positiveListener = listener;
    return this;
  }

  public ChoiceDialog setPositiveButton(int resId, View.OnClickListener listener) {
    this.positiveStr = getContext().getString(resId);
    this.positiveListener = listener;
    return this;
  }

  public ChoiceDialog setNegativeButton(String negative, View.OnClickListener listener) {
    this.negativeListener = listener;
    this.negativeStr = negative;
    return this;
  }

  public ChoiceDialog setNegativeButton(int resId, View.OnClickListener listener) {
    this.negativeStr = getContext().getString(resId);
    this.negativeListener = listener;
    return this;
  }

  @Override
  public void show() {
    if (isShowing()) {
      return;
    }
    renderRootView(rootView);
    try {
      super.show();
    } catch (WindowManager.BadTokenException e) {
      ALog.e("ChoiceDialog", "error message is :" + e.getMessage());
    }
  }
}
