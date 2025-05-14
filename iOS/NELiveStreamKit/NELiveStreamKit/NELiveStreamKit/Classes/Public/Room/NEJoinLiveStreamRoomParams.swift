//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 角色
@objc
public enum NELiveStreamRoomRole: Int {
  /// 房主
  case host = 0

  ///  连麦观众
  case audienceMic

  ///  普通观众
  case audience

  public func toString() -> String {
    switch self {
    case .host: return "host"
    case .audienceMic: return "audience_mic"
    default: return "audience"
    }
  }
}

/// 加入房间参数
@objcMembers
public class NEJoinLiveStreamRoomParams: NSObject {
  /// 房间uid
  public var roomUuid: String = ""
  /// 房间内昵称
  public var nick: String = ""
  /// 直播id
  public var liveRecordId: Int = 0
  /// 角色
  public var role: NELiveStreamRoomRole = .host
  /// 扩展参数
  public var extraData: String?
  /// 是否是重新加入房间
  public var isRejoin: Bool = false
  /// roomInfo
  public var roomInfo: NELiveStreamRoomInfo?
}
