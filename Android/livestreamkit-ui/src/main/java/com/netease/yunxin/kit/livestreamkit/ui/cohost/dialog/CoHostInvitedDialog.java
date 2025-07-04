// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.cohost.dialog;

import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.*;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.*;

public class CoHostInvitedDialog extends TimeoutDialog {
  public CoHostInvitedDialog(String anchorName, OnActionListener listener) {
    super(
        String.format(
            XKitUtils.getApplicationContext().getString(R.string.live_co_host_invited), anchorName),
        listener);
  }
}
