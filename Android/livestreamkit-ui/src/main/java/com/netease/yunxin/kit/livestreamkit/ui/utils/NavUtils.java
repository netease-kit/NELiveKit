// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.utils;

import android.content.Context;
import android.content.Intent;
import com.faceunity.nama.BeautySettingActivity;
import com.netease.yunxin.kit.entertainment.common.LiveConstants;
import com.netease.yunxin.kit.livestreamkit.api.model.NELiveStreamRoomInfo;
import com.netease.yunxin.kit.livestreamkit.ui.activity.*;

public class NavUtils {

  private static final String TAG = "NavUtil";

  public static void toLiveAnchorPage(
      Context context, String username, String avatar, int configId) {
    Intent intent = new Intent(context, LiveStreamAnchorActivity.class);
    intent.putExtra(LiveConstants.INTENT_USER_NAME, username);
    intent.putExtra(LiveConstants.INTENT_USER_AVATAR, avatar);
    intent.putExtra(LiveConstants.INTENT_KEY_CONFIG_ID, configId);
    context.startActivity(intent);
  }

  public static void toLiveAudiencePage(
      Context context, String username, String avatar, NELiveStreamRoomInfo roomInfo) {
    Intent intent = new Intent(context, LiveStreamAudienceActivity.class);
    intent.putExtra(LiveConstants.INTENT_USER_NAME, username);
    intent.putExtra(LiveConstants.INTENT_USER_AVATAR, avatar);
    intent.putExtra(LiveConstants.INTENT_LIVE_INFO, roomInfo);
    context.startActivity(intent);
  }

  public static void toAuthenticatePage(Context context) {
    Intent intent = new Intent(context, AuthenticateActivity.class);
    context.startActivity(intent);
  }

  public static void toBeautySettingPage(Context context) {
    Intent intent = new Intent(context, BeautySettingActivity.class);
    intent.putExtra(BeautySettingActivity.INTENT_KEY_APP_KEY, "");
    context.startActivity(intent);
  }
}
