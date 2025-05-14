// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog.fragment;

import android.os.*;
import androidx.annotation.*;
import androidx.fragment.app.*;
import com.netease.yunxin.kit.common.ui.utils.*;
import com.netease.yunxin.kit.livestreamkit.api.*;
import com.netease.yunxin.kit.livestreamkit.api.model.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import com.netease.yunxin.kit.roomkit.api.service.*;
import java.util.*;
import kotlin.*;

public class BaseLinkSeatFragment extends Fragment {
  private static final String TAG = "BaseLinkSeatFragment";

  protected NELiveStreamListener streamListener =
      new NELiveStreamListener() {
        @Override
        public void onSeatRequestSubmitted(int seatIndex, @NonNull String account) {
          LiveRoomLog.i(TAG, "onSeatRequestSubmitted seatIndex = " + seatIndex);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatRequest();
          } else {
            onRemoteSeatRequest(account);
          }
        }

        @Override
        public void onSeatRequestCancelled(int seatIndex, @NonNull String account) {
          LiveRoomLog.i(TAG, "onSeatRequestCancelled seatIndex = " + seatIndex);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatRequestCanceled();
          } else {
            onRemoteSeatRequestCanceled(account);
          }
        }

        @Override
        public void onSeatRequestApproved(
            int seatIndex,
            @NonNull String account,
            @NonNull String operateBy,
            boolean isAutoAgree) {
          LiveRoomLog.i(TAG, "onSeatRequestApproved seatIndex = " + seatIndex);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatLinked();
          } else {
            onRemoteSeatLinked(account);
          }
        }

        @Override
        public void onSeatInvitationRejected(int seatIndex, @NonNull String account) {
          LiveRoomLog.i(TAG, "onSeatInvitationReceived account = " + account);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatInvitationRejected(seatIndex);
          } else {
            onRemoteSeatInvitationRejected(seatIndex, account);
          }
        }

        @Override
        public void onSeatInvitationAccepted(
            int seatIndex, @NonNull String account, boolean isAutoAgree) {
          LiveRoomLog.i(TAG, "onSeatInvitationAccepted seatIndex = " + seatIndex);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatLinked();
          } else {
            onRemoteSeatLinked(account);
          }
        }

        @Override
        public void onSeatLeave(int seatIndex, @NonNull String account) {
          LiveRoomLog.i(TAG, "onSeatLeave seatIndex = " + seatIndex);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatUnlinked();
          } else {
            onRemoteSeatUnlinked(account);
          }
        }

        @Override
        public void onSeatKicked(
            int seatIndex, @NonNull String account, @NonNull String operateBy) {
          LiveRoomLog.i(TAG, "onSeatKicked seatIndex = " + seatIndex);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatUnlinked();
          } else {
            onRemoteSeatUnlinked(account);
          }
        }

        @Override
        public void onSeatRequestRejected(
            int seatIndex, @NonNull String account, @NonNull String operateBy) {
          LiveRoomLog.i(TAG, "onSeatRequestRejected seatIndex = " + seatIndex);
          if (LiveStreamUtils.isMySelf(account)) {
            onLocalSeatUnlinked();
          } else {
            onRemoteSeatUnlinked(account);
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

  protected void onLocalSeatInvitationRejected(int seatIndex) {}

  protected void onRemoteSeatInvitationRejected(int seatIndex, @NonNull String user) {}

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
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                LiveRoomLog.e(TAG, "leaveSeat failed msg = " + msg);
                ToastX.showShortToast(msg == null ? getString(R.string.network_error) : msg);
              }
            });
  }
}
