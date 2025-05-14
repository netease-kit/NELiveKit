// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.*;
import com.netease.yunxin.kit.common.image.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.utils.LiveStreamUtils;
import com.netease.yunxin.kit.livestreamkit.ui.view.*;
import java.util.*;

public class AudienceLinkSeatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int VIEW_TYPE_ITEM = 1;
  private static final int VIEW_TYPE_EMPTY = 0;
  private final Context context;
  private final List<SeatView.SeatInfo> seatItems = new ArrayList<>();
  private OnAudienceItemClickListener onItemClickListener;

  public AudienceLinkSeatListAdapter(Context context) {
    this.context = context;
  }

  /**
   * 更新数据
   *
   * @param applySeatList 数据
   */
  public void setDataList(List<SeatView.SeatInfo> applySeatList) {
    seatItems.clear();
    if (applySeatList != null && !applySeatList.isEmpty()) {
      seatItems.addAll(applySeatList);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_EMPTY) {
      View emptyView =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.live_apply_seat_list_empty_layout, parent, false);
      return new RecyclerView.ViewHolder(emptyView) {};
    }
    View rootView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.live_audience_apply_seat_list_item_layout, parent, false);
    return new LiveItemHolder(rootView);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof LiveItemHolder) {
      SeatView.SeatInfo seatItem = seatItems.get(position);
      ((LiveItemHolder) holder).tvAnchorName.setText(seatItem.nickname);
      ImageLoader.with(context.getApplicationContext())
          .circleLoad(seatItem.avatar, ((LiveItemHolder) holder).ivAnchor);
      ((LiveItemHolder) holder).tvPosition.setText(String.format("%d", (position + 1)));
      if (LiveStreamUtils.isMySelf(seatItem.uuid)) {
        ((LiveItemHolder) holder).tvCancelOnSeat.setVisibility(View.VISIBLE);
        ((LiveItemHolder) holder)
            .tvCancelOnSeat.setOnClickListener(v -> onItemClickListener.onCancelClick(seatItem));
      } else {
        ((LiveItemHolder) holder).tvCancelOnSeat.setVisibility(View.GONE);
      }
    }
  }

  @Override
  public int getItemCount() {
    if (!seatItems.isEmpty()) {
      return seatItems.size();
    } else {
      return 1;
    }
  }

  @Override
  public int getItemViewType(int position) {
    // 在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
    if (seatItems.isEmpty()) {
      return VIEW_TYPE_EMPTY;
    } else {
      return VIEW_TYPE_ITEM;
    }
    // 如果有数据，则使用ITEM的布局
  }

  public void setOnItemClickListener(OnAudienceItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public interface OnAudienceItemClickListener {
    void onCancelClick(SeatView.SeatInfo seatItem);
  }

  static class LiveItemHolder extends RecyclerView.ViewHolder {
    public TextView tvPosition;
    public ImageView ivAnchor;
    public TextView tvAnchorName;
    public TextView tvCancelOnSeat;

    public LiveItemHolder(@NonNull View itemView) {
      super(itemView);
      tvPosition = itemView.findViewById(R.id.tv_position);
      ivAnchor = itemView.findViewById(R.id.iv_anchor);
      tvAnchorName = itemView.findViewById(R.id.tv_anchor_name);
      tvCancelOnSeat = itemView.findViewById(R.id.tv_cancel_on_seat);
    }
  }
}
