//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 角色
@objc
public enum NELiveStreamRoomRole: Int {
  ///  主播（房主）
  case host = 0

  ///  连麦观众
  case audienceOnSeat

  ///  普通观众
  case audience

  ///  PK 主播
  case invitedHost

  public func toString() -> String {
    switch self {
    case .host: return "host"
    case .audienceOnSeat: return "audience"
    case .audience: return NERoomBuiltinRole.Observer
    case .invitedHost: return "invited_host"
    default: return "audience"
    }
  }
}

/// 加入房间参数
@objcMembers
public class NEJoinLiveStreamRoomParams: NSObject {
  /// roomInfo
  public var roomInfo: NELiveStreamRoomInfo?
  /// 房间内昵称
  public var nick: String = ""
  /// 角色
  public var role: NELiveStreamRoomRole = .host
  /// 扩展参数
  public var extraData: String?
}
