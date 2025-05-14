//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

@objc
public enum NELiveStreamLiveState: Int {
  /// 未开始
  case notStart = 0
  /// 直播中
  case live = 1
  /// 直播结束
  case liveClose = 6
}

@objc
public extension NELiveStreamKit {
  /// 查询房间列表
  /// - Parameters:
  ///   - liveState: 直播状态
  ///   - pageNum: 页码
  ///   - pageSize: 页大小
  ///   - callback: 房间列表回调
  ///
  ///     ///  liveType类型：
  /// 1：互动直播,
  /// 2：语聊房,
  /// 3："KTV房间"，
  /// 4：互动直播——跨频道转发房间，
  /// 5：一起听

  func getRoomList(_ liveState: NELiveStreamLiveState,
                   type: Int = 4,
                   pageNum: Int,
                   pageSize: Int,
                   callback: NELiveStreamCallback<NELiveStreamRoomList>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Room List.")
    Judge.initCondition({
      self.roomService.getRoomList(
        type,
        pageNum: pageNum,
        pageSize: pageSize
      ) { list in
        NELiveStreamLog.successLog(kitTag, desc: "Successfully get room list.")
        callback?(NELiveStreamErrorCode.success, nil, list)
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to get room list. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 获取房间的信息
  func getRoomInfo(_ liveRecordId: Int,
                   callback: NELiveStreamCallback<NELiveStreamRoomInfo>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "get VoiceRoom RoomInfo.")

    Judge.initCondition({
      self.roomService.getRoomInfo(liveRecordId) { info in
        NELiveStreamLog.successLog(
          kitTag,
          desc: "Successfully get create room default info."
        )
        callback?(NELiveStreamErrorCode.success, nil, NELiveStreamRoomInfo(create: info))
      }
      failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to get room info. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }

    }, failure: callback)
  }

  /// 当前所在房间信息
  func getCurrentRoomInfo() -> NELiveStreamRoomInfo? {
    if let _ = roomContext,
       let liveInfo = liveInfo {
      return liveInfo
    }
    return nil
  }

  /// 获取创建房间的默认信息
  /// - Parameter callback: 回调
  func getCreateRoomDefaultInfo(_ callback: NELiveStreamCallback<NECreateLiveStreamRoomDefaultInfo>? =
    nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Get create room default info.")
    Judge.initCondition({
      self.roomService.getDefaultLiveInfo { info in
        NELiveStreamLog.successLog(
          kitTag,
          desc: "Successfully get create room default info."
        )
        callback?(NELiveStreamErrorCode.success, nil, NECreateLiveStreamRoomDefaultInfo(info))
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to get create room default info. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 实名认证
  /// - Parameters:
  ///   - name: 用户真实姓名，以身份证上姓名为准，最大长度32
  ///   - cardNo: 用户身份证号码，目前支持一代/二代身份证，号码必须为18位或15位，末尾为x的需要大写为X，最大长度18
  ///   - callback: 认证结果
  func authenticate(name: String, cardNo: String, callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "authenticate.")
    Judge.initCondition({
      self.roomService.authenticate(name: name, cardNo: cardNo) {
        NELiveStreamLog.successLog(
          kitTag,
          desc: "Successfully authenticate."
        )
        callback?(NELiveStreamErrorCode.success, nil, nil)
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to authenticate. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 创建房间并进入房间
  /// - Parameters:
  ///   - params: 房间参数
  ///   - callback: 回调
  func createRoom(_ params: NECreateLiveStreamRoomParams,
                  callback: NELiveStreamCallback<NELiveStreamRoomInfo>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Create room.")

    // 初始化判断
    Judge.initCondition({
      self.roomService.startLiveStreamRoom(params) { [weak self] resp in
        guard let self = self else { return }
        guard let resp = resp else {
          NELiveStreamLog.errorLog(kitTag, desc: "Failed to create room. RoomUuid is nil.")
          callback?(
            NELiveStreamErrorCode.failed,
            "Failed to create room. RoomUuid is nil.",
            nil
          )
          return
        }
        // 存储直播信息
        self.liveInfo = NELiveStreamRoomInfo(create: resp)
        NELiveStreamLog.successLog(kitTag, desc: "Successfully create room.")
        callback?(NELiveStreamErrorCode.success, nil, NELiveStreamRoomInfo(create: resp))
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to create room. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 加入房间
  /// - Parameters:
  ///   - params: 加入房间时参数
  ///   - callback: 回调
  func joinRoom(_ params: NEJoinLiveStreamRoomParams,
                callback: NELiveStreamCallback<NELiveStreamRoomInfo>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Join room.")

    // liveRecordId

    liveRecordId = params.liveRecordId
    if let roomInfo = params.roomInfo {
      liveInfo = roomInfo
    }

    // 初始化判断
    Judge.initCondition({
      func join(_ params: NEJoinLiveStreamRoomParams,
                callback: NELiveStreamCallback<NELiveStreamRoomInfo>?) {
        self._joinRoom(params.roomUuid,
                       userName: params.nick,
                       role: params.role.toString(),
                       isRejoin: params.isRejoin) { [weak self] joinCode, joinMsg, _ in
          if joinCode == 0 {
            callback?(joinCode, joinMsg, nil)

          } else {
            callback?(joinCode, joinMsg, nil)
          }
        }
      }
      // 如果已经在此房间里则先退出
      if let context = NERoomKit.shared().roomService
        .getRoomContext(roomUuid: params.roomUuid) {
        context.leaveRoom { code, msg, obj in
          join(params, callback: callback)
        }
      } else {
        join(params, callback: callback)
      }
    }, failure: callback)
  }

  // 观众切换角色
  func changeMemberRole(userUuid: String,
                        role: String,
                        callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "change Member Role :\(role)")
    // 初始化、roomContext 判断
    Judge.preCondition({
      // 主动调用离开聊天室

      var roomRole = NERoomBuiltinRole.Observer
      if role == NELiveStreamRoomRole.audienceMic.toString() {
        roomRole = "audience"
      } else if role == NELiveStreamRoomRole.host.toString() {
        roomRole = "host"
      }

      self.roomContext!.changeMemberRole(userUuid: userUuid, role: roomRole) { [weak self] code, msg, _ in
        guard let self = self else { return }
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully change Member Role.")

          // 上麦观众 或主播
          if role == NELiveStreamRoomRole.audienceMic.toString() || role == NELiveStreamRoomRole.host.toString() {
            self.roomContext?.rtcController.setClientRole(.broadcaster)
            self.roomContext?.rtcController.setParameters(["kNERtcKeyAutoSubscribeVideo": true])
            var timestamp = Date().timeIntervalSince1970
            self.roomContext?.rtcController.joinRtcChannel { code, msg, _ in
              timestamp = Date().timeIntervalSince1970
              NELiveStreamLog.infoLog(kitTag, desc: "joinRtcChannel callback Timestamp: \(timestamp)")

              // 默认开启视频
              self.roomContext?.rtcController.enableLocalVideo(enable: true)

              callback?(code, msg, nil)
            }
          } else {
            NELiveStreamLog.infoLog(kitTag, desc: "leaveRtcChannel")
            // leavechannel 默认成功，code 不为0 可以忽略
            self.roomContext?.rtcController.leaveRtcChannel { code, msg, _ in
              callback?(0, msg, nil)
            }
          }

        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to change Member Role. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 离开房间
  /// - Parameter callback: 回调
  func leaveRoom(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Leave room.")

    // 初始化、roomContext 判断
    Judge.preCondition({
      // 主动调用离开聊天室
      self.roomContext!.chatController.leaveChatroom()
      self.roomContext!.leaveRoom { [weak self] code, msg, _ in
        guard let self = self else { return }
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully leave room.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to leave room. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        self.reset()
        // 销毁
        //          self.audioPlayService?.destroy()
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  internal func reset() {
    // 移除 房间监听、消息监听
    roomContext?.removeRoomListener(listener: self)
    // 移除麦位监听
    roomContext?.seatController.removeSeatListener(self)
    roomContext = nil
    liveInfo = nil
  }

  /// 结束房间
  /// - Parameter callback: 回调
  func endRoom(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "End room.")
    Judge.initCondition({
      guard let liveRecordId = self.liveInfo?.liveModel?.liveRecordId else {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to end room. LiveRecordId don't exist."
        )
        callback?(
          NELiveStreamErrorCode.failed,
          "Failed to end room. LiveRecordId don't exist.",
          nil
        )
        return
      }
      self.roomContext?.endRoom(isForce: true)
      self.roomService.endRoom(liveRecordId) {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully end room.")
        callback?(NELiveStreamErrorCode.success, nil, nil)
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to end room. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
      self.reset()
    }, failure: callback)
  }

  /// 查询直播间观众列表
  /// - Parameters:
  ///   - liveRecordId: 直播记录编号
  ///   - pageNum: 页码，默认1
  ///   - pageSize: 页大小，默认50
  ///   - callback: 回调
  func getAudienceList(_ liveRecordId: Int64,
                       pageNum: Int = 1,
                       pageSize: Int = 50,
                       callback: NELiveStreamCallback<NELiveStreamAudienceList>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Get audience list.")

    Judge.initCondition({
      self.roomService.getAudienceList(
        liveRecordId,
        pageNum: pageNum,
        pageSize: pageSize
      ) { list in
        NELiveStreamLog.successLog(kitTag, desc: "Successfully get audience list.")
        callback?(NELiveStreamErrorCode.success, nil, NELiveStreamAudienceList(list))
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to get audience list. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 查询直播间前用户未结束的直播
  /// - callback: 回调
  func getOngoingLive(_ callback: NELiveStreamCallback<NELiveStreamRoomInfo>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "get ongoing Live.")
    Judge.initCondition({
      self.roomService.getOngoingLive { info in
        NELiveStreamLog.successLog(
          kitTag,
          desc: "Successfully get ongoing Live."
        )
        callback?(NELiveStreamErrorCode.success, nil, NELiveStreamRoomInfo(liveInfo: info))
      }
      failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to get ongoing Live. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  ///
  /// - Parameters:
  ///   - liveRecordId: 直播记录编号
  ///   - callback: 回调
  func pauseLive(_ liveRecordId: Int64,
                 notifyMessage: String,
                 callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "pause Live.")

    Judge.initCondition({
      self.roomService.pauseLive(liveRecordId, notifyMessage: notifyMessage) {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully pause Live.")
        callback?(NELiveStreamErrorCode.success, "pause Live.", nil)
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to pause Live. Code: \(error.code). Msg: \(error.localizedDescription)"
        )

        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  ///
  /// - Parameters:
  ///   - liveRecordId: 直播记录编号
  ///   - callback: 回调
  func resumeLive(_ liveRecordId: Int64,
                  notifyMessage: String,
                  callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "resume Live.")

    Judge.initCondition({
      self.roomService.resumeLive(liveRecordId, notifyMessage: notifyMessage) {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully resume Live.")
        callback?(NELiveStreamErrorCode.success, "Successfully resume Live.", nil)
      } failure: { error in
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to resume Live. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }
}

extension NELiveStreamKit {
  func _joinRoom(_ context: NERoomContext,
                 role: String,
                 isRejoin: Bool,
                 callback: NELiveStreamCallback<AnyObject>? = nil) {
    context.rtcController.setClientRole(.audience)

    let group = DispatchGroup()

    var rtcCode: Int?
    var rtcMsg: String?
    var chatCode: Int?
    var chatMsg: String?
    var seatCode = 0
    var seatMsg: String?

    // 加入rtc
    context.rtcController.setClientRole(.broadcaster)
    context.rtcController.enableLocalVideo(enable: true)
    context.rtcController.setParameters(["kNERtcKeyAutoSubscribeVideo": true])

    var timestamp = Date().timeIntervalSince1970

    if role == NELiveStreamRoomRole.host.toString() {
      group.enter()
      NELiveStreamLog.infoLog(kitTag, desc: "joinRtcChannel Timestamp: \(timestamp)")
      context.rtcController.joinRtcChannel { code, msg, _ in
        timestamp = Date().timeIntervalSince1970
        NELiveStreamLog.infoLog(kitTag, desc: "joinRtcChannel callback Timestamp: \(timestamp)")
        rtcCode = code
        rtcMsg = msg

        // 默认开启音频视频
        context.rtcController.enableLocalVideo(enable: true)
        context.rtcController.enableLocalAudio(channelName: context.roomUuid, enable: true)
        context.rtcController.unmuteMyAudio()

        group.leave()
      }
    }

    // 加入聊天室
    group.enter()
    timestamp = Date().timeIntervalSince1970
    NELiveStreamLog.infoLog(kitTag, desc: "joinChatroom Timestamp: \(timestamp)")
    context.chatController.joinChatroom { code, msg, _ in
      timestamp = Date().timeIntervalSince1970
      NELiveStreamLog.infoLog(kitTag, desc: "joinChatroom callback Timestamp: \(timestamp)")
      chatCode = code
      chatMsg = msg

      // 如果是主播，默认上麦,需要放到聊天室后
      if chatCode == 0, role == NELiveStreamRoomRole.host.toString() {
        timestamp = Date().timeIntervalSince1970
        self.requestSeat { code, msg, _ in
          seatCode = code
          seatMsg = msg
          NELiveStreamLog.infoLog(kitTag, desc: "submitSeatRequest callback Timestamp: \(timestamp)")
          group.leave()
        }
      } else {
        group.leave()
      }
    }

    group.notify(queue: .main) {
      timestamp = Date().timeIntervalSince1970
      NELiveStreamLog.infoLog(kitTag, desc: "joinRoom notify Timestamp: \(timestamp)")
      let isOwner = role == NELiveStreamRoomRole.host.toString()
      // seatcode 1306 表示已经在麦上不用管
      if seatCode != 0, seatCode != 1306 {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to submit seat. Code: \(seatCode). Msg: \(seatMsg ?? "")"
        )
        isOwner ? self.endRoom() : context.leaveRoom()
        callback?(seatCode, seatMsg, nil)
      } else if let rtcCode = rtcCode, rtcCode != 0 {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to join rtc. Code: \(rtcCode). Msg: \(rtcMsg ?? "")"
        )
        // 加入rtc 失败，离开房间
        isOwner ? self.endRoom() : context.leaveRoom()
        callback?(rtcCode, rtcMsg, nil)
      } else if let chatCode = chatCode, chatCode != 0 {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to join chatroom. Code: \(chatCode). Msg: \(chatMsg ?? "")"
        )
        // 加入聊天室失败，离开房间
        isOwner ? self.endRoom() : context.leaveRoom()
        callback?(chatCode, chatMsg, nil)
      } else {
        NELiveStreamLog.successLog(kitTag, desc: "Successfully join room.")
        callback?(0, nil, nil)
      }
    }
  }

  func _joinRoom(_ roomUuid: String,
                 userName: String,
                 role: String,
                 isRejoin: Bool,
                 callback: NELiveStreamCallback<AnyObject>? = nil) {
    // 进入房间
    let joinParams = NEJoinRoomParams()
    joinParams.roomUuid = roomUuid
    joinParams.userName = userName
    joinParams.role = role
    let joinOptions = NEJoinRoomOptions()
    joinOptions.enableMyAudioDeviceOnJoinRtc = true

    // 观众通过 observer
    if role == NELiveStreamRoomRole.audience.toString() {
      joinParams.role = NERoomBuiltinRole.Observer
    }

    NERoomKit.shared().roomService.joinRoom(params: joinParams,
                                            options: joinOptions) { [weak self] joinCode, joinMsg, context in
      guard let self = self else { return }
      guard let context = context else {
        NELiveStreamLog.errorLog(
          kitTag,
          desc: "Failed to join room. Code: \(joinCode). Msg: \(joinMsg ?? "")"
        )
        callback?(joinCode, joinMsg, nil)
        return
      }
      self.localSeats = nil
      self.roomContext = context
      self.roomContext?.rtcController.setParameters([NERoomRtcParameters.kNERoomRtcKeyRecordAudioEnabled: true, NERoomRtcParameters.kNERoomRtcKeyRecordVideoEnabled: true])
      context.addRoomListener(listener: self)
      context.seatController.addSeatListener(self)
      // 加入chatroom、rtc
      self._joinRoom(context, role: role, isRejoin: isRejoin, callback: callback)
    }
  }
}
