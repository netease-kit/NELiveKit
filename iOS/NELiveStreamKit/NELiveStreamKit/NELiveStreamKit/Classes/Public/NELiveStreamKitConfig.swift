// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers
public class NELiveStreamKitConfig: NSObject {
  /// 应用 AppKey
  public var appKey: String

  /// API 服务器地址
  public var apiURL: String?

  /// 额外配置信息
  public var extras: [String: String]

  /// 推送证书
  public var APNSCerName: String = ""

  public init(appKey: String) {
    self.appKey = appKey
    extras = [:]
    super.init()
  }
}
