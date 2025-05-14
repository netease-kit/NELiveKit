// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.entertainment.common.LiveConstants;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.entertainment.common.gift.GifAnimationView;
import com.netease.yunxin.kit.entertainment.common.gift.GiftCache;
import com.netease.yunxin.kit.entertainment.common.gift.GiftRender;
import com.netease.yunxin.kit.entertainment.common.utils.BluetoothHeadsetUtil;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomBatchRewardTarget;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.kit.livestreamkit.ui.service.KeepAliveService;
import com.netease.yunxin.kit.livestreamkit.ui.utils.LiveStreamUtils;
import com.netease.yunxin.kit.livestreamkit.ui.view.ChatRoomMsgRecyclerView;
import com.netease.yunxin.kit.livestreamkit.ui.view.SeatView;
import com.netease.yunxin.kit.livestreamkit.ui.viewmodel.BaseLiveViewModel;
import java.util.List;

/** 主播与观众基础页，包含所有的通用UI元素 */
public abstract class LiveStreamBaseActivity extends BaseActivity
    implements ViewTreeObserver.OnGlobalLayoutListener {

  public static final String TAG = "LiveStreamBaseActivity";

  public static final String TAG_REPORT_PAGE = "page_livestream_detail";

  private static final int KEY_BOARD_MIN_SIZE = SizeUtils.dp2px(80);

  private static final int AUDIENCE_SEAT_INDEX = 2;

  // 各种控制开关
  protected FrameLayout settingsContainer;

  private int rootViewVisibleHeight;

  protected int earBack = 100;

  private GiftRender giftRender;
  protected SeatView.SeatInfo anchorSeatInfo;
  protected SeatView.SeatInfo audienceSeatInfo;
  private static final int ROOM_MEMBER_MAX_COUNT = 2;
  protected int liveType;
  private SimpleServiceConnection mServiceConnection;

  protected boolean isOversea = false;
  protected int configId;
  protected String username;
  protected String avatar;
  private final BluetoothHeadsetUtil.BluetoothHeadsetStatusObserver
      bluetoothHeadsetStatusChangeListener =
          new BluetoothHeadsetUtil.BluetoothHeadsetStatusObserver() {
            @Override
            public void connect() {
              if (!BluetoothHeadsetUtil.hasBluetoothConnectPermission(
                  LiveStreamBaseActivity.this)) {
                BluetoothHeadsetUtil.requestBluetoothConnectPermission(LiveStreamBaseActivity.this);
              }
            }

            @Override
            public void disconnect() {}
          };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 屏幕常亮
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    initIntent();

    bindForegroundService();
    BluetoothHeadsetUtil.registerBluetoothHeadsetStatusObserver(
        bluetoothHeadsetStatusChangeListener);
    if (BluetoothHeadsetUtil.isBluetoothHeadsetConnected()
        && !BluetoothHeadsetUtil.hasBluetoothConnectPermission(LiveStreamBaseActivity.this)) {
      BluetoothHeadsetUtil.requestBluetoothConnectPermission(LiveStreamBaseActivity.this);
    }
    initHeader();
    initGiftAnimation();
    initDataObserver();
  }

  protected void initIntent() {
    liveType = getIntent().getIntExtra(LiveConstants.INTENT_LIVE_TYPE, NELiveType.LIVE_TYPE_VOICE);
    isOversea = getIntent().getBooleanExtra(LiveConstants.INTENT_IS_OVERSEA, false);
    configId = getIntent().getIntExtra(LiveConstants.INTENT_KEY_CONFIG_ID, 0);
    username = getIntent().getStringExtra(LiveConstants.INTENT_USER_NAME);
  }

  protected void initHeader() {
    setupBaseViewInner();
    String countStr = String.format(getString(R.string.live_people_online), "0");
    //      getMemberCountTextView().setText(countStr);
  }

  @Override
  protected void onDestroy() {
    giftRender.release();
    unbindForegroundService();
    BluetoothHeadsetUtil.unregisterBluetoothHeadsetStatusObserver(
        bluetoothHeadsetStatusChangeListener);
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (settingsContainer != null && settingsContainer.getVisibility() == View.VISIBLE) {
      settingsContainer.setVisibility(View.GONE);
      return;
    }
    LiveRoomLog.i(TAG, "onBackPressed");
    super.onBackPressed();
  }

  @Override
  public void onGlobalLayout() {
    int preHeight = rootViewVisibleHeight;
    //获取当前根视图在屏幕上显示的大小
    Rect r = new Rect();
    getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
    rootViewVisibleHeight = r.height();
    if (preHeight == 0 || preHeight == rootViewVisibleHeight) {
      return;
    }
    //根视图显示高度变大超过KEY_BOARD_MIN_SIZE，可以看作软键盘隐藏了
    if (rootViewVisibleHeight - preHeight >= KEY_BOARD_MIN_SIZE) {
      getChatMsgListView().toLatestMsg();
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  protected void setupBaseViewInner() {}

  protected void onNetLost() {
    Bundle bundle = new Bundle();
  }

  protected void onNetAvailable() {}

  //  private void initViewAfterJoinRoom() {
  //    initDataObserver();
  //    roomViewModel.initDataOnJoinRoom();
  //    viewModel.initialize(voiceRoomInfo);
  //    if (LiveStreamUtils.isCurrentHost()) {
  //
  //    } else {
  //      NEVoiceRoomMember hostMember = LiveStreamUtils.getHost();
  //      if (hostMember != null) {
  //        updateAnchorUI(hostMember.getName(), hostMember.getAvatar(), hostMember.isAudioOn());
  //      }
  //      roomViewModel.getSeatInfo();
  //    }
  //  }

  private void updateAnchorUI(String nick, String avatar, boolean isAudioOn) {
    anchorSeatInfo.nickname = nick;
    anchorSeatInfo.avatar = avatar;
    anchorSeatInfo.isAudioMute = !isAudioOn;
    //    seatsLayout.setAnchorSeatInfo(anchorSeatInfo);
  }

  protected void initDataObserver() {
    //    roomViewModel
    //        .getMemberCountData()
    //        .observe(
    //            this,
    //            count -> {
    //              String countStr = String.format(getString(R.string.live_people_online), count + "");
    //              tvMemberCount.setText(countStr);
    //            });
    //    roomViewModel
    //        .getOnSeatListData()
    //        .observe(
    //            this,
    //            seatList -> {
    //              List<VoiceRoomSeat> audienceSeats = new ArrayList<>();
    //              for (VoiceRoomSeat model : seatList) {
    //                if (model.getSeatIndex() == AUDIENCE_SEAT_INDEX) {
    //                  audienceSeats.add(model);
    //                }
    //                final NEVoiceRoomMember member = model.getMember();
    //                if (member != null && LiveStreamUtils.isHost(member.getAccount())) {
    //                  updateAnchorUI(member.getName(), member.getAvatar(), member.isAudioOn());
    //                }
    //              }
    //              if (audienceSeats.size() == 1) {
    //                NEVoiceRoomMember member = audienceSeats.get(0).getMember();
    //                if (member != null) {
    //                  audienceSeatInfo.isOnSeat = true;
    //                  audienceSeatInfo.nickname = member.getName();
    //                  audienceSeatInfo.avatar = member.getAvatar();
    //                  audienceSeatInfo.isMute = !member.isAudioOn();
    //                } else {
    //                  audienceSeatInfo.isOnSeat = false;
    //                }
    ////                seatsLayout.setAnchorSeatInfo(anchorSeatInfo);
    ////                seatsLayout.setAudienceSeatInfo(audienceSeatInfo);
    //              }
    //            });
    //
    getLiveViewModel()
        .getChatRoomMsgData()
        .observe(
            this,
            charSequence -> {
              getChatMsgListView().appendItem(charSequence);
            });

    getLiveViewModel()
        .getErrorData()
        .observe(
            this,
            endReason -> {
              if (endReason == NEVoiceRoomEndReason.CLOSE_BY_MEMBER) {
                if (!LiveStreamUtils.isCurrentHost()) {
                  ToastUtils.INSTANCE.showShortToast(
                      LiveStreamBaseActivity.this, getString(R.string.live_host_close_room));
                }
                LiveRoomLog.i(TAG, "finish endReason == NEVoiceRoomEndReason.CLOSE_BY_MEMBER");

              } else {
                LiveRoomLog.i(TAG, "finish endReason = " + endReason);
              }
              finish();
            });

    getLiveViewModel()
        .bachRewardData
        .observe(
            this,
            batchReward -> {
              LiveRoomLog.i(TAG, "bachRewardData observe giftModel:" + batchReward);
              List<NELiveRoomBatchRewardTarget> targets = batchReward.getTargets();
              if (targets.isEmpty()) {
                return;
              }
              for (NELiveRoomBatchRewardTarget target : targets) {
                CharSequence batchGiftReward =
                    ChatRoomMsgCreator.createBatchGiftReward(
                        LiveStreamBaseActivity.this,
                        batchReward.getUserName(),
                        target.getUserName(),
                        GiftCache.getGift(batchReward.getGiftId()).getName(),
                        batchReward.getGiftCount(),
                        GiftCache.getGift(batchReward.getGiftId()).getStaticIconResId());
                getChatMsgListView().appendItem(batchGiftReward);
                LiveRoomLog.i(TAG, "target:" + target);
              }
              giftRender.addGift(GiftCache.getGift(batchReward.getGiftId()).getDynamicIconResId());
            });
    //
    ////    roomViewModel.anchorAvatarAnimation.observe(
    ////        this, show -> seatsLayout.showAnchorAvatarAnimal(show));
    ////
    ////    roomViewModel.audienceAvatarAnimation.observe(
    ////        this, show -> seatsLayout.showAudienceAvatarAnimal(show));
    //
  }

  protected void setAudioCaptureVolume(int volume) {
    NELiveStreamKit.getInstance().adjustRecordingSignalVolume(volume);
  }

  protected int enableEarBack(boolean enable) {
    if (enable) {
      return NELiveStreamKit.getInstance().enableEarback(earBack);
    } else {
      return NELiveStreamKit.getInstance().disableEarback();
    }
  }

  protected void initGiftAnimation() {
    GifAnimationView gifAnimationView = new GifAnimationView(this);
    int size = ScreenUtil.getDisplayWidth();
    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(size, size);
    layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
    layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
    ViewGroup root = (ViewGroup) getWindow().getDecorView();
    root.addView(gifAnimationView, layoutParams);
    gifAnimationView.bringToFront();
    giftRender = new GiftRender();
    giftRender.init(gifAnimationView);
  }

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  private void bindForegroundService() {
    Intent intent = new Intent();
    intent.setClass(this, KeepAliveService.class);
    mServiceConnection = new SimpleServiceConnection();
    bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
  }

  private void unbindForegroundService() {
    if (mServiceConnection != null) {
      unbindService(mServiceConnection);
    }
  }

  private class SimpleServiceConnection implements ServiceConnection {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

      if (service instanceof KeepAliveService.SimpleBinder) {
        LiveRoomLog.i(TAG, "onServiceConnect");
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      LiveRoomLog.i(TAG, "onServiceDisconnected");
    }
  }

  protected abstract BaseLiveViewModel getLiveViewModel();

  protected abstract ChatRoomMsgRecyclerView getChatMsgListView();
}
