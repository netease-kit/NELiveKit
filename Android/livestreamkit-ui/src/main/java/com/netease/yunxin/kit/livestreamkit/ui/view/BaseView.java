// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseView extends FrameLayout {
  protected Context mContext;
  private boolean mIsAddObserver;
  protected final Handler handler = new Handler(Looper.getMainLooper());

  public BaseView(@NonNull Context context) {
    this(context, null);
  }

  public BaseView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BaseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
    initView();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (!mIsAddObserver) {
      addObserver();
      mIsAddObserver = true;
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (mIsAddObserver) {
      removeObserver();
      mIsAddObserver = false;
    }
  }

  protected abstract void initView();

  protected abstract void addObserver();

  protected abstract void removeObserver();
}
