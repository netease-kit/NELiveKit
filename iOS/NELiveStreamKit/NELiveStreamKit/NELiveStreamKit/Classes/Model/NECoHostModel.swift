// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 连麦请求消息模型

@objcMembers
class _NECoHostRequestConnectionResponse: NSObject, Codable {
  var connectionGroupId: String = ""
  var inviter: NECoHostUserInfo?
  var inviteeList: [NECoHostUserInfo] = []
  var status: Int = 0
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, inviter, inviteeList, status, ext
  }
}

@objcMembers
class NECoHostUserInfo: NSObject, Codable {
  var roomUuid: String = ""
  var icon: String = "" // 头像
  var userName: String = ""
  var userUuid: String = "" // 兼容部分接口
  var joinedTime: TimeInterval? = nil

  private enum CodingKeys: String, CodingKey {
    case roomUuid, icon, userName, userUuid, joinedTime
  }
}

@objcMembers
class NECoHostConnectionRequestMsg: NSObject, Codable {
  var connectionGroupId: String = ""
  var inviter: NECoHostUserInfo?
  var inviteeList: [NECoHostUserInfo] = []
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, inviter, inviteeList, ext
  }
}

@objcMembers
class NECoHostConnectionAcceptedMsg: NSObject, Codable {
  var connectionGroupId: String = ""
  var inviter: NECoHostUserInfo?
  var inviteeList: [NECoHostUserInfo] = []
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, inviter, inviteeList, ext
  }
}

@objcMembers
class NECoHostConnectionRejectedMsg: NSObject, Codable {
  var connectionGroupId: String = ""
  var operatorUser: NECoHostUserInfo? // "operator" 是关键字
  var inviter: NECoHostUserInfo?
  var inviteeList: [NECoHostUserInfo] = []
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, operatorUser = "operator", inviter, inviteeList, ext
  }
}

@objcMembers
class NECoHostConnectionCancelMsg: NSObject, Codable {
  var connectionGroupId: String = ""
  var inviter: NECoHostUserInfo?
  var inviteeList: [NECoHostUserInfo] = []
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, inviter, inviteeList, ext
  }
}

@objcMembers
class NECoHostConnectionListChangeMsg: NSObject, Codable {
  var connectionGroupId: String = ""
  var connectedList: [NECoHostUserInfo] = []
  var joinedList: [NECoHostUserInfo] = []
  var leavedList: [NECoHostUserInfo] = []
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, connectedList, joinedList, leavedList, ext
  }
}

@objcMembers
class NECoHostConnectionEndedMsg: NSObject, Codable {
  var connectionGroupId: String = ""
  var operatorUser: NECoHostUserInfo? // "operator" 是关键字
  var connectedList: [NECoHostUserInfo] = []
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, operatorUser = "operator", connectedList, ext
  }
}

@objcMembers
class NECoHostConnectionTimeoutMsg: NSObject, Codable {
  var connectionGroupId: String = ""
  var inviter: NECoHostUserInfo?
  var inviteeList: [NECoHostUserInfo] = []
  var ext: String? = nil

  private enum CodingKeys: String, CodingKey {
    case connectionGroupId, inviter, inviteeList, ext
  }
}
