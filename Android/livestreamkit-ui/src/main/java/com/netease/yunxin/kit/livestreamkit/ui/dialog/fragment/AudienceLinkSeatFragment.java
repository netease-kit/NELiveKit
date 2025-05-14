// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.databinding.*;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.*;
import com.netease.yunxin.kit.livestreamkit.ui.utils.LiveStreamUtils;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import java.util.List;

public class AudienceLinkSeatFragment extends BaseLinkSeatFragment {
  private static final String TAG = "AudienceLinkSeatFragment";
  private LiveApplySeatDialogLayoutBinding binding;
  private AudienceLinkSeatListAdapter seatListAdapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = LiveApplySeatDialogLayoutBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView();
    initData();
  }

  private void initView() {
    binding.rcvAnchor.setLayoutManager(new LinearLayoutManager(getContext()));
    seatListAdapter = new AudienceLinkSeatListAdapter(getContext());
    seatListAdapter.setOnItemClickListener(
        seatItem -> {
          cancelApplySeat();
        });
    binding.rcvAnchor.setAdapter(seatListAdapter);
    binding.tvApplySeat.setOnClickListener(v -> submitSeatRequest());
  }

  private void initData() {
    initSeatRequestList();
  }

  @Override
  public void onLocalSeatRequest() {
    binding.tvApplySeat.setVisibility(View.GONE);
    binding.clSelfState.setVisibility(View.VISIBLE);
    binding.tvWaitingTip.setText(R.string.live_waiting_confirm);
    binding.btnCancel.setText(R.string.live_cancel_apply);
    binding.btnCancel.setOnClickListener(v -> cancelApplySeat());
    refreshSeatRequestList2UI();
  }

  @Override
  public void onRemoteSeatRequest(@NonNull String account) {
    refreshSeatRequestList2UI();
  }

  @Override
  public void onLocalSeatRequestCanceled() {
    binding.tvApplySeat.setVisibility(View.VISIBLE);
    binding.clSelfState.setVisibility(View.GONE);
    refreshSeatRequestList2UI();
  }

  @Override
  public void onLocalSeatLinked() {
    binding.tvApplySeat.setVisibility(View.GONE);
    binding.clSelfState.setVisibility(View.VISIBLE);
    binding.tvWaitingTip.setText(R.string.live_seat_linked);
    binding.btnCancel.setText(R.string.live_unlink_seat);
    binding.btnCancel.setOnClickListener(v -> leaveSeat());
    refreshSeatRequestList2UI();
  }

  @Override
  protected void onRemoteSeatLinked(String account) {
    refreshSeatRequestList2UI();
  }

  @Override
  public void onLocalSeatUnlinked() {
    binding.tvApplySeat.setVisibility(View.VISIBLE);
    binding.clSelfState.setVisibility(View.GONE);
    refreshSeatRequestList2UI();
  }

  @Override
  protected void onRemoteSeatUnlinked(String account) {
    refreshSeatRequestList2UI();
  }

  @Override
  protected void onRemoteSeatRequestCanceled(String account) {
    refreshSeatRequestList2UI();
  }

  private void initSeatRequestList() {

    getOnSeatInfo(
        new NELiveStreamCallback<List<SeatView.SeatInfo>>() {
          @Override
          public void onSuccess(@Nullable List<SeatView.SeatInfo> seatInfos) {
            if (LiveStreamUtils.isCurrentOnSeat(seatInfos)) {
              onLocalSeatLinked();
            }
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {}
        });

    getSeatRequestList(
        new NELiveStreamCallback<List<SeatView.SeatInfo>>() {
          @Override
          public void onSuccess(@Nullable List<SeatView.SeatInfo> applyLinkSeatModels) {
            if (seatListAdapter != null) {
              seatListAdapter.setDataList(applyLinkSeatModels);
            }
            if (applyLinkSeatModels != null) {
              for (SeatView.SeatInfo item : applyLinkSeatModels) {
                if (TextUtils.equals(item.uuid, LiveStreamUtils.getLocalAccount())) {
                  onLocalSeatRequest();
                }
              }
            }
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {}
        });
  }

  private void refreshSeatRequestList2UI() {
    getSeatRequestList(
        new NELiveStreamCallback<List<SeatView.SeatInfo>>() {
          @Override
          public void onSuccess(@Nullable List<SeatView.SeatInfo> applyLinkSeatModels) {
            if (seatListAdapter != null) {
              seatListAdapter.setDataList(applyLinkSeatModels);
            }
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {}
        });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
