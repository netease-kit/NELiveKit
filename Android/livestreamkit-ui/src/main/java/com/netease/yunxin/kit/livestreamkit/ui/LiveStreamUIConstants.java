// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.livestreamkit.ui;

public class LiveStreamUIConstants {
  public static final int COUNT_SEAT = 1 + 3; // 一个主播和3个连麦的观众
  public static final int PAGE_SIZE = 20;
  public static final Long CO_HOST_CONNECT_TIMEOUT = 12L;

  public static class StreamLayout {
    //signal live stream layout
    public static final int SIGNAL_HOST_LIVE_WIDTH = 720;
    public static final int SIGNAL_HOST_LIVE_HEIGHT = 1280;

    //pk live stream layout
    public static final int PK_LIVE_WIDTH = 360;
    public static final int PK_LIVE_HEIGHT = 640;
    public static final float WH_RATIO_PK = PK_LIVE_WIDTH * 2f / PK_LIVE_HEIGHT;
  }
}
