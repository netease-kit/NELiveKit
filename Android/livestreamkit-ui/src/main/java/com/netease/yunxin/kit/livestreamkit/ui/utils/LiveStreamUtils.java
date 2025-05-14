// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.livestreamkit.ui.utils;

import android.text.TextUtils;
import com.netease.yunxin.kit.entertainment.common.LiveConstants;
import com.netease.yunxin.kit.entertainment.common.model.LiveInfo;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomInfo;
import com.netease.yunxin.kit.livestreamkit.ui.model.VoiceRoomSeat;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.roomkit.api.*;
import java.util.ArrayList;
import java.util.List;

public class LiveStreamUtils {

  public static int skipFrame = 5;

  public static boolean isCurrentHost() {
    return NELiveStreamKit.getInstance().getLocalMember() != null
        && TextUtils.equals(
            NELiveStreamKit.getInstance().getLocalMember().getRole().getName(),
            LiveConstants.ROLE_HOST);
  }

  public static boolean isMySelf(String uuid) {
    return NELiveStreamKit.getInstance().getLocalMember() != null
        && TextUtils.equals(NELiveStreamKit.getInstance().getLocalMember().getUuid(), uuid);
  }

  public static boolean isHost(String uuid) {
    NERoomMember member = getMember(uuid);
    if (member == null) {
      return false;
    }
    return TextUtils.equals(member.getRole().getName(), LiveConstants.ROLE_HOST);
  }

  public static NERoomMember getMember(String uuid) {
    List<NERoomMember> allMemberList = NELiveStreamKit.getInstance().getAllMemberList();
    for (int i = 0; i < allMemberList.size(); i++) {
      NERoomMember member = allMemberList.get(i);
      if (TextUtils.equals(member.getUuid(), uuid)) {
        return member;
      }
    }
    return null;
  }

  public static NERoomMember getHost() {
    List<NERoomMember> allMemberList = NELiveStreamKit.getInstance().getAllMemberList();
    for (int i = 0; i < allMemberList.size(); i++) {
      NERoomMember member = allMemberList.get(i);
      if (TextUtils.equals(member.getRole().getName(), LiveConstants.ROLE_HOST)) {
        return member;
      }
    }
    return null;
  }

  public static String getHostUuid() {
    return getHost() == null ? null : getHost().getUuid();
  }

  public static boolean isMute(String uuid) {
    NERoomMember member = getMember(uuid);
    if (member != null) {
      return !member.isAudioOn();
    }
    return true;
  }

  public static List<VoiceRoomSeat> createSeats() {
    int size = VoiceRoomSeat.SEAT_COUNT;
    List<VoiceRoomSeat> seats = new ArrayList<>(size);
    for (int i = 1; i < size; i++) {
      seats.add(new VoiceRoomSeat(i + 1));
    }
    return seats;
  }

  public static String getLocalName() {
    if (NELiveStreamKit.getInstance().getLocalMember() == null) {
      return "";
    }
    return NELiveStreamKit.getInstance().getLocalMember().getName();
  }

  public static String getLocalAccount() {
    if (NELiveStreamKit.getInstance().getLocalMember() == null) {
      return "";
    }
    return NELiveStreamKit.getInstance().getLocalMember().getUuid();
  }

  public static List<LiveInfo> voiceRoomInfos2LiveInfos(List<NELiveRoomInfo> voiceRoomInfos) {
    List<LiveInfo> result = new ArrayList<>();
    for (NELiveRoomInfo roomInfo : voiceRoomInfos) {
      result.add(neVoiceRoomInfo2RoomInfo(roomInfo));
    }
    return result;
  }

  public static LiveInfo neVoiceRoomInfo2RoomInfo(NELiveRoomInfo roomInfo) {
    if (roomInfo == null) {
      return null;
    }
    LiveInfo liveInfo = new LiveInfo();
    liveInfo.setRoomUuid(roomInfo.getLiveModel().getRoomUuid());
    if (roomInfo.getLiveModel().getLiveConfig() != null) {
      liveInfo.setPullUrl(roomInfo.getLiveModel().getLiveConfig().getPullRtmpUrl());
    }
    int onlineCount = 0;
    if (roomInfo.getLiveModel().getAudienceCount() != null
        && roomInfo.getLiveModel().getOnSeatCount() != null) {
      onlineCount =
          Math.max(
              roomInfo.getLiveModel().getAudienceCount() + 1,
              roomInfo.getLiveModel().getOnSeatCount());
    }
    liveInfo.setAudienceCount(onlineCount);
    liveInfo.setCover(roomInfo.getLiveModel().getCover());
    liveInfo.setLiveRecordId(roomInfo.getLiveModel().getLiveRecordId());
    liveInfo.setLiveTopic(roomInfo.getLiveModel().getLiveTopic());
    liveInfo.setAnchorAvatar(roomInfo.getAnchor().getAvatar());
    liveInfo.setAnchorNick(roomInfo.getAnchor().getNick());
    liveInfo.setAnchorUserUuid(roomInfo.getAnchor().getAccount());
    return liveInfo;
  }

  public static boolean isCurrentOnSeat(List<SeatView.SeatInfo> seatInfos) {
    if (seatInfos != null && !seatInfos.isEmpty()) {
      for (SeatView.SeatInfo seatInfo : seatInfos) {
        if (TextUtils.equals(seatInfo.uuid, getLocalAccount())) {
          return true;
        }
      }
      return false;
    }
    return false;
  }
}
