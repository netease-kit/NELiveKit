// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.fragment.AudienceLinkSeatFragment;

public class AudienceLinkSeatDialog extends BaseLinkSeatDialog {

  @Override
  protected void initData() {
    super.initData();
    AudienceLinkSeatFragment fragment = new AudienceLinkSeatFragment();
    getChildFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commit();
  }

  @Override
  protected @Nullable View getRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    return inflater.inflate(R.layout.live_audience_link_seat_dialog_layout, container, false);
  }
}
