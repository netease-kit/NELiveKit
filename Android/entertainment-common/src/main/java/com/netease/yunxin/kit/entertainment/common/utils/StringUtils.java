// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.entertainment.common.utils;

import com.netease.yunxin.kit.entertainment.common.R;
import java.text.DecimalFormat;

public class StringUtils {

  /**
   * 格式化展示云币数量，超过99999 展示为 99999+
   *
   * @param coinCount 云币总数
   * @return 云币数字符串
   */
  public static String formatCoinCount(long coinCount) {
    if (coinCount <= 99999) {
      return String.valueOf(coinCount);
    }
    return "99999+";
  }

  /**
   * 格式化展示观众数，超过 1w 展示 xxw
   *
   * @param audienceCount 观众实际数
   * @return 观众数字符串
   */
  public static String getAudienceCount(int audienceCount) {
    if (audienceCount < 0) {
      return "0";
    }

    if (audienceCount < 10000) {
      return String.valueOf(audienceCount);
    }
    if (audienceCount < 1000000) {
      DecimalFormat decimalFormat = new DecimalFormat("#.##");
      return decimalFormat.format(Double.valueOf(audienceCount / 10000f))
          + Utils.getApp().getString(R.string.ten_thousand);
    }
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    return decimalFormat.format(Double.valueOf(audienceCount / 1000000f))
        + Utils.getApp().getString(R.string.million);
  }
}
