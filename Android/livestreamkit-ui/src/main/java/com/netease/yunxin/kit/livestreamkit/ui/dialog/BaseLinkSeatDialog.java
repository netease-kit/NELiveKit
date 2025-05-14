// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.dialog;

import android.view.*;
import com.netease.yunxin.kit.common.ui.dialog.*;
import com.netease.yunxin.kit.livestreamkit.impl.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.*;

public abstract class BaseLinkSeatDialog extends BaseBottomDialog {

  @Override
  protected void initParams() {
    Window window = null;
    if (getDialog() != null) {
      window = getDialog().getWindow();
    }
    if (window != null) {
      window.setBackgroundDrawableResource(R.drawable.white_corner_bottom_dialog_bg);
      WindowManager.LayoutParams params = window.getAttributes();
      params.gravity = Gravity.BOTTOM;
      params.width = ViewGroup.LayoutParams.MATCH_PARENT;
      params.height = ScreenUtil.getDisplayHeight() / 2;
      window.setAttributes(params);
    }
    setCancelable(true);
  }
}
