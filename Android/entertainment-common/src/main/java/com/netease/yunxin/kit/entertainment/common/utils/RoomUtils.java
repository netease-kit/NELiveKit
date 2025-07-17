// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.entertainment.common.utils;

import android.text.TextUtils;
import com.netease.yunxin.kit.corekit.service.XKitServiceManager;
import com.netease.yunxin.kit.entertainment.common.LiveConstants;
import com.netease.yunxin.kit.entertainment.common.model.RoomSeat;
import com.netease.yunxin.kit.roomkit.api.NERoomContext;
import com.netease.yunxin.kit.roomkit.api.NERoomKit;
import com.netease.yunxin.kit.roomkit.api.NERoomMember;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.*;

public class RoomUtils {
  private static final String ROOM_SERVICE_NAME = "RoomService";

  public static boolean isLocalAnchor(String roomUuid) {
    NERoomMember localMember = getLocalMember(roomUuid);
    if (localMember != null) {
      return TextUtils.equals(localMember.getRole().getName(), LiveConstants.ROLE_HOST);
    }

    return false;
  }

  public static boolean isLocal(String roomUuid, String uuid) {
    NERoomMember localMember = getLocalMember(roomUuid);
    if (localMember != null) {
      return TextUtils.equals(localMember.getUuid(), uuid);
    }
    return false;
  }

  public static String getLocalAccount(String roomUuid) {
    NERoomMember localMember = getLocalMember(roomUuid);
    if (localMember != null) {
      return localMember.getUuid();
    }
    return "";
  }

  public static String getLocalName(String roomUuid) {
    NERoomMember localMember = getLocalMember(roomUuid);
    if (localMember != null) {
      return localMember.getName();
    }
    return "";
  }

  public static String getMemberName(String roomUuid, String uuid) {
    NERoomMember member = getMember(roomUuid, uuid);
    if (member != null) {
      return member.getName();
    }
    return "";
  }

  public static boolean isHost(String roomUuid, String uuid) {
    NERoomMember member = getMember(roomUuid, uuid);
    if (member == null) {
      return false;
    }
    return TextUtils.equals(member.getRole().getName(), LiveConstants.ROLE_HOST);
  }

  public static NERoomMember getLocalMember(String roomUuid) {
    NERoomContext roomContext = getRoomContext(roomUuid);
    if (roomContext != null) {
      return roomContext.getLocalMember();
    }
    return null;
  }

  public static List<NERoomMember> getAllMembers(String roomUuid) {
    NERoomContext roomContext = getRoomContext(roomUuid);
    if (roomContext != null) {
      List<NERoomMember> allMemberList = new ArrayList<>(roomContext.getRemoteMembers());
      allMemberList.add(roomContext.getLocalMember());
      return allMemberList;
    }
    return null;
  }

  public static NERoomMember getMember(String roomUuid, String uuid) {
    List<NERoomMember> allMemberList = getAllMembers(roomUuid);
    if (allMemberList != null) {
      for (int i = 0; i < allMemberList.size(); i++) {
        NERoomMember member = allMemberList.get(i);
        if (TextUtils.equals(member.getUuid(), uuid)) {
          return member;
        }
      }
    }
    return null;
  }

  public static NERoomMember getHost(String roomUuid) {
    List<NERoomMember> allMemberList = getAllMembers(roomUuid);
    if (allMemberList != null) {
      for (int i = 0; i < allMemberList.size(); i++) {
        NERoomMember member = allMemberList.get(i);
        if (TextUtils.equals(member.getRole().getName(), LiveConstants.ROLE_HOST)) {
          return member;
        }
      }
    }
    return null;
  }

  public static String getHostUuid(String roomUuid) {
    NERoomMember hostMember = getHost(roomUuid);
    if (hostMember != null) {
      return hostMember.getUuid();
    }
    return "";
  }

  public static NERoomMember getHost2(String roomUuid) {
    NERoomContext roomContext = getRoomContext(roomUuid);
    if (roomContext != null) {
      List<NERoomMember> allMemberList = roomContext.getRemoteMembers();
      for (int i = 0; i < allMemberList.size(); i++) {
        NERoomMember member = allMemberList.get(i);
        if (TextUtils.equals(member.getRole().getName(), LiveConstants.ROLE_HOST)) {
          return member;
        }
      }
    }
    return null;
  }

  public static NERoomContext getRoomContext(String roomUuid) {
    return NERoomKit.getInstance().getRoomService().getRoomContext(roomUuid);
  }

  public static List<RoomSeat> createSeats() {
    int size = RoomSeat.SEAT_COUNT;
    List<RoomSeat> seats = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      seats.add(new RoomSeat(i + 1));
    }
    return seats;
  }

  public static boolean isShowFloatView() {
    Object result =
        XKitServiceManager.Companion.getInstance()
            .callService(ROOM_SERVICE_NAME, "isShowFloatView", null);
    return result instanceof Boolean && (boolean) result;
  }

  public static void stopFloatPlay() {
    XKitServiceManager.Companion.getInstance()
        .callService(ROOM_SERVICE_NAME, "stopFloatPlay", null);
  }

  public static List<RoomSeat> transNESeatItem2VoiceRoomSeat2(
      String roomUuid, List<NESeatItem> neSeatItemList) {
    if (neSeatItemList == null) neSeatItemList = Collections.emptyList();
    List<RoomSeat> onSeatList = new ArrayList<>();
    for (NESeatItem item : neSeatItemList) {
      NERoomMember user = RoomUtils.getMember(roomUuid, item.getUser());
      int status;
      switch (item.getStatus()) {
        case NESeatItemStatus.WAITING:
          status = RoomSeat.Status.APPLY;
          break;
        case NESeatItemStatus.CLOSED:
          status = RoomSeat.Status.CLOSED;
          break;
        case NESeatItemStatus.TAKEN:
          status = RoomSeat.Status.ON;
          break;
        default:
          status = RoomSeat.Status.INIT;
          break;
      }
      final int reason;
      if (item.getOnSeatType() == NESeatOnSeatType.REQUEST) {
        reason = RoomSeat.Reason.ANCHOR_APPROVE_APPLY;
      } else if (item.getOnSeatType() == NESeatOnSeatType.INVITATION) {
        reason = RoomSeat.Reason.ANCHOR_INVITE;
      } else {
        reason = RoomSeat.Reason.NONE;
      }
      onSeatList.add(new RoomSeat(item.getIndex(), status, reason, user));
    }
    return onSeatList;
  }
}
