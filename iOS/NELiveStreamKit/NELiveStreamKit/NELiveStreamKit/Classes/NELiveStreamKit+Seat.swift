//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

public extension NELiveStreamKit {
  /// 申请上麦
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - exclusive: 是否占用麦位
  ///   - callback: 回调
  func submitSeatRequest(_ seatIndex: Int,
                         exclusive: Bool = true,
                         callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Submit seat request. SeatIndex: \(seatIndex).")
    Judge.preCondition({
      self.roomContext!.seatController
        .submitSeatRequest(seatIndex, exclusive: exclusive) { code, msg, _ in
          if code == 0 {
            NELiveStreamLog.successLog(kitTag, desc: "Successfully submit seat request.")
          } else {
            NELiveStreamLog.errorLog(
              kitTag,
              desc: "Failed to submit seat request. Code: \(code). Msg: \(msg ?? "")"
            )
          }
          callback?(code, msg, nil)
        }
    }, failure: callback)
  }

  /// 申请上麦
  /// - Parameter callback: 回调
  func requestSeat(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Apply on seat.")
    Judge.preCondition({
      self.roomContext!.seatController.submitSeatRequest { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully apply on seat.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to apply on seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 获取麦位信息
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameter callback: 回调
  func getSeatInfo(_ callback: NELiveStreamCallback<NELiveStreamSeatInfo>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Get seat info.")

    Judge.preCondition({
      self.roomContext!.seatController.getSeatInfo { code, msg, info in
        if code == 0 {
          guard let info = info else {
            NELiveStreamLog.errorLog(
              kitTag,
              desc: "Failed to get seat info. Data structure error."
            )
            callback?(
              NELiveStreamErrorCode.failed,
              "Failed to get seat info. Data structure error.",
              nil
            )
            return
          }
          NELiveStreamLog.successLog(kitTag, desc: "Successfully get seat info.")
          callback?(NELiveStreamErrorCode.success, nil, NELiveStreamSeatInfo(info))
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to get seat info. Code: \(code). Msg: \(msg ?? "")"
          )
          callback?(code, msg, nil)
        }
      }
    }, failure: callback)
  }

  /// 获取麦位申请列表。按照申请时间正序排序，先申请的成员排在列表前面
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameter callback: 回调
  func getSeatRequestList(_ callback: NELiveStreamCallback<[NELiveStreamSeatRequestItem]>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Get seat request list.")

    Judge.preCondition({
      self.roomContext!.seatController.getSeatRequestList { code, msg, items in
        if code == 0 {
          guard let items = items else {
            NELiveStreamLog.errorLog(
              kitTag,
              desc: "Failed to get seat request list. Data structure error."
            )
            callback?(
              NELiveStreamErrorCode.failed,
              "Failed to get seat request list. Data structure error.",
              nil
            )
            return
          }
          let requestItems = items.map { NELiveStreamSeatRequestItem($0) }
          NELiveStreamLog.successLog(kitTag, desc: "Successfully get seat request list.")
          callback?(NELiveStreamErrorCode.success, nil, requestItems)
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to get seat request list. Code: \(code). Msg: \(msg ?? "")"
          )
          callback?(code, msg, nil)
        }
      }
    }, failure: callback)
  }

  /// 获取麦位邀请列表。按照邀请时间正序排序，先邀请的成员排在列表前面
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameter callback: 回调
  func getSeatInvitationList(_ callback: NELiveStreamCallback<[NELiveStreamSeatInvitationItem]>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Get seat invitation list.")

    Judge.preCondition({
      self.roomContext!.seatController.getSeatInvitationList { code, msg, items in
        if code == 0 {
          guard let items = items else {
            NELiveStreamLog.errorLog(
              kitTag,
              desc: "Failed to get seat invitation list. Data structure error."
            )
            callback?(NELiveStreamErrorCode.failed, "Failed to get seat invitation list. Data structure error.", nil)
            return
          }
          let invitationItems = items.map { NELiveStreamSeatInvitationItem($0) }
          NELiveStreamLog.successLog(kitTag, desc: "Successfully get seat invitation list.")
          callback?(NELiveStreamErrorCode.success, nil, invitationItems)
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to get seat invitation list. Code: \(code). Msg: \(msg ?? "")"
          )
          callback?(code, msg, nil)
        }
      }
    }, failure: callback)
  }

  /// 房主向成员[user]发送上麦邀请，指定位置为[seatIndex]，非管理员执行该操作会失败。
  /// - Parameters:
  ///   - account: 用户Id
  ///   - callback: 回调
  func sendSeatInvitation(account: String,
                          callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(
      kitTag,
      desc: "Send seat invitation.  User: \(account)"
    )

    Judge.preCondition({
      self.roomContext!.seatController.sendSeatInvitation(account) { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully send seat invitation.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to send seat invitation. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 取消申请上麦
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameter callback: 回调
  func cancelSeatRequest(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Cancel seat request.")

    Judge.preCondition({
      self.roomContext!.seatController.cancelSeatRequest { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully cancel seat request.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to cancel seat request. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 同意上麦
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameters:
  ///   - account: 被同意上麦的用户account
  ///   - callback: 回调
  func approveSeatRequest(account: String, callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Approve seat request. Account: \(account).")

    Judge.preCondition({
      self.roomContext!.seatController.approveSeatRequest(account) { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully approve seat request.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to approve seat request. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 拒绝上麦
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameters:
  ///   - account: 被拒绝上麦的用户account
  ///   - callback: 回调
  func rejectSeatRequest(account: String, callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Reject seat request. Account: \(account)")
    Judge.preCondition({
      self.roomContext!.seatController.rejectSeatRequest(account) { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully reject seat request.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to reject seat request. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 踢麦
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameters:
  ///   - account: 被踢用户的account
  ///   - callback: 回调
  func kickSeat(account: String, callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Kick seat. Account: \(account)")

    Judge.preCondition({
      self.roomContext!.seatController.kickSeat(account) { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully kick seat.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to kick seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  func acceptSeatInvitation(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Accept seat invitation")

    Judge.preCondition({
      self.roomContext!.seatController.acceptSeatInvitation { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully accept seat invitation.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to accept seat invitation. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  func rejectSeatInvitation(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Reject seat invitation.")

    Judge.preCondition({
      self.roomContext!.seatController.rejectSeatInvitation { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully reject seat invitation.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to reject SeatInvitation. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 下麦
  ///
  /// 使用前提：该方法仅在调用[login]方法登录成功后调用有效
  /// - Parameter callback: 回调
  func leaveSeat(_ callback: NELiveStreamCallback<AnyObject>? = nil) {
    NELiveStreamLog.apiLog(kitTag, desc: "Leave seat.")
    Judge.preCondition({
      self.roomContext!.seatController.leaveSeat { code, msg, _ in
        if code == 0 {
          NELiveStreamLog.successLog(kitTag, desc: "Successfully leave seat.")
        } else {
          NELiveStreamLog.errorLog(
            kitTag,
            desc: "Failed to leave seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }
}

extension NELiveStreamKit: NESeatEventListener {}
