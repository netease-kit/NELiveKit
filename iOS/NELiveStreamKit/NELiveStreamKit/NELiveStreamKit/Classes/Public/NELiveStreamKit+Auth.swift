// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

// MARK: - Auth Related

public extension NELiveStreamKit {
  private static var authListeners: [NELiveStreamAuthListener] = []

  /// 登录
  /// - Parameters:
  ///   - account: 账号
  ///   - token: 令牌
  ///   - callback: 回调
  func login(_ account: String,
             token: String,
             callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Login. Account: \(account). Token: \(token)")

    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Failed to login. Uninitialized.")
      callback?(NELiveStreamErrorCode.failed, "Failed to login. Uninitialized.", nil)
      return
    }

    NERoomKit.shared().authService.login(account: account,
                                         token: token) { code, str, _ in
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully login.")
        // 登陆成功后，headers添加属性
        NE.addHeader([
          "user": account,
          "token": token,
          "appkey": self.config?.appKey ?? "",
        ])
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to login. Code: \(code)")
      }
      callback?(code, str, nil)
    }
  }

  /// 退出登录
  /// - Parameter callback: 回调
  func logout(callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Logout.")

    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Failed to logout. Uninitialized.")
      callback?(NELiveStreamErrorCode.failed, "Failed to logout. Uninitialized.", nil)
      return
    }

    NERoomKit.shared().authService.logout { code, str, _ in
      if code == 0 {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully logout.")
      } else {
        NELiveStreamLog.errorLog(kitTag, desc: "Failed to logout. Code: \(code)")
      }
      callback?(code, str, nil)
    }
  }

  /// 是否登录
  var isLoggedIn: Bool {
    NERoomKit.shared().authService.isLoggedIn
  }

  /// 添加认证监听器
  /// - Parameter listener: 监听器
  func addAuthListener(_ listener: NELiveStreamAuthListener) {
    if !NELiveStreamKit.authListeners.contains(where: { $0 === listener }) {
      NELiveStreamKit.authListeners.append(listener)
    }
  }

  /// 移除认证监听器
  /// - Parameter listener: 监听器
  func removeAuthListener(_ listener: NELiveStreamAuthListener) {
    NELiveStreamKit.authListeners.removeAll(where: { $0 === listener })
  }

  // MARK: - Private Methods

  private func notifyAuthListeners(_ event: NELiveStreamAuthEvent) {
    for listener in NELiveStreamKit.authListeners {
      listener.onLiveStreamAuthEvent(event)
    }
  }
}
