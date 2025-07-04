// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view.host;

import android.app.*;
import android.content.*;
import android.os.CountDownTimer;
import android.util.*;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.constraintlayout.widget.*;
import androidx.lifecycle.*;
import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.entertainment.common.utils.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.impl.manager.*;
import com.netease.yunxin.kit.livestreamkit.impl.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.*;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.livestreamkit.ui.viewmodel.*;
import com.netease.yunxin.kit.roomkit.api.view.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HostVideoView extends BaseView implements NECoHostListener {
  private static final String TAG = "HostVideoView";
  private LiveHostVideoLayoutBinding binding;
  private HostLiveViewModel liveViewModel;
  private CountDownTimer pkCountDownTimer;

  public HostVideoView(@NonNull Context context) {
    this(context, null);
  }

  public HostVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HostVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void initView() {
    binding = LiveHostVideoLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
    ViewUtils.paddingStatusBarHeight((Activity) getContext(), binding.getRoot());
    liveViewModel =
        new ViewModelProvider((AppCompatActivity) mContext).get(HostLiveViewModel.class);
    binding.localVideoView.setZOrderMediaOverlay(true);
    binding.localVideoView.setScalingType(NERoomVideoView.VideoScalingType.SCALE_ASPECT_BALANCED);
  }

  public NERoomVideoView getLocalVideoView() {
    return binding.localVideoView;
  }

  private void change2SingleHost() {
    binding.remoteVideoView.setVisibility(View.GONE);
    binding.line.setVisibility(View.GONE);
    ViewGroup.LayoutParams localLayoutParams = binding.localVideoView.getLayoutParams();
    if (localLayoutParams instanceof ConstraintLayout.LayoutParams) {
      ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) localLayoutParams;
      params.topMargin = ScreenUtil.dip2px(0);
      params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; // 或 R.id.parent
      params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; // 或 R.id.parent
      params.height = ConstraintLayout.LayoutParams.MATCH_PARENT; // 关键修复：恢复全屏高度
      binding.localVideoView.setLayoutParams(params);
    } else {
      Log.e("TAG", "LayoutParams is not ConstraintLayout.LayoutParams");
    }
  }

  private void change2CoHost(@NonNull ConnectionUser connectUser) {
    // 显示远程视图并设置画布
    binding.line.setVisibility(View.VISIBLE);
    binding.remoteVideoView.setVisibility(View.VISIBLE);
    binding.remoteVideoView.setZOrderMediaOverlay(true);
    binding.remoteVideoView.setScalingType(NERoomVideoView.VideoScalingType.SCALE_ASPECT_BALANCED);
    NELiveStreamKit.getInstance()
        .setupRemoteVideoCanvas(binding.remoteVideoView, connectUser.getUserUuid());
    // 设置本地视图布局（右侧半屏）
    ViewGroup.LayoutParams localParams = binding.localVideoView.getLayoutParams();
    if (localParams instanceof ConstraintLayout.LayoutParams) {
      ConstraintLayout.LayoutParams localLayoutParams = (ConstraintLayout.LayoutParams) localParams;

      // 约束：本地视图在远程视图左侧
      localLayoutParams.topMargin =
          BarUtils.getStatusBarHeight(getContext()) + ScreenUtil.dip2px(44);
      localLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
      localLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;

      // 设置高度为屏幕一半
      localLayoutParams.height = ScreenUtil.getDisplayHeight() / 2;
      localLayoutParams.width = 0; // 由约束自动计算宽度

      binding.localVideoView.setLayoutParams(localLayoutParams);
    }
    // 设置远程视图布局（左侧半屏）
    ViewGroup.LayoutParams remoteParams = binding.remoteVideoView.getLayoutParams();
    if (remoteParams instanceof ConstraintLayout.LayoutParams) {
      ConstraintLayout.LayoutParams remoteLayoutParams =
          (ConstraintLayout.LayoutParams) remoteParams;

      // 约束：远程视图在父布局右侧
      remoteLayoutParams.topMargin =
          BarUtils.getStatusBarHeight(getContext()) + ScreenUtil.dip2px(44);
      remoteLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
      remoteLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;

      // 设置高度为屏幕一半
      remoteLayoutParams.height = ScreenUtil.getDisplayHeight() / 2;
      remoteLayoutParams.width = 0; // 由约束自动计算宽度

      binding.remoteVideoView.setLayoutParams(remoteLayoutParams);
    }
  }

  @Override
  protected void addObserver() {
    liveViewModel
        .getLiveStateData()
        .observe(
            (AppCompatActivity) getContext(),
            liveState -> {
              if (liveState == HostLiveViewModel.LIVE_STATE_LIVING) {
              } else if (liveState == HostLiveViewModel.LIVE_STATE_PREVIEW) {
              } else if (liveState == HostLiveViewModel.LIVE_STATE_FINISH) {
              } else if (liveState == HostLiveViewModel.LIVE_STATE_ERROR) {
              }
            });
    NELiveStreamKit.getInstance().getCoHostManager().addListener(this);
  }

  @Override
  protected void removeObserver() {
    NELiveStreamKit.getInstance().getCoHostManager().removeListener(this);
  }

  @Override
  public void onConnectionUserListChanged(
      @NonNull List<ConnectionUser> connectedList,
      @NonNull List<ConnectionUser> joinedList,
      @NonNull List<ConnectionUser> leavedList) {
    LiveRoomLog.i(TAG, "onConnectionUserListChanged connectedList = " + connectedList);
    if (connectedList.isEmpty()) {
      change2SingleHost();
      stopPkCountDown();
    } else {
      change2CoHost(connectedList.get(0));
      startPkCountDown(TimeUnit.MINUTES.toMillis(3));
    }
  }

  @Override
  public void onConnectionRequestReceived(
      @NonNull ConnectionUser inviter,
      @NonNull List<ConnectionUser> inviteeList,
      @Nullable String ext) {
    LiveRoomLog.i(TAG, "onConnectionRequestReceived inviter = " + inviter);
  }

  @Override
  public void onConnectionRequestCancelled(@NonNull ConnectionUser inviter) {
    LiveRoomLog.i(TAG, "onConnectionRequestCancelled inviter = " + inviter);
  }

  @Override
  public void onConnectionRequestAccept(@NonNull ConnectionUser invitee) {
    LiveRoomLog.i(TAG, "onConnectionRequestAccept invitee = " + invitee);
  }

  @Override
  public void onConnectionRequestReject(@NonNull ConnectionUser invitee) {
    LiveRoomLog.i(TAG, "onConnectionRequestReject invitee = " + invitee);
  }

  @Override
  public void onConnectionRequestTimeout(
      @NonNull ConnectionUser inviter,
      @NonNull List<ConnectionUser> inviteeList,
      @Nullable String ext) {
    LiveRoomLog.i(TAG, "onConnectionRequestTimeout inviter = " + inviter);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    stopPkCountDown();
  }

  public void startPkCountDown(long millisInFuture) {
    if (pkCountDownTimer != null) {
      pkCountDownTimer.cancel();
    }
    binding.tvPkCountdown.setVisibility(View.VISIBLE);
    pkCountDownTimer =
        new CountDownTimer(millisInFuture, 1000) {
          @Override
          public void onTick(long millisUntilFinished) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
            long seconds =
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                    - TimeUnit.MINUTES.toSeconds(minutes);
            binding.tvPkCountdown.setText(
                getContext().getString(R.string.co_host_count_down, minutes, seconds));
          }

          @Override
          public void onFinish() {
            binding.tvPkCountdown.setVisibility(View.GONE);
            NELiveStreamKit.getInstance().getCoHostManager().disconnect();
          }
        };
    pkCountDownTimer.start();
  }

  public void stopPkCountDown() {
    if (pkCountDownTimer != null) {
      pkCountDownTimer.cancel();
      pkCountDownTimer = null;
    }
    binding.tvPkCountdown.setVisibility(View.GONE);
  }
}
