// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.entertainment.common.LiveConstants;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.entertainment.common.databinding.ActivityRoomListBinding;
import com.netease.yunxin.kit.entertainment.common.smartrefresh.api.RefreshLayout;
import com.netease.yunxin.kit.entertainment.common.smartrefresh.listener.OnLoadMoreListener;
import com.netease.yunxin.kit.entertainment.common.smartrefresh.listener.OnRefreshListener;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.OneOnOneUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;
import com.netease.yunxin.kit.entertainment.common.utils.VoiceRoomUtils;
import com.netease.yunxin.kit.entertainment.common.widget.FooterView;
import com.netease.yunxin.kit.entertainment.common.widget.HeaderView;
import com.netease.yunxin.kit.livestreamkit.api.NELiveRoomLiveState;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamCallback;
import com.netease.yunxin.kit.livestreamkit.api.NELiveStreamKit;
import com.netease.yunxin.kit.livestreamkit.api.NELiveType;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomInfo;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveRoomList;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.adapter.LiveStreamListAdapter;
import com.netease.yunxin.kit.livestreamkit.ui.utils.NavUtils;
import kotlin.Unit;

public class LiveStreamRoomListActivity extends BaseActivity
    implements OnRefreshListener, OnLoadMoreListener {
  private static final String TAG = "page_live_stream_list";

  public static final int ROOM_MAX_AUDIENCE_COUNT = 1;
  protected ActivityRoomListBinding binding;
  protected int pageNum = 1;
  public static final int PAGE_SIZE = 20;
  public static final int SPAN_COUNT = 2;
  protected int tempPageNum = 1;
  protected LiveStreamListAdapter adapter;
  private GridLayoutManager layoutManager;
  protected boolean isOversea = false;
  protected int configId;

  protected String userName;

  protected String avatar;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRoomListBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    paddingStatusBarHeight(binding.getRoot());
    isOversea = getIntent().getBooleanExtra(LiveConstants.INTENT_IS_OVERSEA, false);
    configId = getIntent().getIntExtra(LiveConstants.INTENT_KEY_CONFIG_ID, 0);
    userName = getIntent().getStringExtra(LiveConstants.INTENT_USER_NAME);
    avatar = getIntent().getStringExtra(LiveConstants.INTENT_USER_AVATAR);
    init();
    setEvent();
    binding.tvTitle.setText(getString(R.string.live_room));
    binding.tvStart.setText(getString(R.string.voiceroom_start_live));
  }

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  private void init() {
    adapter = getRoomListAdapter();
    layoutManager = new GridLayoutManager(this, SPAN_COUNT);
    layoutManager.setSpanSizeLookup(new LiveStreamRoomListActivity.MySpanSizeLookup());
    binding.rvRoomList.setAdapter(adapter);
    binding.rvRoomList.addItemDecoration(new LiveStreamRoomListActivity.MyItemDecoration());
    binding.rvRoomList.setLayoutManager(layoutManager);
  }

  @Override
  protected void onResume() {
    super.onResume();
    refresh();
  }

  @Override
  public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
    loadMore();
  }

  @Override
  public void onRefresh(@NonNull RefreshLayout refreshLayout) {
    refresh();
  }

  static class MyItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int pixel8 = SizeUtils.dp2px(8f);
      int pixel4 = SizeUtils.dp2px(4f);
      int position = parent.getChildAdapterPosition(view);
      int left;
      int right;
      if (position % 2 == 0) {
        left = pixel8;
        right = pixel4;
      } else {
        left = pixel4;
        right = pixel8;
      }
      outRect.set(left, pixel4, right, pixel4);
    }
  }

  class MySpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    @Override
    public int getSpanSize(int position) {
      // 如果是空布局，让它占满一行
      if (adapter.isEmptyPosition(position)) {
        return layoutManager.getSpanCount();
      } else {
        return 1;
      }
    }
  }

  protected void setEvent() {
    binding.ivBack.setOnClickListener(v -> finish());
    binding.refreshLayout.setRefreshHeader(new HeaderView(this));
    binding.refreshLayout.setRefreshFooter(new FooterView(this));
    binding.refreshLayout.setOnRefreshListener(this);
    binding.refreshLayout.setOnLoadMoreListener(this);
    binding.ivCreateRoom.setOnClickListener(
        v -> {
          ReportUtils.report(LiveStreamRoomListActivity.this, TAG, "live_stream_start_live");
          NavUtils.toLiveAnchorPage(LiveStreamRoomListActivity.this, userName, avatar, configId);
        });
    adapter.setItemOnClickListener(
        info -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          if (NetworkUtils.isConnected()) {
            handleJoinLiveRoom(info);
          } else {
            ToastUtils.INSTANCE.showShortToast(
                LiveStreamRoomListActivity.this,
                getString(
                    com.netease.yunxin.kit.entertainment.common.R.string.common_network_error));
          }
        });
  }

  protected LiveStreamListAdapter getRoomListAdapter() {
    return new LiveStreamListAdapter(LiveStreamRoomListActivity.this);
  }

  protected void refresh() {
    NELiveStreamKit.getInstance()
        .fetchLiveRoomList(
            NELiveRoomLiveState.Live,
            NELiveType.LIVE_INTERACTION,
            tempPageNum,
            PAGE_SIZE,
            new NELiveStreamCallback<NELiveRoomList>() {
              @Override
              public void onSuccess(@Nullable NELiveRoomList neLiveRoomList) {
                pageNum = tempPageNum;
                if (neLiveRoomList == null
                    || neLiveRoomList.getList() == null
                    || neLiveRoomList.getList().isEmpty()) {
                  binding.emptyView.setVisibility(View.VISIBLE);
                  binding.rvRoomList.setVisibility(View.GONE);
                } else {
                  binding.emptyView.setVisibility(View.GONE);
                  binding.rvRoomList.setVisibility(View.VISIBLE);
                  adapter.refreshList(neLiveRoomList.getList());
                }
                binding.refreshLayout.finishRefresh(true);
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                tempPageNum = pageNum;
                binding.refreshLayout.finishRefresh(false);
                ToastUtils.INSTANCE.showShortToast(
                    LiveStreamRoomListActivity.this, getString(R.string.network_error));
              }
            });
  }

  protected void loadMore() {
    NELiveStreamKit.getInstance()
        .fetchLiveRoomList(
            NELiveRoomLiveState.Live,
            NELiveType.LIVE_TYPE_TOGETHER_LISTEN,
            tempPageNum,
            PAGE_SIZE,
            new NELiveStreamCallback<NELiveRoomList>() {
              @Override
              public void onSuccess(@Nullable NELiveRoomList neLiveRoomList) {
                pageNum = tempPageNum;
                if (neLiveRoomList != null && neLiveRoomList.getList() != null) {
                  adapter.loadMore(neLiveRoomList.getList());
                }
                binding.refreshLayout.finishLoadMore(true);
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                tempPageNum = pageNum;
                binding.refreshLayout.finishLoadMore(false);
              }
            });
  }

  private void handleJoinLiveRoom(NELiveRoomInfo info) {
    if (VoiceRoomUtils.isShowFloatView()) {
      AlertDialog.Builder builder = new AlertDialog.Builder(LiveStreamRoomListActivity.this);
      builder.setTitle(getString(R.string.voiceroom_tip));
      builder.setMessage(getString(R.string.click_roomlist_tips));
      builder.setCancelable(true);
      builder.setPositiveButton(
          getString(R.string.voiceroom_sure),
          (dialog, which) -> {
            NELiveStreamKit.getInstance()
                .leaveRoom(
                    new NELiveStreamCallback<Unit>() {
                      @Override
                      public void onSuccess(@Nullable Unit unit) {
                        joinLiveRoom(info);
                      }

                      @Override
                      public void onFailure(int code, @Nullable String msg) {}
                    });
            dialog.dismiss();
          });
      builder.setNegativeButton(
          getString(R.string.voiceroom_cancel), (dialog, which) -> dialog.dismiss());
      AlertDialog alertDialog = builder.create();
      alertDialog.show();
    } else if (OneOnOneUtils.isInTheCall()) {
      ToastX.showShortToast(getString(R.string.ec_in_the_call_tips));
    } else {
      joinLiveRoom(info);
    }
  }

  private void joinLiveRoom(NELiveRoomInfo info) {
    NavUtils.toLiveAudiencePage(LiveStreamRoomListActivity.this, userName, avatar, info);
  }
}
