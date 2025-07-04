// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.coaudience.dialog;

import com.netease.yunxin.kit.common.utils.*;
import com.netease.yunxin.kit.livestreamkit.ui.R;
import com.netease.yunxin.kit.livestreamkit.ui.dialog.*;

public class AudienceLinkSeatInvitedDialog extends TimeoutDialog {
  public AudienceLinkSeatInvitedDialog(String anchorName, OnActionListener listener) {
    super(
        String.format(
            XKitUtils.getApplicationContext()
                .getString(R.string.live_anchor_invite_audience_join_seats),
            anchorName),
        listener);
  }
}
