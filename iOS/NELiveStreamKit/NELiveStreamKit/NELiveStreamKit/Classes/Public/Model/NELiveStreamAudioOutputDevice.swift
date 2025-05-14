// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 音频输出设备类型
@objc
public enum NELiveStreamAudioOutputDevice: UInt {
  /// 听筒
  case earpiece = 0
  /// 扬声器
  case speakerPhone = 1
  /// 有线耳机
  case wiredHeadset = 2
  /// 蓝牙耳机
  case bluetooth = 3
}
