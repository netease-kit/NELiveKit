// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view.host;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.*;
import androidx.lifecycle.*;
import com.netease.yunxin.kit.common.ui.utils.*;
import com.netease.yunxin.kit.entertainment.common.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.*;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.LiveAnchorPreviewLayoutBinding;
import com.netease.yunxin.kit.livestreamkit.ui.view.BaseView;
import com.netease.yunxin.kit.livestreamkit.ui.viewmodel.*;

public class HostPreviewView extends BaseView {
  private static final String TAG = "AnchorPreviewView";
  private LiveAnchorPreviewLayoutBinding binding;
  private OnCloseClickListener onCloseClickListener;
  private OnSwitchCameraClickListener onSwitchCameraClickListener;
  private OnBeautyClickListener onBeautyClickListener;
  private HostLiveViewModel liveViewModel;
  private String username;
  private int configId;

  public HostPreviewView(@NonNull Context context) {
    this(context, null);
  }

  public HostPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HostPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void initView() {
    binding = LiveAnchorPreviewLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
    ViewUtils.paddingStatusBarHeight((Activity) getContext(), binding.getRoot());
    liveViewModel =
        new ViewModelProvider((AppCompatActivity) mContext).get(HostLiveViewModel.class);
    // 设置输入监听
    binding.edtLiveTitle.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            // 限制最多20个字符（中英文都算1个）
            if (s.length() > 20) {
              s.delete(20, s.length());
            }
          }
        });

    // 设置关闭按钮点击事件
    binding.ivClose.setOnClickListener(
        v -> {
          if (onCloseClickListener != null) {
            onCloseClickListener.onCloseClick();
          }
        });

    // 设置切换摄像头点击事件
    binding.llSwitchCamera.setOnClickListener(
        v -> {
          if (onSwitchCameraClickListener != null) {
            onSwitchCameraClickListener.onSwitchCamera();
          }
        });

    binding.llBeauty.setOnClickListener(
        v -> {
          if (onBeautyClickListener != null) {
            onBeautyClickListener.onBeautyClick();
          }
        });
    binding.btnStartLive.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (ClickUtils.isFastClick()) {
              return;
            }
            if (VoiceRoomUtils.isShowFloatView()) {
              AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
              builder.setTitle(getContext().getString(R.string.voiceroom_tip));
              builder.setMessage(getContext().getString(R.string.click_create_room_tips));
              builder.setCancelable(true);
              builder.setPositiveButton(
                  getContext().getString(R.string.voiceroom_sure),
                  (dialog, which) -> {
                    liveViewModel.endLive();
                    VoiceRoomUtils.stopFloatPlay();
                    dialog.dismiss();
                  });
              builder.setNegativeButton(
                  getContext().getString(R.string.voiceroom_cancel),
                  (dialog, which) -> dialog.dismiss());
              AlertDialog alertDialog = builder.create();
              alertDialog.show();
            } else if (OneOnOneUtils.isInTheCall()) {
              ToastX.showShortToast(getContext().getString(R.string.ec_in_the_call_tips));
            } else {
              // Show loading before creating live room
              binding.flLoading.setVisibility(View.VISIBLE);
              binding.btnStartLive.setEnabled(false);
              liveViewModel.createLive(getRoomName(), username, "", configId);
            }
          }
        });
  }

  public void initData(String username, int configId) {
    this.username = username;
    this.configId = configId;
  }

  @Override
  protected void addObserver() {
    liveViewModel
        .getLiveStateData()
        .observe(
            (AppCompatActivity) getContext(),
            liveState -> {
              if (liveState == HostLiveViewModel.LIVE_STATE_LIVING) {
                binding.flLoading.setVisibility(View.GONE);
              } else if (liveState == HostLiveViewModel.LIVE_STATE_PREVIEW) {
                binding.flLoading.setVisibility(View.GONE);
              } else if (liveState == HostLiveViewModel.LIVE_STATE_FINISH) {
                binding.flLoading.setVisibility(View.GONE);
              } else if (liveState == HostLiveViewModel.LIVE_STATE_ERROR) {
                binding.flLoading.setVisibility(View.GONE);
                binding.btnStartLive.setEnabled(true);
              }
            });
  }

  @Override
  protected void removeObserver() {}

  public String getRoomName() {
    return binding.edtLiveTitle.getText().toString().trim();
  }

  public void setOnCloseClickListener(OnCloseClickListener listener) {
    this.onCloseClickListener = listener;
  }

  public void setOnSwitchCameraClickListener(OnSwitchCameraClickListener listener) {
    this.onSwitchCameraClickListener = listener;
  }

  public void setOnBeautyClickListener(OnBeautyClickListener onBeautyClickListener) {
    this.onBeautyClickListener = onBeautyClickListener;
  }

  public interface OnCloseClickListener {
    void onCloseClick();
  }

  public interface OnSwitchCameraClickListener {
    void onSwitchCamera();
  }

  public interface OnBeautyClickListener {
    void onBeautyClick();
  }
}
