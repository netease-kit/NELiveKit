// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.livestreamkit.activity;

import android.view.View;
import com.netease.yunxin.app.livestreamkit.databinding.ActivitySettingBinding;
import com.netease.yunxin.kit.entertainment.common.activity.BasePartyActivity;
import com.netease.yunxin.kit.entertainment.common.utils.CommonNavUtils;

/** setting page */
public class CommonSettingActivity extends BasePartyActivity {
  private ActivitySettingBinding binding;

  @Override
  protected void init() {
    paddingStatusBarHeight(binding.getRoot());
    handlePSTNTips();
    initEvent();
  }

  @Override
  protected View getRootView() {
    binding = ActivitySettingBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  private void handlePSTNTips() {}

  private void initEvent() {
    binding.itemViewPrivacyPolicy.setOnClickListener(
        v -> CommonNavUtils.toPrivacyPolicyPage(CommonSettingActivity.this));
    binding.itemViewUserAgreement.setOnClickListener(
        v -> CommonNavUtils.toUserPolicePage(CommonSettingActivity.this));
  }
}
