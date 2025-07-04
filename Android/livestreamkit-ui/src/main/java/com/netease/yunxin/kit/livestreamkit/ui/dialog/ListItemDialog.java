// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.adapter.LiveBaseAdapter;
import com.netease.yunxin.kit.livestreamkit.ui.view.OnItemClickListener;
import java.util.ArrayList;
import java.util.List;

/** Created by luc on 1/29/21. */
public class ListItemDialog extends BottomBaseDialog {
  private final List<String> items = new ArrayList<>();
  private OnItemClickListener<String> onItemClickListener;

  public ListItemDialog(@NonNull Activity activity) {
    super(activity);
  }

  public ListItemDialog setOnItemClickListener(OnItemClickListener<String> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
    return this;
  }

  @Override
  protected void renderTopView(FrameLayout parent) {}

  @Override
  protected boolean enableTop() {
    return false;
  }

  @Override
  protected void renderContentView(FrameLayout parent) {
    RecyclerView rvMemberList = new RecyclerView(getContext());
    rvMemberList.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
    FrameLayout.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    rvMemberList.setLayoutManager(new LinearLayoutManager(getContext()));
    rvMemberList.setAdapter(
        new LiveBaseAdapter<String>(getContext(), items) {

          @Override
          protected int getLayoutId(int viewType) {
            return R.layout.live_view_item_list_text;
          }

          @Override
          protected LiveViewHolder onCreateViewHolder(View itemView) {
            return new LiveViewHolder(itemView);
          }

          @Override
          protected void onBindViewHolder(@NonNull LiveViewHolder holder, String itemData) {
            super.onBindViewHolder(holder, itemData);
            TextView tvName = holder.getView(R.id.tv_item);
            tvName.setText(itemData);
            holder.itemView.setOnClickListener(
                v -> {
                  if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(itemData);
                  }
                  dismiss();
                });
          }
        });
    parent.addView(rvMemberList, layoutParams);
  }

  public void show(List<String> items) {
    if (items != null && !items.isEmpty()) {
      this.items.addAll(items);
    }
    show();
  }
}
