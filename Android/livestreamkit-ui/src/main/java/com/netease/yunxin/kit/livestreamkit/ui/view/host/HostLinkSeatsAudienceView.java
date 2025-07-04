// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.view.host;

import android.content.*;
import android.text.*;
import android.util.*;
import android.view.*;
import androidx.annotation.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.model.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.*;

public class HostLinkSeatsAudienceView extends BaseView {
  private static final String TAG = "AnchorLinkSeatsAudienceView";
  private LinkSeatsAudienceRecycleView linkSeatsRecycleView;

  public HostLinkSeatsAudienceView(@NonNull Context context) {
    super(context);
  }

  public HostLinkSeatsAudienceView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public HostLinkSeatsAudienceView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private final NELiveStreamListener streamListener =
      new NELiveStreamListener() {

        @Override
        public void onMemberJoinRtcChannel(@NonNull List<? extends NERoomMember> members) {
          for (NERoomMember member : members) {
            LiveRoomLog.i(TAG, "onMemberJoinRtcChannel " + member.getUuid());
          }
        }

        @Override
        public void onMemberLeaveRtcChannel(@NonNull List<? extends NERoomMember> members) {
          for (NERoomMember member : members) {
            LiveRoomLog.i(TAG, "onMemberLeaveRtcChannel " + member.getUuid());
          }
        }

        @Override
        public void onSeatListChanged(@NonNull List<NESeatItem> seatItems) {
          List<SeatMemberInfo> seatMemberInfoList = new ArrayList<>();
          for (NESeatItem item : seatItems) {
            LiveRoomLog.i(
                TAG,
                "onSeatListChanged user = " + item.getUser() + " status = " + item.getStatus());
            if (item.getStatus() == NESeatItemStatus.TAKEN
                && item.getUser() != null
                && item.getUserInfo() != null
                && !TextUtils.equals(item.getUser(), LiveStreamUtils.getLocalAccount())) {
              SeatView.SeatInfo seatInfo =
                  new SeatView.SeatInfo(
                      item.getUserInfo().getUser(),
                      item.getUserInfo().getUserName(),
                      item.getUserInfo().getIcon());
              SeatMemberInfo seatMemberInfo = new SeatMemberInfo(seatInfo, false);
              seatMemberInfoList.add(seatMemberInfo);
            }
          }

          linkSeatsRecycleView.setList(seatMemberInfoList);
        }
      };

  @Override
  protected void initView() {
    linkSeatsRecycleView = new LinkSeatsAudienceRecycleView(getContext());
    linkSeatsRecycleView.setUseScene(LinkSeatsAudienceRecycleView.UseScene.ANCHOR);
    addView(
        linkSeatsRecycleView,
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
  }

  @Override
  protected void addObserver() {
    NELiveStreamKit.getInstance().addLiveStreamListener(streamListener);
  }

  @Override
  protected void removeObserver() {
    NELiveStreamKit.getInstance().removeLiveStreamListener(streamListener);
  }
}
