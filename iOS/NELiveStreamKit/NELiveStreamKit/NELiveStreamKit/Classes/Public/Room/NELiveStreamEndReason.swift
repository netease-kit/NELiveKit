// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 结束直播间原因
@objc
public enum NELiveStreamEndReason: UInt {
  /// 成员主动离开房间
  case leaveBySelf
  /// 数据同步错误
  case syncDataError
  /// 多端同时加入同一房间被踢
  case kickBySelf
  /// 被管理员踢出房间
  case kickOut
  /// 主播自己关闭了直播间
  case closeByAnchor
  /// 直播间有时限，到时间后强制关闭
  case endOfLife
  /// 收到rtc房间关闭的通知时，关闭此房间
  case allMembersOut
  /// 由后台接口关闭房间
  case closeByBackend
  /// 账号异常
  case loginStateError
  /// RTC断开
  case endOfRtc
  /// 未知异常
  case unknow
  /// 系统错误
  case systemError

  static func convertFromString(reason: String) -> NELiveStreamEndReason {
    switch reason {
    case "LEAVE_BY_SELF": return .leaveBySelf
    case "SYNC_DATA_ERROR": return .syncDataError
    case "KICK_OUT": return .kickOut
    case "CLOSE_BY_ANCHOR": return .closeByAnchor
    case "END_OF_LIFE": return .endOfLife
    case "ALL_MEMBERS_OUT": return .allMembersOut
    case "CLOSE_BY_BACKEND": return .closeByBackend
    case "LOGIN_STATE_ERROR": return .loginStateError
    case "SELF_KICK": return .kickBySelf
    case "SYSTEM_ERROR": return .systemError
    default: return .unknow
    }
  }

  func convertToString() -> String {
    switch self {
    case .leaveBySelf:
      return "LEAVE_BY_SELF"
    case .syncDataError:
      return "SYNC_DATA_ERROR"
    case .kickOut:
      return "KICK_OUT"
    case .kickBySelf:
      return "SELF_KICK"
    case .closeByAnchor:
      return "CLOSE_BY_ANCHOR"
    case .endOfLife:
      return "END_OF_LIFE"
    case .allMembersOut:
      return "ALL_MEMBERS_OUT"
    case .closeByBackend:
      return "CLOSE_BY_BACKEND"
    case .loginStateError:
      return "LOGIN_STATE_ERROR"
    case .systemError:
      return "SYSTEM_ERROR"
    default:
      return "unknown"
    }
  }
}
