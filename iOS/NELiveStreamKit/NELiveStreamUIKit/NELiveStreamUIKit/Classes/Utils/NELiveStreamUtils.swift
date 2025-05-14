//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import AVFoundation
import Foundation

class NELiveStreamUtils {
  /// 获取麦克风权限状态
  /// - Parameters:
  ///   - mediaType: 媒体类型，默认使用音频类型
  ///   - completion: 异步返回授权结果的闭包（主线程回调）
  static func getMeidaPermissions(mediaType: AVMediaType = .audio,
                                  completion: ((Bool) -> Void)?) {
    let authStatus = AVCaptureDevice.authorizationStatus(for: mediaType)

    switch authStatus {
    case .notDetermined:
      // 未决定时发起权限请求
      AVCaptureDevice.requestAccess(for: mediaType) { granted in
        completion?(granted)
      }

    case .authorized:
      // 已授权
      completion?(true)

    default:
      // 其他状态均视为未授权
      completion?(false)
    }
  }
}
