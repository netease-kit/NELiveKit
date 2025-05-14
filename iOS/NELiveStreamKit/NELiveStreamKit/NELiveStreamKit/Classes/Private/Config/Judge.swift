//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

enum Judge {
  /// 前置条件判断
  static func preCondition<T: Any>(_ success: @escaping () -> Void,
                                   failure: NELiveStreamCallback<T>? = nil) {
    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Uninitialized.")
      failure?(NELiveStreamErrorCode.failed, "Uninitialized.", nil)
      return
    }
    guard let _ = NELiveStreamKit.getInstance().roomContext else {
      NELiveStreamLog.errorLog(kitTag, desc: "RoomContext not exist.")
      failure?(NELiveStreamErrorCode.failed, "RoomContext not exist.", nil)
      return
    }
    success()
  }

  /// 初始化判断条件
  static func initCondition<T: Any>(_ success: @escaping () -> Void,
                                    failure: NELiveStreamCallback<T>? = nil) {
    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Uninitialized.")
      failure?(NELiveStreamErrorCode.failed, "Uninitialized.", nil)
      return
    }
    success()
  }

  /// 同步返回
  @discardableResult

  static func syncCondition(_ success: @escaping () -> Int) -> Int {
    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Uninitialized.")
      return NELiveStreamErrorCode.failed
    }
    guard let _ = NELiveStreamKit.getInstance().roomContext else {
      NELiveStreamLog.errorLog(kitTag, desc: "RoomContext is nil.")
      return NELiveStreamErrorCode.failed
    }
    return success()
  }

  static func condition(_ success: @escaping () -> Void) {
    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Uninitialized.")
      return
    }
    guard let _ = NELiveStreamKit.getInstance().roomContext else {
      NELiveStreamLog.errorLog(kitTag, desc: "RoomContext is nil.")
      return
    }
    success()
  }

  static func syncResult<T: Any>(_ success: @escaping () -> T) -> T? {
    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Uninitialized.")
      return nil
    }
    guard let _ = NELiveStreamKit.getInstance().roomContext else {
      NELiveStreamLog.errorLog(kitTag, desc: "RoomContext is nil.")
      return nil
    }
    return success()
  }
}
