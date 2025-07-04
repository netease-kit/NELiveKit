// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 连麦主播信息
@objcMembers
public class NEConnectionUser: NSObject {
  /// 用户ID
  public var userUuid: String
  /// 房间ID
  public var roomUuid: String?
  /// 用户名称
  public var name: String?
  /// 用户头像
  public var avatar: String?
  /// 连麦开始标记时间戳
  public var connectionTime: TimeInterval?

  public init(userUuid: String, roomUuid: String? = nil, name: String? = nil, avatar: String? = nil, connectionTime: TimeInterval? = nil) {
    self.userUuid = userUuid
    self.roomUuid = roomUuid
    self.name = name
    self.avatar = avatar
    self.connectionTime = connectionTime
  }

  override public func isEqual(_ object: Any?) -> Bool {
    guard let other = object as? NEConnectionUser else { return false }
    return userUuid == other.userUuid
  }

  override public var hash: Int {
    userUuid.hashValue
  }
}

@objcMembers public class NECoHostManager: NSObject {
  private static let kitTag = "NECoHostManager"

  // NECoHostManager 监听器数组
  var listeners = NSPointerArray.weakObjects()

  /// 已连线的用户列表
  private var connectedUserList: [NEConnectionUser] = []

  /// 获取已连线的用户列表
  public var coHostUserList: [NEConnectionUser] {
    connectedUserList
  }

  // 记录上一次的用户列表
  private var lastConnectedList: [NEConnectionUser] = []
  private var lastJoinedList: [NEConnectionUser] = []
  private var lastLeavedList: [NEConnectionUser] = []

  /// 更新已连线的用户列表
  /// - Parameters:
  ///   - users: 新的用户列表
  func updateConnectedUserList(_ users: [NEConnectionUser]) {
    let previousList = connectedUserList
    let oldUserIds = Set(previousList.map(\.userUuid))
    let newUserIds = Set(users.map(\.userUuid))
    let joinedIds = newUserIds.subtracting(oldUserIds)
    let leavedIds = oldUserIds.subtracting(newUserIds)
    let joinedList = users.filter { joinedIds.contains($0.userUuid) }
    let leavedList = previousList.filter { leavedIds.contains($0.userUuid) }
    connectedUserList = users

    updatesUserList(users: users, joinedList: joinedList, leavedList: leavedList)
  }

  /// 通知监听器用户列表发生变化
  private func notifyConnectionUserListChanged(connectedList: [NEConnectionUser], joinedList: [NEConnectionUser], leavedList: [NEConnectionUser]) {
    for pointListener in listeners.allObjects {
      if let coHostListener = pointListener as? NECoHostListener {
        coHostListener.onConnectionUserListChanged?(connectedList: connectedList, joinedList: joinedList, leavedList: leavedList)
      }
    }
  }

  /// 添加直播流监听器
  public func addListener(_ listener: NECoHostListener) {
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Add CoHost listener.")
    listeners.addWeakObject(listener)
  }

  /// 移除直播流监听器
  public func removeListener(_ listener: NECoHostListener) {
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Remove CoHost listener.")
    listeners.removeWeakObject(listener)
  }

  private let coHostService = NECoHostService()

  /// 请求主播连麦
  /// - Parameters:
  ///   - roomUuid: 房间id
  ///   - timeoutSeconds: 超时时间
  ///   - callback: 回调
  public func requestConnection(roomUuid: String, timeoutSeconds: Int, callback: NELiveStreamCallback<Void>? = nil) {
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Request connection with room: \(roomUuid)")
    coHostService.requestHostConnection(toRoomIds: [roomUuid], timeout: timeoutSeconds, ext: nil, success: { response in
      if response != nil {
        NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Request connection with room: \(roomUuid) success")
        callback?(NELiveStreamErrorCode.success, nil, nil)
      } else {
        NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Request connection with room: \(roomUuid) failed: response is nil")
        callback?(NELiveStreamErrorCode.failed, "response is nil", nil)
      }
    }, failure: { error in
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Request connection with room: \(roomUuid) failed: \(error.localizedDescription)")
      callback?(error.code, error.localizedDescription, nil)
    })
  }

  /// 取消请求主播连麦
  /// - Parameters:
  ///   - roomUuid: 房间id
  ///   - callback: 回调
  public func cancelRequest(roomUuid: String, callback: NELiveStreamCallback<Void>? = nil) {
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Cancel connection request for room: \(roomUuid)")
    coHostService.cancelRequestHostConnection(toRoomIds: [roomUuid], success: {
      NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Cancel connection request for room: \(roomUuid) success")
      callback?(NELiveStreamErrorCode.success, nil, nil)
    }, failure: { error in
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Cancel connection request for room: \(roomUuid) failed: \(error.localizedDescription)")
      callback?(error.code, error.localizedDescription, nil)
    })
  }

  /// 接受主播连麦
  /// - Parameters:
  ///   - roomUuid: 房间id
  ///   - callback: 回调
  public func accept(roomUuid: String, callback: NELiveStreamCallback<Void>? = nil) {
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Accept connection for room: \(roomUuid)")
    coHostService.acceptRequestHostConnection(toRoomId: roomUuid, success: { [weak self] in
      self?.startMediaRelay(roomUuid: roomUuid, callback: { code, msg, _ in
        mainAsyncSafe {
          if code == 0 {
            callback?(NELiveStreamErrorCode.success, nil, nil)
            NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Accept Start media relay success")
          } else if code == NEErrorCode.DUPLICATE_CALL {
            callback?(code, msg ?? "", nil)
            NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Accept Start media relay DUPLICATE_CALL: \(msg ?? "")")
          } else {
            callback?(code, msg ?? "", nil)
            self?.disconnect(roomUuid: roomUuid)
            NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Accept Start media relay failed: \(msg ?? "")")
          }
        }
      })
    }, failure: { error in
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Accept connection for room: \(roomUuid) failed: \(error.localizedDescription)")
      callback?(error.code, error.localizedDescription, nil)
    })
  }

  /// 拒绝主播连麦
  /// - Parameters:
  ///   - roomUuid: 房间id
  ///   - callback: 回调
  public func reject(roomUuid: String, callback: NELiveStreamCallback<Void>? = nil) {
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Reject connection for room: \(roomUuid)")
    coHostService.rejectRequestHostConnection(toRoomId: roomUuid, success: {
      NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Reject connection for room: \(roomUuid) success")
      callback?(NELiveStreamErrorCode.success, nil, nil)
    }, failure: { error in
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Reject connection for room: \(roomUuid) failed: \(error.localizedDescription)")
      callback?(error.code, error.localizedDescription, nil)
    })
  }

  /// 断开主播连麦
  /// - Parameter roomUuid: 房间id
  public func disconnect(roomUuid: String) {
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Disconnect from room: \(roomUuid)")
    coHostService.disconnectHostConnection(toRoomId: roomUuid, success: {
      NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Disconnect from room: \(roomUuid) success")
    }, failure: { error in
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Disconnect from room: \(roomUuid) failed: \(error.localizedDescription)")
    })
    let newList: [NEConnectionUser] = []
    updateConnectedUserList(newList)
    stopMediaRelay(roomUuid: roomUuid)
  }

  func startMediaRelay(roomUuid: String, callback: NELiveStreamCallback<Void>? = nil) {
    guard let roomContext = NELiveStreamKit.getInstance().roomContext else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Start media relay failed: roomContext is nil")
      return
    }
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Start media relay for room: \(roomUuid)")
    roomContext.rtcController.startChannelMediaRelay(roomUuid: roomUuid, callback: { code, msg, _ in
      if code == 0 {
        NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Start media relay success")
        callback?(NELiveStreamErrorCode.success, nil, nil)
      } else {
        NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Start media relay failed: \(msg ?? "")")
        callback?(code, msg ?? "", nil)
      }
    })
  }

  func stopMediaRelay(roomUuid: String) {
    guard let roomContext = NELiveStreamKit.getInstance().roomContext else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Stop media relay failed: roomContext is nil")
      return
    }
    NELiveStreamLog.apiLog(NECoHostManager.kitTag, desc: "Stop media relay for room: \(roomUuid)")
    roomContext.rtcController.stopChannelMediaRelay(roomUuid: roomUuid, callback: { code, msg, _ in
      if code == 0 {
        NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Stop media relay success")
      } else {
        NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Stop media relay failed: \(msg ?? "")")
      }
    })
  }

  func cleanResource() {
    let newList: [NEConnectionUser] = []
    updateConnectedUserList(newList)
  }

  func updatesUserList(users: [NEConnectionUser], joinedList: [NEConnectionUser], leavedList: [NEConnectionUser]) {
    // 只有有变化才回调
    func arrayChanged(_ a: [NEConnectionUser], _ b: [NEConnectionUser]) -> Bool {
      if a.isEmpty, b.isEmpty { return false }
      if a.count != b.count { return true }
      return !a.elementsEqual(b)
    }

    let connectedChanged = arrayChanged(users, lastConnectedList)
    let joinedChanged = arrayChanged(joinedList, lastJoinedList)
    let leavedChanged = arrayChanged(leavedList, lastLeavedList)
    if connectedChanged || joinedChanged || leavedChanged {
      NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "connectedList: \(users), joinedList: \(joinedList), leavedList: \(leavedList)")
      notifyConnectionUserListChanged(connectedList: users, joinedList: joinedList, leavedList: leavedList)
      lastConnectedList = users
      lastJoinedList = joinedList
      lastLeavedList = leavedList
    }
  }
}

extension NECoHostManager: NERoomListener {
  public func onMemberJoinRtcChannel(members: [NERoomMember]) {
    var newList = connectedUserList
    let existingUserIds = Set(newList.map(\.userUuid))
    for member in members {
      NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "onMemberJoinRtcChannel: \(member.uuid ?? "")")
      if !existingUserIds.contains(member.uuid) {
        // 判断如果是自己，就return
        if let localMember = NELiveStreamKit.getInstance().localMember,
           member.uuid == localMember.account {
          continue
        }
        guard let roomUuid = member.relayInfo?.fromRoomUuid else {
          NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "onMemberJoinRtcChannel failed: roomUuid is nil for user: \(member.uuid ?? "")")
          continue
        }
        let user = NEConnectionUser(userUuid: member.uuid, roomUuid: roomUuid, name: member.name, avatar: member.avatar, connectionTime: nil)
        newList.append(user)
      }
    }
    updateConnectedUserList(newList)
  }

  public func onMemberLeaveRtcChannel(members: [NERoomMember]) {
    var newList = connectedUserList
    for member in members {
      NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "onMemberLeaveRtcChannel: \(member.uuid ?? "")")
      newList.removeAll { $0.userUuid == member.uuid }
    }
    updateConnectedUserList(newList)
  }

  public func onMemberLeaveRoom(members: [NERoomMember]) {
    var newList = connectedUserList
    for member in members {
      NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "onMemberLeaveRoom: \(member.uuid ?? "")")
      newList.removeAll { $0.userUuid == member.uuid }
    }
    updateConnectedUserList(newList)
  }

  public func onRtcChannelDisconnect(channel: String?, reason: Int) {
    let newList: [NEConnectionUser] = []
    updateConnectedUserList(newList)
  }

  public func onReceiveChatroomMessages(messages: [NERoomChatMessage]) {
    DispatchQueue.main.async {
      for message in messages {
        switch message.messageType {
        case .custom:
          if let msg = message as? NERoomChatCustomMessage {
            self.handleCustomMessage(msg)
          }
        default:
          break
        }
      }
    }
  }

  public func onRoomEnded(reason: NERoomEndReason) {
    cleanResource()
  }
}

extension NECoHostManager {
  func handleCustomMessage(_ message: NERoomChatCustomMessage) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Receive custom message.")
    guard let dic = message.attachStr?.toDictionary() else { return }
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "custom message:\(dic)")
    if let _ = message.attachStr,
       let data = dic["data"] as? [String: Any],
       let jsonData = try? JSONSerialization.data(withJSONObject: data, options: []),
       let jsonString = String(data: jsonData, encoding: .utf8),
       let type = dic["type"] as? Int {
      switch type {
      // 合唱消息
      case NELiveCoHostRequestType.request.rawValue ... NELiveCoHostRequestType.timeout.rawValue:
        let actionType = NELiveCoHostRequestType(rawValue: type) ?? .unknown
        handleCoHostMessage(actionType, message: jsonString)
      default: break
      }
    }
  }

  func handleCoHostMessage(_ actiontype: NELiveCoHostRequestType, message: String) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Receive co host message. actiontype: \(actiontype.rawValue)")
    switch actiontype {
    case .request:
      if let obj = NELiveStreamDecoder.decode(
        NECoHostConnectionRequestMsg.self,
        jsonString: message
      ) {
        handleCoHostRequestMessage(obj)
      }
    case .cancel:
      if let obj = NELiveStreamDecoder.decode(
        NECoHostConnectionCancelMsg.self,
        jsonString: message
      ) {
        handleCoHostCancelMessage(obj)
      }
    case .accept:
      if let obj = NELiveStreamDecoder.decode(
        NECoHostConnectionAcceptedMsg.self,
        jsonString: message
      ) {
        handleCoHostAcceptMessage(obj)
      }
    case .reject:
      if let obj = NELiveStreamDecoder.decode(
        NECoHostConnectionRejectedMsg.self,
        jsonString: message
      ) {
        handleCoHostRejectMessage(obj)
      }
    case .exit:
      if let obj = NELiveStreamDecoder.decode(
        NECoHostConnectionEndedMsg.self,
        jsonString: message
      ) {
        handleCoHostExitMessage(obj)
      }
    case .timeout:
      if let obj = NELiveStreamDecoder.decode(
        NECoHostConnectionTimeoutMsg.self,
        jsonString: message
      ) {
        handleCoHostTimeoutMessage(obj)
      }
    case .unknown: break
      // 不需要处理
    }
  }

  func handleCoHostRequestMessage(_ message: NECoHostConnectionRequestMsg) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Handle co host request message.")
    guard let fromUser = message.inviter else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Handle co host request message failed: fromUser is nil")
      return
    }
    let inviter = NEConnectionUser(userUuid: fromUser.userUuid, roomUuid: fromUser.roomUuid, name: fromUser.userName, avatar: fromUser.icon, connectionTime: fromUser.joinedTime)
    guard let toUser = message.inviteeList.first else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Handle co host request message failed: toUser is nil")
      return
    }

    var inviteeList: [NEConnectionUser] = []

    inviteeList = [NEConnectionUser(userUuid: toUser.userUuid, roomUuid: toUser.roomUuid, name: toUser.userName, avatar: toUser.icon, connectionTime: toUser.joinedTime)]
    let ext = message.ext
    notifyConnectionRequestReceived(inviter: inviter, inviteeList: inviteeList, ext: ext)
  }

  func handleCoHostCancelMessage(_ message: NECoHostConnectionCancelMsg) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Handle co host cancel message.")
    guard let fromUser = message.inviter else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Handle co host cancel message failed: fromUser is nil")
      return
    }
    let inviter = NEConnectionUser(userUuid: fromUser.userUuid, roomUuid: fromUser.roomUuid, name: fromUser.userName, avatar: fromUser.icon, connectionTime: fromUser.joinedTime)
    notifyConnectionRequestCancelled(inviter: inviter)
  }

  func handleCoHostAcceptMessage(_ message: NECoHostConnectionAcceptedMsg) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Handle co host accept message.")
    guard let toUser = message.inviteeList.first else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Handle co host accept message failed: toUser is nil")
      return
    }
    let invitee = NEConnectionUser(userUuid: toUser.userUuid, roomUuid: toUser.roomUuid, name: toUser.userName, avatar: toUser.icon, connectionTime: toUser.joinedTime)
    notifyConnectionRequestAccepted(invitee: invitee)
  }

  func handleCoHostRejectMessage(_ message: NECoHostConnectionRejectedMsg) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Handle co host reject message.")
    guard let toUser = message.inviteeList.first else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Handle co host reject message failed: toUser is nil")
      return
    }
    let invitee = NEConnectionUser(userUuid: toUser.userUuid, roomUuid: toUser.roomUuid, name: toUser.userName, avatar: toUser.icon, connectionTime: toUser.joinedTime)
    notifyConnectionRequestRejected(invitee: invitee)
  }

  func handleCoHostExitMessage(_ message: NECoHostConnectionEndedMsg) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Handle co host exit message.")
    guard let fromUser = message.operatorUser else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Handle co host exit message failed: fromUser is nil")
      return
    }
    let invitee = NEConnectionUser(userUuid: fromUser.userUuid, roomUuid: fromUser.roomUuid, name: fromUser.userName, avatar: fromUser.icon, connectionTime: fromUser.joinedTime)
    notifyConnectionRequestExited(invitee: invitee)
  }

  func handleCoHostTimeoutMessage(_ message: NECoHostConnectionTimeoutMsg) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Handle co host timeout message.")
    guard let fromUser = message.inviter else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Handle co host timeout message failed: inviter is nil")
      return
    }
    let inviter = NEConnectionUser(userUuid: fromUser.userUuid, roomUuid: fromUser.roomUuid, name: fromUser.userName, avatar: fromUser.icon, connectionTime: fromUser.joinedTime)
    let inviteeList = message.inviteeList.map { NEConnectionUser(userUuid: $0.userUuid, roomUuid: $0.roomUuid, name: $0.userName, avatar: $0.icon, connectionTime: $0.joinedTime) }
    notifyConnectionRequestTimeout(inviter: inviter, inviteeList: inviteeList)
  }

  func notifyConnectionRequestReceived(inviter: NEConnectionUser, inviteeList: [NEConnectionUser], ext: String?) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Notify connection request received.")

    for pointListener in listeners.allObjects {
      if let coHostListener = pointListener as? NECoHostListener {
        coHostListener.onConnectionRequestReceived?(inviter: inviter, inviteeList: inviteeList, ext: ext)
      }
    }
  }

  func notifyConnectionRequestCancelled(inviter: NEConnectionUser) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Notify connection request cancelled.")
    for pointListener in listeners.allObjects {
      if let coHostListener = pointListener as? NECoHostListener {
        coHostListener.onConnectionRequestCancelled?(inviter: inviter)
      }
    }
  }

  func notifyConnectionRequestAccepted(invitee: NEConnectionUser) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Notify connection request accepted.")
    for pointListener in listeners.allObjects {
      if let coHostListener = pointListener as? NECoHostListener {
        coHostListener.onConnectionRequestAccept?(invitee: invitee)
      }
    }
    guard let roomUuid = invitee.roomUuid else {
      NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "Notify connection request accepted failed: roomUuid is nil")
      return
    }

    startMediaRelay(roomUuid: roomUuid, callback: { [weak self] code, msg, _ in
      if code == 0 {
        NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "onAccepted Start media relay success")
      } else if code == NEErrorCode.DUPLICATE_CALL {
        NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "onAccepted Start media relay DUPLICATE_CALL: \(msg ?? "")")
      } else {
        NELiveStreamLog.errorLog(NECoHostManager.kitTag, desc: "onAccepted Start media relay failed: \(msg ?? "")")
        // 失败后 要取消PK
        self?.disconnect(roomUuid: roomUuid)
      }
    })
  }

  func notifyConnectionRequestRejected(invitee: NEConnectionUser) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Notify connection request rejected.")
    for pointListener in listeners.allObjects {
      if let coHostListener = pointListener as? NECoHostListener {
        coHostListener.onConnectionRequestReject?(invitee: invitee)
      }
    }
  }

  func notifyConnectionRequestTimeout(inviter: NEConnectionUser, inviteeList: [NEConnectionUser]) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Notify connection request timeout.")
    for pointListener in listeners.allObjects {
      if let coHostListener = pointListener as? NECoHostListener {
        coHostListener.onConnectionRequestTimeout?(inviter: inviter, inviteeList: inviteeList)
      }
    }
  }

  func notifyConnectionRequestExited(invitee: NEConnectionUser) {
    NELiveStreamLog.infoLog(NECoHostManager.kitTag, desc: "Notify connection request exited.")
    var newList = connectedUserList
    newList.removeAll { $0.userUuid == invitee.userUuid }
    updateConnectedUserList(newList)
  }
}
