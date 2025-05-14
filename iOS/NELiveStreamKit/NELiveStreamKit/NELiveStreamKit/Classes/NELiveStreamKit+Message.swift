// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

public extension NELiveStreamKit {
  /// 发送聊天室消息
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameters:
  ///   - content: 发送内容
  ///   - callback: 回调
  func sendTextMessage(_ content: String, callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Send text message. Content: \(content).")
    Judge.preCondition({
      self.roomContext!.chatController
        .sendBroadcastTextMessage(message: content) { code, msg, _ in
          if code == 0 {
            NELiveStreamLog.successLog(kitTag, desc: "Successfully send text message.")
          } else {
            NELiveStreamLog.errorLog(
              kitTag,
              desc: "Failed to send text message. Code: \(code). Msg: \(msg ?? "")"
            )
          }
          callback?(code, msg, nil)
        }
    }, failure: callback)
  }

  /// 给房间内用户发送自定义消息，如房间内信令
  /// - Parameters:
  ///   - userUuid: 目标成员Id
  ///   - commandId: 消息类型 区间[10000 - 19999]
  ///   - data: 自定义消息内容
  ///   - callback: 回调
  func sendCustomMessage(_ userUuid: String,
                         commandId: Int,
                         data: String,
                         callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(
      kitTag,
      desc: "Send custom message. UserUuid: \(userUuid). CommandId: \(commandId). Data: \(data)"
    )
    Judge.preCondition({
      NERoomKit.shared().messageChannelService
        .sendCustomMessage(roomUuid: self.roomContext!.roomUuid,
                           userUuid: userUuid,
                           commandId: commandId,
                           data: data,
                           crossAppAuthorization: nil) { code, msg, _ in
          if code == 0 {
            NELiveStreamLog.successLog(kitTag, desc: "Successfully send custom message.")
          } else {
            NELiveStreamLog.errorLog(
              kitTag,
              desc: "Failed to send custom message. Code: \(code). Msg: \(msg ?? "")"
            )
          }
          callback?(code, msg, nil)
        }
    }, failure: callback)
  }

  func handleNotificationMessage(_ message: NERoomChatNotificationMessage) {
    guard let members = message.members else {
      return
    }

    let list = members.map { NELiveStreamMember($0) }
    switch message.eventType {
    case .enter:
      for pointListener in listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onMemberJoinChatroom(_:))) {
          listener.onMemberJoinChatroom?(list)
        }
      }

    case .exit:
      for pointListener in listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onMemberLeaveChatroom(_:))) {
          listener.onMemberLeaveChatroom?(list)
        }
      }

    default:
      break // 处理未来可能新增的枚举值
    }
  }

  /// 处理RoomKit自定义消息
  func handleCustomMessage(_ message: NERoomChatCustomMessage) {
    NELiveStreamLog.infoLog(kitTag, desc: "Receive custom message.")
    guard let dic = message.attachStr?.toDictionary() else { return }
    NELiveStreamLog.infoLog(kitTag, desc: "custom message:\(dic)")
    if let _ = message.attachStr,
       let data = dic["data"] as? [String: Any],
       let jsonData = try? JSONSerialization.data(withJSONObject: data, options: []),
       let jsonString = String(data: jsonData, encoding: .utf8),
       let type = dic["type"] as? Int {
//            if type == 1005,
//            let obj = NELiveStreamDecoder.decode(
//             _NELiveStreamBatchRewardMessage.self,
//             jsonString: jsonString
//            ) {
//             handleBatchGiftMessage(obj)
//            }
      switch type {
      // 合唱消息
      case 1005:
        if let obj = NELiveStreamDecoder.decode(
          _NELiveStreamBatchRewardMessage.self,
          jsonString: jsonString
        ) {
          handleBatchGiftMessage(obj)
        }
      case 1105:
        handlePauseLiveMessage()
      case 1106:
        handleResumeLiveMessage()
      default: break
      }
    }
  }

  /// 发送礼物
  /// - Parameters:
  ///   - giftId: 礼物编号
  ///   - giftCount: 礼物数量
  ///   - userUuids: 要打赏的目标用户
  ///   - callback: 结果回调
  func sendBatchGift(_ giftId: Int,
                     giftCount: Int,
                     userUuids: [String],
                     callback: NELiveStreamCallback<AnyObject>? = nil) {
    guard NELiveStreamKit.getInstance().isInitialized else {
      NELiveStreamLog.errorLog(kitTag, desc: "Failed to send batch gift. Uninitialized.")
      callback?(NELiveStreamErrorCode.failed, "Failed to send batch gift. Uninitialized.", nil)
      return
    }

    guard let liveRecordId = liveInfo?.liveModel?.liveRecordId else {
      NELiveStreamLog.errorLog(kitTag, desc: "Failed to send batch gift. liveRecordId not exist.")
      callback?(
        NELiveStreamErrorCode.failed,
        "Failed to send batch gift. liveRecordId not exist.",
        nil
      )
      return
    }

    roomService.batchReward(liveRecordId, giftId: giftId, giftCount: giftCount, userUuids: userUuids) {
      callback?(NELiveStreamErrorCode.success, "Successfully send batch gift.", nil)
    } failure: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }
}

// MARK: - NERoomListener

extension NELiveStreamKit: NERoomListener {
  public func onReceiveChatroomMessages(messages: [NERoomChatMessage]) {
    DispatchQueue.main.async {
      for message in messages {
        switch message.messageType {
        case .text:
          for pointListener in self.listeners.allObjects {
            guard let listener = pointListener as? NELiveStreamListener,
                  let textMessage = message as? NERoomChatTextMessage else { continue }

            if listener.responds(to: #selector(NELiveStreamListener.onReceiveTextMessage(_:))) {
              listener.onReceiveTextMessage?(NELiveStreamChatTextMessage(textMessage))
            }
          }
        case .custom:
          if let msg = message as? NERoomChatCustomMessage {
            self.handleCustomMessage(msg)
          }
        case .notification:
          if let msg = message as? NERoomChatNotificationMessage {
            self.handleNotificationMessage(msg)
          }
        default:
          break
        }
      }
    }
  }

  public func onMemberJoinChatroom(members: [NERoomMember]) {
    DispatchQueue.main.async {
      let list = members.map { NELiveStreamMember($0) }
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onMemberJoinChatroom(_:))) {
          listener.onMemberJoinChatroom?(list)
        }
      }
    }
  }

  /// 处理批量礼物消息
  private func handleBatchGiftMessage(_ giftMsg: _NELiveStreamBatchRewardMessage) {
    guard let _ = giftMsg.userUuid,
          let _ = giftMsg.userName,
          let _ = giftMsg.giftId,
          !giftMsg.targets.isEmpty else {
      return
    }

    let giftModel = NELiveStreamBatchGiftModel(giftMsg)
    NELiveStreamLog.messageLog(
      kitTag,
      desc: "Handle batch gift message. SendAccount: \(giftModel.sendAccount). SendNick: \(giftModel.rewarderUserName). GiftId: \(giftModel.giftId)."
    )

    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onReceiveBatchGift(giftModel:))) {
          listener.onReceiveBatchGift?(giftModel: giftModel)
        }
      }
    }
  }

  private func handlePauseLiveMessage() {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onLivePause)) {
          listener.onLivePause?()
        }
      }
    }
  }

  private func handleResumeLiveMessage() {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onLiveResume)) {
          listener.onLiveResume?()
        }
      }
    }
  }

  public func onRoomEnded(reason: NERoomEndReason) {
    DispatchQueue.main.async {
      self.reset()
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onRoomEnded(_:))) {
          listener.onRoomEnded?(NELiveStreamEndReason(rawValue: reason.rawValue) ?? .systemError)
        }
      }
    }
  }

  public func onMemberJoinRoom(members: [NERoomMember]) {
    DispatchQueue.main.async {
      let list = members.map { NELiveStreamMember($0) }
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onMemberJoinRoom(_:))) {
          listener.onMemberJoinRoom?(list)
        }
      }
    }
  }

  public func onMemberLeaveRoom(members: [NERoomMember]) {
    DispatchQueue.main.async {
      let list = members.map { NELiveStreamMember($0) }
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onMemberLeaveRoom(_:))) {
          listener.onMemberLeaveRoom?(list)
        }
      }
    }
  }

  public func onRtcChannelError(code: Int) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onRtcChannelError(_:))) {
          listener.onRtcChannelError?(code)
        }
      }
    }
  }

  public func onSeatRequestSubmitted(_ seatIndex: Int, user: String) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener.onSeatRequestSubmitted(_:account:))) {
          listener.onSeatRequestSubmitted?(seatIndex, account: user)
        }
      }
    }
  }

  public func onSeatRequestCancelled(_ seatIndex: Int, user: String) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener.onSeatRequestCancelled(_:account:))) {
          listener.onSeatRequestCancelled?(seatIndex, account: user)
        }
      }
    }
  }

  public func onSeatRequestApproved(_ seatIndex: Int, user: String, operateBy: String,
                                    isAutoAgree: Bool) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener
              .onSeatRequestApproved(_:account:operateBy:isAutoAgree:))) {
          listener.onSeatRequestApproved?(
            seatIndex,
            account: user,
            operateBy: operateBy,
            isAutoAgree: isAutoAgree
          )
        }
      }
    }
  }

  public func onSeatRequestRejected(_ seatIndex: Int, user: String, operateBy: String) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener
              .onSeatRequestRejected(_:account:operateBy:))) {
          listener.onSeatRequestRejected?(seatIndex, account: user, operateBy: operateBy)
        }
      }
    }
  }

  public func onSeatLeave(_ seatIndex: Int, user: String) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onSeatLeave(_:account:))) {
          listener.onSeatLeave?(seatIndex, account: user)
        }
      }
    }
  }

  public func onSeatKicked(_ seatIndex: Int, user: String, operateBy: String) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener.onSeatKicked(_:account:operateBy:))) {
          listener.onSeatKicked?(seatIndex, account: user, operateBy: operateBy)
        }
      }
    }
  }

  public func onSeatInvitationReceived(_ seatIndex: Int, user: String, operateBy: String) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener.onSeatInvitationReceived(_:account:operateBy:))) {
          listener.onSeatInvitationReceived?(seatIndex, account: user, operateBy: operateBy)
        }
      }
    }
  }

  public func onSeatInvitationCancelled(_ seatIndex: Int, user: String, operateBy: String) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener.onSeatInvitationCancelled(_:account:operateBy:))) {
          listener.onSeatInvitationCancelled?(seatIndex, account: user, operateBy: operateBy)
        }
      }
    }
  }

  /// seat open
  /// seat close
  /// seat enter
  public func onSeatListChanged(_ seatItems: [NESeatItem]) {
    DispatchQueue.main.async {
      let items = seatItems.map { NELiveStreamSeatItem($0) }
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener.responds(to: #selector(NELiveStreamListener.onSeatListChanged(_:))) {
          listener.onSeatListChanged?(items)
        }
      }
      guard let context = self.roomContext else { return }
      var newState = NELiveStreamSeatItemStatus.initial
      if let item = items.first(where: { $0.user == context.localMember.uuid }) {
        newState = item.status
      }

      var oldState = NELiveStreamSeatItemStatus.initial
      if let localItem = self.localSeats?.first(where: { $0.user == context.localMember.uuid }) {
        oldState = localItem.status
      }

      // 上报自己麦位状态的变更
      if newState != oldState {
        for pointListener in self.listeners.allObjects {
          if let listener = pointListener as? NELiveStreamListener,
             listener.responds(to: #selector(NELiveStreamListener.onSelfSeatStatusChanged(new:old:))) {
            listener.onSelfSeatStatusChanged?(new: newState, old: oldState)
          }
        }

        // 新状态 在麦上
        if newState == .taken {
          self.unmuteMyAudio()
        }

        NELiveStreamLog.infoLog(kitTag, desc: "self seat state changed, new state:\(newState.rawValue), old state:\(oldState.rawValue)")
      }

      self.localSeats = items
    }
  }

  public func onSeatInvitationAccepted(_ seatIndex: Int, user: String, isAutoAgree: Bool) {
    DispatchQueue.main.async {
      for pointListener in self.listeners.allObjects {
        guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

        if listener
          .responds(to: #selector(NELiveStreamListener
              .onSeatInvitationAccepted(_:account:isAutoAgree:))) {
          listener.onSeatInvitationAccepted?(
            seatIndex,
            account: user,
            isAutoAgree: isAutoAgree
          )
        }
      }
    }
  }

  public func onSeatInvitationRejected(_ seatIndex: Int, user: String) {
    for pointListener in listeners.allObjects {
      guard pointListener is NELiveStreamListener, let listener = pointListener as? NELiveStreamListener else { continue }

      if listener
        .responds(to: #selector(NELiveStreamListener
            .onSeatInvitationRejected(_:account:))) {
        listener.onSeatInvitationRejected?(seatIndex, account: user)
      }
    }
  }
}
