// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.livestreamkit.ui.beauty;

import com.netease.yunxin.kit.roomkit.api.NERoomVideoFrame;

public interface IBeautyModule {
  void init();

  void release();

  void onFrameAvailable(NERoomVideoFrame frame);

  void onCameraChanged(boolean isFrontCamera);

  void onSensorChanged(float x, float y);

  boolean isInitialized();
}
