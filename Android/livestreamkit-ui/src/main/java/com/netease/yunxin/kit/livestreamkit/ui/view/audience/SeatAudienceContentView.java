// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view.audience;

import android.*;
import android.content.*;
import android.text.*;
import android.util.*;
import android.view.*;
import androidx.annotation.*;
import com.netease.yunxin.kit.common.ui.dialog.*;
import com.netease.yunxin.kit.common.ui.utils.*;
import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.entertainment.common.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.model.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import com.netease.yunxin.kit.roomkit.api.view.*;
import java.util.*;
import kotlin.*;

public class SeatAudienceContentView extends BaseAudienceContentView
    implements Permission.PermissionCallback {
  private static final String TAG = "SeatAudienceContentView";
  private final String[] permissions = {
    Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
  };
  private TopPopupWindow permissionPop;
  private NERoomVideoView rtcVideoView;
  private LinkSeatsAudienceRecycleView linkSeatsRecycleView;

  public SeatAudienceContentView(@NonNull Context context) {
    super(context);
  }

  public SeatAudienceContentView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SeatAudienceContentView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void initView() {
    super.initView();

    linkSeatsRecycleView = new LinkSeatsAudienceRecycleView(mContext);
    linkSeatsRecycleView.setVisibility(GONE);
    linkSeatsRecycleView.setUseScene(LinkSeatsAudienceRecycleView.UseScene.AUDIENCE);

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    params.topMargin = SizeUtils.dp2px(90f);
    params.rightMargin = SizeUtils.dp2px(6f);
    params.gravity = Gravity.TOP | Gravity.END;
    addView(linkSeatsRecycleView, params);
  }

  @Override
  protected void onLocalSeatRequestApproved() {
    //    showRtcView(account);
    requestPermissionsIfNeeded();
  }

  @Override
  protected void onLocalSeatInvitationAccepted() {
    requestPermissionsIfNeeded();
  }

  @Override
  protected void onLivingSeatListChanged(List<NESeatItem> seatItems) {
    List<SeatMemberInfo> seatMemberInfoList = new ArrayList<>();
    for (NESeatItem item : seatItems) {
      LiveRoomLog.i(
          TAG,
          "onSeatListChanged user = "
              + item.getUser()
              + " userName = "
              + item.getUserName()
              + " status = "
              + item.getStatus());
      if (item.getStatus() == NESeatItemStatus.TAKEN
          && !TextUtils.equals(item.getUser(), LiveStreamUtils.getLocalAccount())) {
        SeatView.SeatInfo seatInfo =
            new SeatView.SeatInfo(item.getUser(), item.getUserName(), item.getIcon());
        SeatMemberInfo seatMemberInfo = new SeatMemberInfo(seatInfo, false);
        seatMemberInfoList.add(seatMemberInfo);
      }
    }

    linkSeatsRecycleView.setList(seatMemberInfoList);
  }

  /** 权限检查 */
  private void requestPermissionsIfNeeded() {
    if (!PermissionUtils.hasPermissions(getContext(), permissions)) {
      permissionPop =
          new TopPopupWindow(
              getContext(),
              R.string.app_permission_tip_microphone_title,
              R.string.app_permission_tip_microphone_content);
      post(
          () -> {
            permissionPop.showAtLocation(getRootView(), Gravity.TOP, 0, 100);
          });
      Permission.requirePermissions(getContext(), permissions).request(this);
    } else {
      joinRtcAndShowRtcUI();
    }
  }

  @Override
  public void onGranted(List<String> granted) {
    if (permissionPop != null) {
      permissionPop.dismiss();
    }
    joinRtcAndShowRtcUI();
  }

  @Override
  public void onDenial(List<String> list, List<String> list1) {
    if (permissionPop != null) {
      permissionPop.dismiss();
    }
    joinRtcAndShowRtcUI();
  }

  @Override
  public void onException(Exception e) {}

  private void showRtcView(SeatMemberInfo memberInfo, boolean fetchSeat) {
    // 添加RTC流播放
    if (rtcVideoView == null) {
      rtcVideoView = new NERoomVideoView(mContext);
      addView(rtcVideoView, 0, generateDefaultLayoutParams());
    }
    //设置主播的RTC流画面
    rtcVideoView.setScalingType(NERoomVideoView.VideoScalingType.SCALE_ASPECT_BALANCED);
    rtcVideoView.setZOrderMediaOverlay(false);
    NELiveStreamKit.getInstance().setupLocalVideoCanvas(rtcVideoView);
    rtcVideoView.setVisibility(VISIBLE);
    cdnStreamTextureView.setVisibility(GONE);
    linkSeatsRecycleView.setVisibility(VISIBLE);
  }

  @Override
  protected void showCdnView() {
    super.showCdnView();
    if (rtcVideoView != null) {
      rtcVideoView.setVisibility(GONE);
    }
    if (linkSeatsRecycleView != null) {
      linkSeatsRecycleView.setVisibility(GONE);
    }
  }

  @Override
  protected void onLocalUserJoinSeat() {
    LiveRoomLog.i(TAG, "onLocalJoinSeat");
    NERoomMember member = NELiveStreamKit.getInstance().getLocalMember();
    SeatView.SeatInfo seatInfo =
        new SeatView.SeatInfo(member.getUuid(), member.getName(), member.getAvatar());
    SeatMemberInfo seatMemberInfo = new SeatMemberInfo(seatInfo, false);
    //      DurationStatisticTimer.DurationUtil.setBeginTimeStamp(System.currentTimeMillis());
    showRtcView(seatMemberInfo, false);
  }

  protected void onRemoteUserJoinSeat(NERoomMember member) {
    LiveRoomLog.i(TAG, "onRemoteJoinSeat members = " + member);

    //    if (showRoomMsg) {
    //      infoBinding.crvMsgList.appendItem(ChatRoomMsgCreator.createSeatEnter(member.seatInfo.nickName))
    //    }
  }

  protected void onRemoteUserLeaveSeat(String uuid) {
    LiveRoomLog.i(TAG, "onRemoteLeaveSeat uuid = " + uuid);
    //    if (showRoomMsg) {
    //      infoBinding.crvMsgList.appendItem(ChatRoomMsgCreator.createSeatExit(member.seatInfo.nickName))
    //    }

  }

  @Override
  protected void onLocalUserLeaveSeat() {
    super.onLocalUserLeaveSeat();
    LiveRoomLog.i(TAG, "onLocalLeaveSeat");
    leaveRtcAndHideRtcUI();
  }

  private void joinRtcAndShowRtcUI() {

    NELiveStreamKit.getInstance()
        .changeLocalMemberRole(
            LiveConstants.ROLE_AUDIENCE_ON_SEAT,
            new NECallback2<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit data) {
                LiveRoomLog.i(TAG, "changeLocalMemberRole success");
              }

              @Override
              public void onError(int code, @Nullable String message) {
                LiveRoomLog.i(TAG, "changeLocalMemberRole failed");
              }
            });
  }

  private void leaveRtcAndHideRtcUI() {
    NELiveStreamKit.getInstance()
        .changeLocalMemberRole(
            LiveConstants.ROLE_AUDIENCE,
            new NECallback2<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit data) {
                LiveRoomLog.i(TAG, "changeLocalMemberRole success");
              }

              @Override
              public void onError(int code, @Nullable String message) {
                LiveRoomLog.i(TAG, "changeLocalMemberRole failed");
              }
            });
  }
}
