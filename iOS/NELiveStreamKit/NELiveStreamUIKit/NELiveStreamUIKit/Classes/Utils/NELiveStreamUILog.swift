// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECoreKit

@objcMembers
public class NELiveStreamUILog: NSObject {
  private static var log: XKitLog?

//    public static let liveStreamUILog = "NELiveStreamUILog"

  public class func setUp(_ appKey: String) {
    let options = XKitLogOptions()
    options.level = XKitLogLevelInfo
    options.moduleName = "LiveStreamUI"
    options.sensitives = [appKey]
    log = XKitLog.setUp(options)
  }

  /// API类型日志
  public class func apiLog(_ className: String, desc: String) {
    log?.apiLog(className, desc: "🚰 \(desc)")
  }

  /// Info类型日志
  public class func infoLog(_ className: String, desc: String) {
    log?.infoLog(className, desc: "⚠️ \(desc)")
  }

  /// 警告类型日志
  public class func warnLog(_ className: String, desc: String) {
    log?.warn(className, desc: "❗️ \(desc)")
  }

  /// 成功类型日志
  public class func successLog(_ className: String, desc: String) {
    log?.infoLog(className, desc: "✅ \(desc)")
  }

  /// 错误类型日志
  public class func errorLog(_ className: String, desc: String) {
    log?.errorLog(className, desc: "❌ \(desc)")
  }

  /// 消息类型日志
  public class func messageLog(_ className: String, desc: String) {
    log?.infoLog(className, desc: "✉️ \(desc)")
  }

  /// 网络类型日志
  public class func networkLog(_ className: String, desc: String) {
    log?.infoLog(className, desc: "📶 \(desc)")
  }
}
