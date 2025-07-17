// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.coaudience.fragment;

import android.os.*;
import androidx.annotation.*;
import androidx.fragment.app.*;
import com.netease.yunxin.kit.common.ui.utils.*;
import com.netease.yunxin.kit.entertainment.common.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.roomkit.api.model.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.*;
import kotlin.*;

public class BaseLinkSeatFragment extends Fragment {
  private static final String TAG = "BaseLinkSeatFragment";

  protected NELiveStreamListener streamListener =
      new NELiveStreamListener() {
        @Override
        public void onSeatRequestSubmitted(@NonNull NESeatRequestItem requestItem) {
          LiveRoomLog.i(TAG, "onSeatRequestSubmitted requestItem = " + requestItem);
          if (LiveStreamUtils.isMySelf(requestItem.getUser())) {
            onLocalSeatRequest();
          } else {
            onRemoteSeatRequest(requestItem.getUser());
          }
        }

        @Override
        public void onSeatRequestCancelled(@NonNull NESeatRequestItem requestItem) {
          LiveRoomLog.i(TAG, "onSeatRequestCancelled requestItem = " + requestItem);
          if (LiveStreamUtils.isMySelf(requestItem.getUser())) {
            onLocalSeatRequestCanceled();
          } else {
            onRemoteSeatRequestCanceled(requestItem.getUser());
          }
        }

        @Override
        public void onSeatRequestApproved(
            @NonNull NESeatRequestItem requestItem,
            @NonNull NERoomUser operateByUser,
            boolean isAutoAgree) {
          LiveRoomLog.i(TAG, "onSeatRequestApproved requestItem = " + requestItem);
          if (LiveStreamUtils.isMySelf(requestItem.getUser())) {
            onLocalSeatLinked();
          } else {
            onRemoteSeatLinked(requestItem.getUser());
          }
        }

        @Override
        public void onSeatRequestRejected(
            @NonNull NESeatRequestItem requestItem, @NonNull NERoomUser operateBy) {
          LiveRoomLog.i(TAG, "onSeatRequestRejected requestItem = " + requestItem);
          if (LiveStreamUtils.isMySelf(requestItem.getUser())) {
            onLocalSeatUnlinked();
          } else {
            onRemoteSeatUnlinked(requestItem.getUser());
          }
        }

        @Override
        public void onSeatInvitationRejected(@NonNull NESeatInvitationItem invitationItem) {
          LiveRoomLog.i(TAG, "onSeatInvitationReceived invitationItem = " + invitationItem);
          if (LiveStreamUtils.isMySelf(invitationItem.getUser())) {
            onLocalSeatInvitationRejected(invitationItem);
          } else {
            onRemoteSeatInvitationRejected(invitationItem);
          }
        }

        @Override
        public void onSeatInvitationAccepted(
            @NonNull NESeatInvitationItem invitationItem, boolean isAutoAgree) {
          LiveRoomLog.i(TAG, "onSeatInvitationAccepted invitationItem = " + invitationItem);
          if (LiveStreamUtils.isMySelf(invitationItem.getUser())) {
            onLocalSeatLinked();
          } else {
            onRemoteSeatLinked(invitationItem.getUser());
          }
        }

        @Override
        public void onSeatLeave(@NonNull NESeatItem seatItem) {
          LiveRoomLog.i(TAG, "onSeatLeave seatItem = " + seatItem);
          if (LiveStreamUtils.isMySelf(seatItem.getUser())) {
            onLocalSeatUnlinked();
          } else {
            onRemoteSeatUnlinked(seatItem.getUser());
          }
        }

        @Override
        public void onSeatKicked(@NonNull NESeatItem seatItem, @NonNull NERoomUser operateBy) {
          LiveRoomLog.i(TAG, "onSeatKicked seatItem = " + seatItem);
          if (LiveStreamUtils.isMySelf(seatItem.getUser())) {
            onLocalSeatUnlinked();
          } else {
            onRemoteSeatUnlinked(seatItem.getUser());
          }
        }

        @Override
        public void onSeatListChanged(@NonNull List<NESeatItem> seatItems) {
          LiveRoomLog.i(TAG, "onSeatListChanged seatItems = " + seatItems);
        }
      };

  protected void onLocalSeatRequest() {}

  protected void onRemoteSeatRequest(String account) {}

  protected void onLocalSeatRequestCanceled() {}

  protected void onRemoteSeatRequestCanceled(String account) {}

  protected void onLocalSeatLinked() {}

  protected void onRemoteSeatLinked(String account) {}

  protected void onLocalSeatUnlinked() {}

  protected void onRemoteSeatUnlinked(String account) {}

  protected void onLocalSeatInvitationRejected(NESeatInvitationItem item) {}

  protected void onRemoteSeatInvitationRejected(NESeatInvitationItem item) {}

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NELiveStreamKit.getInstance().addLiveStreamListener(streamListener);
  }

  @Override
  public void onDestroy() {
    NELiveStreamKit.getInstance().removeLiveStreamListener(streamListener);
    super.onDestroy();
  }

  protected void getSeatRequestList(NELiveStreamCallback<List<SeatView.SeatInfo>> callback) {
    NELiveStreamKit.getInstance()
        .getSeatRequestList(
            new NELiveStreamCallback<List<NELiveRoomSeatRequestItem>>() {
              @Override
              public void onSuccess(@Nullable List<NELiveRoomSeatRequestItem> seatRequestItems) {
                LiveRoomLog.i(
                    TAG, "getSeatRequestList success seatRequestItems = " + seatRequestItems);
                List<SeatView.SeatInfo> applySeatList = new ArrayList<>();
                if (seatRequestItems != null) {
                  for (NELiveRoomSeatRequestItem item : seatRequestItems) {
                    if (item != null) {
                      applySeatList.add(
                          new SeatView.SeatInfo(
                              item.getUser(), item.getUserName(), item.getIcon()));
                    }
                  }
                }

                if (callback != null) {
                  callback.onSuccess(applySeatList);
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "getSeatRequestList failed msg = " + msg);
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });
  }

  protected void getOnSeatInfo(NELiveStreamCallback<List<SeatView.SeatInfo>> callback) {
    NELiveStreamKit.getInstance()
        .getSeatInfo(
            new NELiveStreamCallback<NESeatInfo>() {
              @Override
              public void onSuccess(@Nullable NESeatInfo neVoiceRoomSeatInfo) {
                LiveRoomLog.i(
                    TAG, "getSeatInfo success neVoiceRoomSeatInfo = " + neVoiceRoomSeatInfo);
                if (callback != null && neVoiceRoomSeatInfo != null) {
                  callback.onSuccess(
                      SeatUtils.transNESeatItems2OnSeatInfos(neVoiceRoomSeatInfo.getSeatItems()));
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "getSeatInfo failed msg = " + msg);
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });
  }

  protected void submitSeatRequest() {
    NELiveStreamKit.getInstance()
        .submitSeatRequest(
            new NELiveStreamCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                LiveRoomLog.i(TAG, "submitSeatRequest success");
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "submitSeatRequest failed msg = " + msg);
                ToastX.showShortToast(msg == null ? getString(R.string.network_error) : msg);
              }
            });
  }

  protected void cancelApplySeat() {
    NELiveStreamKit.getInstance()
        .cancelSeatRequest(
            new NELiveStreamCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                LiveRoomLog.i(TAG, "cancelSeatRequest success");
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "cancelSeatRequest failed msg = " + msg);
                ToastX.showShortToast(msg == null ? getString(R.string.network_error) : msg);
              }
            });
  }

  protected void leaveSeat() {
    NELiveStreamKit.getInstance()
        .leaveSeat(
            new NELiveStreamCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                LiveRoomLog.i(TAG, "leaveSeat success");

                NELiveStreamKit.getInstance()
                    .changeMemberRole(
                        NELiveStreamKit.getInstance().getLocalMember().getUuid(),
                        NELiveRoomRole.AUDIENCE_OBSERVER.getValue(),
                        new NELiveStreamCallback<Unit>() {
                          @Override
                          public void onSuccess(@Nullable Unit data) {
                            LiveRoomLog.i(TAG, "changeLocalMemberRole success");
                          }

                          @Override
                          public void onFailure(int code, @Nullable String msg) {
                            LiveRoomLog.i(TAG, "changeLocalMemberRole failed");
                          }
                        });
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "leaveSeat failed msg = " + msg);
                ToastX.showShortToast(msg == null ? getString(R.string.network_error) : msg);
              }
            });
  }
}
