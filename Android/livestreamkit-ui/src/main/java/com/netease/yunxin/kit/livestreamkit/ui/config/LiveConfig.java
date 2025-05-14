// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.config;

public class LiveConfig {
  public static final class Video {
    public static final int DEFAULT_WIDTH = 540;
    public static final int DEFAULT_HEIGHT = 960;
    public static final int DEFAULT_FPS = 15;
    public static final int DEFAULT_BITRATE = 1000;
  }

  public static final class Audio {
    public static final int DEFAULT_VOLUME = 100;
    public static final int DEFAULT_EARBACK_VOLUME = 100;
    public static final int VOLUME_INDICATION_INTERVAL = 200; // ms
  }

  public static final class Room {
    public static final int MAX_SEAT_COUNT = 2;
    public static final int ANCHOR_SEAT_INDEX = 1;
    public static final int AUDIENCE_SEAT_INDEX = 2;
  }

  public static final class Network {
    public static final int RETRY_COUNT = 3;
    public static final int RETRY_DELAY = 1000; // ms
  }

  public static final class UI {
    public static final int KEYBOARD_MIN_HEIGHT = 80; // dp
    public static final int GIFT_ANIMATION_SIZE = 200; // dp
  }
}
