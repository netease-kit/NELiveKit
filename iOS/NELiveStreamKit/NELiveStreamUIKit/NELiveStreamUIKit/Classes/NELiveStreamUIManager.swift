// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NELiveStreamKit

/// 登录事件枚举
@objc public enum NELiveStreamClientEvent: Int {
  /// 被踢出登录
  case kickOut
  /// 服务器禁止登录
  case forbidden
  /// 账号或密码错误
  case accountTokenError
  /// 登录成功
  case loggedIn
  /// 未登录
  case loggedOut
  /// 授权错误
  case incorrectToken
  /// Token过期
  case tokenExpired
}

@objc public protocol NELiveStreamUIDelegate: AnyObject {
  func onLiveStreamClientEvent(_ event: NELiveStreamClientEvent)
  func onLiveStreamJoinRoom()
  func onLiveStreamLeaveRoom()
}

@objcMembers
public class NELiveStreamUIManager: NSObject, NELiveStreamAuthListener {
  // MARK: - Properties

  var nickname: String = ""
  var account: String = ""
  var token: String = ""

  public var isLoggedIn: Bool {
    NELiveStreamKit.getInstance().isLoggedIn
  }

  public weak var delegate: NELiveStreamUIDelegate?

  public var config: NELiveStreamKitConfig?

  public var configId: Int = 0

  /// 是否已经在房间内，携带忙碌信息
  public var canContinueAction: (() -> Bool)?

  // MARK: - Singleton

  public static let shared = NELiveStreamUIManager()

  override private init() {
    super.init()
    NELiveStreamKit.getInstance().addAuthListener(self)
  }

  // MARK: - Public Methods

  public func initialize(with config: NELiveStreamKitConfig,
                         configId: Int,
                         callback: @escaping (Int, String?, Any?) -> Void) {
    self.config = config
    self.configId = configId
    NELiveStreamKit.getInstance().initialize(config: config, callback: callback)
    NELiveStreamLog.setUp(config.appKey)
    NELiveStreamUILog.setUp(config.appKey)
    NELiveAudienceViewController.setPlayerLogDir()
  }

  public func login(account: String,
                    token: String,
                    nickname: String,
                    callback: NELiveStreamCallback<Any>?) {
    self.account = account
    self.token = token
    self.nickname = nickname
    NELiveStreamKit.getInstance().login(account, token: token, callback: callback)
  }

  public func logout(callback: NELiveStreamCallback<Any>?) {
    NELiveStreamKit.getInstance().logout(callback: callback)
  }

  // MARK: - NELiveStreamAuthListener

  public func onLiveStreamAuthEvent(_ event: NELiveStreamAuthEvent) {
    delegate?.onLiveStreamClientEvent(NELiveStreamClientEvent(rawValue: event.rawValue) ?? .loggedOut)
  }
}
