// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.livestreamkit.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.app.livestreamkit.R;
import com.netease.yunxin.app.livestreamkit.databinding.FragmentAppEntranceBinding;
import com.netease.yunxin.app.livestreamkit.utils.NavUtils;
import com.netease.yunxin.kit.entertainment.common.adapter.FunctionAdapter;
import com.netease.yunxin.kit.entertainment.common.fragment.BaseFragment;
import java.util.ArrayList;
import java.util.List;

public class AppEntranceFragment extends BaseFragment {

  private FragmentAppEntranceBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentAppEntranceBinding.inflate(inflater, container, false);
    View rootView = binding.getRoot();
    paddingStatusBarHeight(rootView);
    initView();
    initListener();
    return rootView;
  }

  private void initView() {
    binding.ivTopLogo.setImageResource(R.drawable.icon_app_top_logo);
    binding.rvFunctionList.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    List<FunctionAdapter.FunctionItem> list = new ArrayList<>();
    list.add(
        new FunctionAdapter.FunctionItem(
            getString(R.string.live_room),
            getString(R.string.live_room_desc),
            R.drawable.home_item_bg_live_room,
            R.raw.home_live,
            () -> {
              NavUtils.toLiveStreamRoomListPage(getContext());
            }));
    binding.rvFunctionList.setAdapter(new FunctionAdapter(getContext(), list));
  }

  private void initListener() {}

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
