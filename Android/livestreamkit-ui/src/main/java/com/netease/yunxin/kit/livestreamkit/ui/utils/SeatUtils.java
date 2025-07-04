// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.utils;

import android.text.*;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomOnSeatType;
import com.netease.yunxin.kit.livestreamkit.api.model.NEVoiceRoomSeatItemStatus;
import com.netease.yunxin.kit.livestreamkit.ui.model.*;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.roomkit.api.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.ArrayList;
import java.util.List;

public class SeatUtils {

  public static String getCurrentUuid() {
    if (NELiveStreamKit.getInstance().getLocalMember() == null) {
      return "";
    }
    return NELiveStreamKit.getInstance().getLocalMember().getUuid();
  }

  public static String getMemberNick(String uuid) {
    NERoomMember member = LiveStreamUtils.getMember(uuid);
    if (member != null) {
      return member.getName();
    }
    return "";
  }

  public static List<SeatView.SeatInfo> transNESeatItems2OnSeatInfos(
      List<NESeatItem> neSeatItemList) {
    List<SeatView.SeatInfo> seatInfoList = new ArrayList<>();
    for (NESeatItem item : neSeatItemList) {
      if (item.getStatus() == NEVoiceRoomSeatItemStatus.TAKEN
          && item.getUser() != null
          && item.getUserInfo() != null
          && !TextUtils.equals(item.getUserInfo().getUser(), LiveStreamUtils.getHostUuid())) {
        SeatView.SeatInfo seatInfo =
            new SeatView.SeatInfo(
                item.getUserInfo().getUser(),
                item.getUserInfo().getUserName(),
                item.getUserInfo().getIcon());
        seatInfoList.add(seatInfo);
      }
    }

    return seatInfoList;
  }

  public static List<VoiceRoomSeat> transNESeatItem2VoiceRoomSeat(List<NESeatItem> neSeatItemList) {
    List<VoiceRoomSeat> onSeatList = new ArrayList<>();
    for (NESeatItem item : neSeatItemList) {
      NERoomMember user = getMember(item.getUser());
      int status;
      switch (item.getStatus()) {
        case NEVoiceRoomSeatItemStatus.WAITING:
          status = VoiceRoomSeat.Status.APPLY;
          break;
        case NEVoiceRoomSeatItemStatus.CLOSED:
          status = VoiceRoomSeat.Status.CLOSED;
          break;
        case NEVoiceRoomSeatItemStatus.TAKEN:
          status = VoiceRoomSeat.Status.ON;
          break;
        default:
          status = VoiceRoomSeat.Status.INIT;
          break;
      }
      final int reason;
      if (item.getOnSeatType() == NEVoiceRoomOnSeatType.REQUEST) {
        reason = VoiceRoomSeat.Reason.ANCHOR_APPROVE_APPLY;
      } else if (item.getOnSeatType() == NEVoiceRoomOnSeatType.INVITATION) {
        reason = VoiceRoomSeat.Reason.ANCHOR_INVITE;
      } else {
        reason = VoiceRoomSeat.Reason.NONE;
      }
      onSeatList.add(new VoiceRoomSeat(item.getIndex(), status, reason, user));
    }
    return onSeatList;
  }

  public static NERoomMember getMember(String account) {
    List<NERoomMember> allMemberList = NELiveStreamKit.getInstance().getAllMemberList();
    if (!allMemberList.isEmpty()) {
      for (NERoomMember neVoiceRoomMember : allMemberList) {
        if (neVoiceRoomMember.getUuid().equals(account)) {
          return neVoiceRoomMember;
        }
      }
    }
    return null;
  }
}
