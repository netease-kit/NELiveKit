// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;
import com.faceunity.nama.dialog.BeautyBottomDialog;
import com.netease.yunxin.kit.common.ui.dialog.*;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.entertainment.common.dialog.*;
import com.netease.yunxin.kit.entertainment.common.utils.*;
import com.netease.yunxin.kit.livestreamkit.api.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.coaudience.dialog.*;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.LiveActivityAnchorBinding;
import com.netease.yunxin.kit.livestreamkit.ui.view.ChatRoomMsgRecyclerView;
import com.netease.yunxin.kit.livestreamkit.ui.view.VolumeSetup;
import com.netease.yunxin.kit.livestreamkit.ui.view.host.HostPreviewView;
import com.netease.yunxin.kit.livestreamkit.ui.viewmodel.HostLiveViewModel;
import java.util.List;

/** 主播页 */
public class LiveStreamAnchorActivity extends LiveStreamBaseActivity
    implements Permission.PermissionCallback {
  private final String TAG = "LiveStreamAnchorActivity";
  private final String[] permissions = {
    Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
  };
  private LiveActivityAnchorBinding binding;
  private TopPopupWindow permissionPop;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = LiveActivityAnchorBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    requestPermissionsIfNeeded();
    initObservers();
    initListeners();
  }

  private void initObservers() {
    getLiveViewModel()
        .getLiveStateData()
        .observe(
            this,
            liveState -> {
              if (liveState == HostLiveViewModel.LIVE_STATE_LIVING) {
                binding.hostPreview.setVisibility(View.GONE);
                binding.hostLivingView.setVisibility(View.VISIBLE);
              } else if (liveState == HostLiveViewModel.LIVE_STATE_PREVIEW) {
                binding.hostPreview.setVisibility(View.VISIBLE);
                binding.hostLivingView.setVisibility(View.GONE);
              } else if (liveState == HostLiveViewModel.LIVE_STATE_FINISH) {
                finish();
              }
            });
    getLiveViewModel()
        .getRoomInfoData()
        .observe(
            this,
            roomInfo -> {
              binding.hostLivingView.setLiveName(roomInfo.getLiveModel().getLiveTopic());
              binding.hostLivingView.setLiveId("ID：" + roomInfo.getLiveModel().getRoomUuid());
              binding.hostLivingView.setAnchorAvatar(roomInfo.getAnchor().getAvatar());
            });

    getLiveViewModel()
        .getMemberCount()
        .observe(
            this,
            memberCount -> {
              binding.hostLivingView.setMemberCount(memberCount);
            });

    // 监听已存在的直播间信息
    getLiveViewModel()
        .getExistingRoomInfo()
        .observe(
            this,
            roomInfo -> {
              if (roomInfo != null) {
                showExistingRoomDialog(roomInfo);
              }
            });
  }

  private void showExistingRoomDialog(NELiveStreamRoomInfo roomInfo) {
    ChoiceDialog dialog =
        new ChoiceDialog(this)
            .setTitle(getString(R.string.live_existing_room_title))
            .setPositiveButton(
                R.string.live_recovery,
                v -> {
                  // 进入已存在的直播间
                  getLiveViewModel().joinLive(username, avatar, roomInfo);
                })
            .setNegativeButton(R.string.live_cancel, v -> {});
    dialog.setCancelable(false);
    dialog.show();
  }

  private void initListeners() {
    binding.hostPreview.setOnCloseClickListener(
        new HostPreviewView.OnCloseClickListener() {
          @Override
          public void onCloseClick() {
            finish();
          }
        });

    binding.hostPreview.setOnBeautyClickListener(
        new HostPreviewView.OnBeautyClickListener() {
          @Override
          public void onBeautyClick() {
            BeautyBottomDialog dialog =
                new BeautyBottomDialog(
                    LiveStreamAnchorActivity.this, getLiveViewModel().getFaceUnityDataFactory());
            dialog.show(getSupportFragmentManager(), "MyDialogTag");
          }
        });

    binding.hostLivingView.setOnBeautyClickListener(
        new HostPreviewView.OnBeautyClickListener() {
          @Override
          public void onBeautyClick() {
            BeautyBottomDialog dialog =
                new BeautyBottomDialog(
                    LiveStreamAnchorActivity.this, getLiveViewModel().getFaceUnityDataFactory());
            dialog.show(getSupportFragmentManager(), "MyDialogTag");
          }
        });

    binding.hostPreview.setOnSwitchCameraClickListener(
        new HostPreviewView.OnSwitchCameraClickListener() {
          @Override
          public void onSwitchCamera() {
            getLiveViewModel().switchCamera();
          }
        });

    binding.hostLivingView.setOnLinkMicClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.network_error);
              return;
            }
            showLinkMicDialog();
          }
        });

    binding.hostLivingView.setOnLeaveRoomClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            LiveRoomLog.i(TAG, "click leave room button");
            doEndRoom();
          }
        });
  }

  private void initAnchorViews() {
    findBaseView();
    setupBaseViewInner();
    //    initSeatsInfo();
    binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(this);
  }

  /** 权限检查 */
  private void requestPermissionsIfNeeded() {
    if (!PermissionUtils.hasPermissions(this, permissions)) {
      permissionPop =
          new TopPopupWindow(
              LiveStreamAnchorActivity.this,
              R.string.app_permission_tip_microphone_title,
              R.string.app_permission_tip_microphone_content);
      binding
          .getRoot()
          .post(
              () -> {
                permissionPop.showAtLocation(binding.getRoot(), Gravity.TOP, 0, 100);
              });
      Permission.requirePermissions(LiveStreamAnchorActivity.this, permissions).request(this);
    } else {
      startPreview();
    }
  }

  @Override
  public void onGranted(@NonNull List<String> granted) {
    if (permissionPop != null) {
      permissionPop.dismiss();
    }
    if (permissions.length == granted.size()) {
      startPreview();

      //      enterRoomInner(
      //          voiceRoomInfo.getRoomUuid(),
      //          voiceRoomInfo.getNick(),
      //          voiceRoomInfo.getAvatar(),
      //          voiceRoomInfo.getLiveRecordId(),
      //          voiceRoomInfo.getRole());
      //      boolean isAnchor = NEVoiceRoomRole.HOST.getValue().equals(voiceRoomInfo.getRole());
      //      ivGift.setVisibility(isAnchor ? View.GONE : View.VISIBLE);
    }
  }

  @Override
  public void onDenial(List<String> permissionsDenial, List<String> permissionDenialForever) {
    LiveRoomLog.i(TAG, "finish onDenial");
    if (permissionPop != null) {
      permissionPop.dismiss();
    }
    ToastX.showShortToast(R.string.live_permission_deny_tips);
    finish();
  }

  @Override
  public void onException(Exception exception) {
    if (permissionPop != null) {
      permissionPop.dismiss();
    }
  }

  private void startPreview() {
    binding.hostPreview.setVisibility(View.VISIBLE);
    binding.hostPreview.initData(username, configId);
    getLiveViewModel().startPreview(binding.hostVideoView.getLocalVideoView());
  }

  //  protected void initSeatsInfo() {
  //    anchorSeatInfo = new SeatView.SeatInfo();
  //    anchorSeatInfo.nickname = voiceRoomInfo.getAnchor().getNick();
  //    anchorSeatInfo.avatar = voiceRoomInfo.getAnchor().getAvatar();
  //    anchorSeatInfo.isAnchor = true;
  //    anchorSeatInfo.isOnSeat = true;
  //    anchorSeatInfo.isMute = false;
  //    audienceSeatInfo = new SeatView.SeatInfo();
  //    audienceSeatInfo.isAnchor = false;
  //    audienceSeatInfo.isOnSeat = false;
  //  }

  private void findBaseView() {
    settingsContainer = findViewById(R.id.settings_container);
    settingsContainer.setOnClickListener(view -> settingsContainer.setVisibility(View.GONE));
    findViewById(R.id.settings_action_container).setOnClickListener(view -> {});
    SeekBar skRecordingVolume = settingsContainer.findViewById(R.id.recording_volume_control);
    skRecordingVolume.setOnSeekBarChangeListener(
        new VolumeSetup() {

          @Override
          protected void onVolume(int volume) {
            setAudioCaptureVolume(volume);
          }
        });
    SwitchCompat switchEarBack = settingsContainer.findViewById(R.id.ear_back);
    switchEarBack.setChecked(false);
    switchEarBack.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (!DeviceUtils.hasEarBack(LiveStreamAnchorActivity.this)) {
            buttonView.setChecked(false);
            return;
          }
          enableEarBack(isChecked);
        });
  }

  //  private void watchNetWork() {
  //    roomViewModel
  //        .getNetData()
  //        .observe(
  //            this,
  //            state -> {
  //              if (state == LiveStreamRoomViewModel.NET_AVAILABLE) { // 网可用
  //                onNetAvailable();
  //              } else { // 不可用
  //                onNetLost();
  //              }
  //            });
  //  }

  private void showLinkMicDialog() {
    if (ClickUtils.isFastClick()) {
      return;
    }
    HostLinkSeatDialog hostLinkSeatDialog = new HostLinkSeatDialog();
    Bundle bundle = new Bundle();
    hostLinkSeatDialog.setArguments(bundle);
    hostLinkSeatDialog.show(getSupportFragmentManager(), "audienceConnectDialog");

    // 显示连麦设置对话框
    //    new LinkMicSettingDialog((Activity) getContext())
    //        .setOnLinkMicSettingListener(new LinkMicSettingDialog.OnLinkMicSettingListener() {
    //          @Override
    //          public void onEnableLinkMic(boolean enable) {
    //            // 设置是否允许观众连麦
    //            NELiveStreamKit.getInstance().enableAudienceLinkMic(enable, new NELiveStreamCallback<Unit>() {
    //              @Override
    //              public void onSuccess(@Nullable Unit unit) {
    //                ToastX.showShortToast(enable ? R.string.link_mic_enabled : R.string.link_mic_disabled);
    //              }
    //
    //              @Override
    //              public void onFailure(int code, @Nullable String msg) {
    //                ToastX.showShortToast(R.string.operation_failed);
    //              }
    //            });
    //          }
    //        })
    //        .show();
  }

  protected void doEndRoom() {
    new ChoiceDialog(LiveStreamAnchorActivity.this)
        .setTitle(getString(R.string.live_end_live_title))
        .setContent(getString(R.string.live_end_live_tips))
        .setNegativeButton(getString(R.string.live_cancel), null)
        .setPositiveButton(getString(R.string.live_sure), v -> getLiveViewModel().endLive())
        .show();
  }

  @Override
  public void onBackPressed() {
    if (getLiveViewModel() != null
        && getLiveViewModel().getLiveStateData() != null
        && getLiveViewModel().getLiveStateData().getValue() != null
        && getLiveViewModel().getLiveStateData().getValue()
            == HostLiveViewModel.LIVE_STATE_LIVING) {
      new ChoiceDialog(LiveStreamAnchorActivity.this)
          .setTitle(getString(R.string.live_end_live_title))
          .setContent(getString(R.string.live_end_live_tips))
          .setNegativeButton(getString(R.string.live_cancel), null)
          .setPositiveButton(getString(R.string.live_sure), v -> getLiveViewModel().endLive())
          .show();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected HostLiveViewModel getLiveViewModel() {
    HostLiveViewModel viewModel = new ViewModelProvider(this).get(HostLiveViewModel.class);
    LiveRoomLog.d(TAG, "getLiveViewModel = " + viewModel);
    return viewModel;
  }

  @Override
  protected ChatRoomMsgRecyclerView getChatMsgListView() {
    return binding.hostLivingView.getChatRoomMsgRecyclerView();
  }

  @Override
  protected void onDestroy() {
    binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
    super.onDestroy();
  }
}
