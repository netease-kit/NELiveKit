// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.entertainment.common.LiveConstants;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveStreamRoomInfo;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.LiveActivityAudienceBinding;
import com.netease.yunxin.kit.livestreamkit.ui.model.SeatEvent;
import com.netease.yunxin.kit.livestreamkit.ui.model.VoiceRoomSeat;
import com.netease.yunxin.kit.livestreamkit.ui.view.ChatRoomMsgRecyclerView;
import com.netease.yunxin.kit.livestreamkit.ui.viewmodel.AudienceLiveViewModel;

/** 观众页 */
public class LiveStreamAudienceActivity extends LiveStreamBaseActivity {
  private final String TAG = "LiveStreamAudienceActivity";

  private LiveActivityAudienceBinding binding;
  private NELiveStreamRoomInfo roomInfo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = LiveActivityAudienceBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initViews();
    initDate();
  }

  private void initViews() {
    // 设置返回按钮点击事件
    binding.audienceLivingView.setOnLeaveRoomClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onBackPressed();
          }
        });

    // 设置主播信息
    if (roomInfo != null) {
      if (!TextUtils.isEmpty(roomInfo.getAnchor().getAvatar())) {
        binding.audienceLivingView.setAnchorAvatar(roomInfo.getAnchor().getAvatar());
      }
      if (!TextUtils.isEmpty(roomInfo.getLiveModel().getLiveTopic())) {
        binding.audienceLivingView.setLiveName(roomInfo.getLiveModel().getLiveTopic());
      }
      if (!TextUtils.isEmpty(roomInfo.getLiveModel().getRoomUuid())) {
        binding.audienceLivingView.setLiveId("ID：" + roomInfo.getLiveModel().getRoomUuid());
      }
      if (roomInfo.getLiveModel().getAudienceCount() != null) {
        binding.audienceLivingView.setMemberCount(roomInfo.getLiveModel().getAudienceCount());
      }
    }
  }

  private void initDate() {
    binding.audienceContentView.setRoomInfo(roomInfo);
    getLiveViewModel().joinLive(username, avatar, roomInfo);
  }

  @Override
  protected void initIntent() {
    super.initIntent();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      roomInfo =
          getIntent()
              .getSerializableExtra(LiveConstants.INTENT_LIVE_INFO, NELiveStreamRoomInfo.class);
    } else {
      roomInfo =
          (NELiveStreamRoomInfo) (getIntent().getSerializableExtra(LiveConstants.INTENT_LIVE_INFO));
    }
  }

  @Override
  public void initDataObserver() {
    super.initDataObserver();
    getLiveViewModel()
        .getLiveStateData()
        .observe(
            this,
            liveState -> {
              LiveRoomLog.d(TAG, "initDataObserver live state change, liveState:" + liveState);
              if (liveState == AudienceLiveViewModel.LIVE_STATE_FINISH) {
                binding.audienceErrorView.setVisibility(View.VISIBLE);
                binding.audienceErrorView.renderInfo(
                    roomInfo.getAnchor().getAvatar(), roomInfo.getAnchor().getNick());
              }
            });

    getLiveViewModel()
        .getCurrentSeatEvent()
        .observe(
            this,
            event -> {
              LiveRoomLog.d(TAG, "initDataObserver currentSeatEvent,event:" + event);
              switch (event.getReason()) {
                case VoiceRoomSeat.Reason.ANCHOR_INVITE:
                case VoiceRoomSeat.Reason.ANCHOR_APPROVE_APPLY:
                  onEnterSeat(event, false);
                  break;
              }
            });
    getLiveViewModel()
        .getCurrentSeatState()
        .observe(
            this,
            integer -> {
              LiveRoomLog.d(TAG, "initDataObserver currentSeatState,integer:" + integer);
            });

    // 观察在线人数变化
    getLiveViewModel()
        .getMemberCountData()
        .observe(
            this,
            count -> {
              LiveRoomLog.d(TAG, "initDataObserver getMemberCountData,count:" + count);
              binding.audienceLivingView.setMemberCount(count);
            });
  }

  @Override
  protected AudienceLiveViewModel getLiveViewModel() {
    AudienceLiveViewModel viewModel = new ViewModelProvider(this).get(AudienceLiveViewModel.class);
    LiveRoomLog.d(TAG, "getLiveViewModel = " + viewModel);
    return viewModel;
  }

  public void onEnterSeat(SeatEvent event, boolean last) {}

  @Override
  protected ChatRoomMsgRecyclerView getChatMsgListView() {
    return binding.audienceLivingView.getChatRoomMsgRecyclerView();
  }

  protected void doLeaveRoom() {
    getLiveViewModel().leaveLive();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    if (getLiveViewModel() != null) {
      doLeaveRoom();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
