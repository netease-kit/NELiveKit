//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NELiveStreamKit

extension NELiveAnchorViewController: NEliveStreamMicListDelegate {
  func didRequestSubmitMicData() -> [NELiveStreamSeatItem] {
    // 将申请列表转换为麦位项
    let applySeatItems = (applySeatItems ?? []).map { request in
      let seatItem = NELiveStreamSeatItem()
      seatItem.user = request.user
      seatItem.userName = request.userName
      seatItem.icon = request.icon
      seatItem.status = .waiting
      return seatItem
    }
    return applySeatItems
  }

  func didRequestMicManagerData() -> [NELiveStreamSeatItem] {
    let takenSeats = NELiveStreamKit.getInstance().localSeats?.filter {
      $0.status == .taken && $0.user != liveInfo?.anchor?.userUuid
    } ?? []

    return takenSeats
  }

  func didRequestInviteMicData() -> [NELiveStreamSeatItem] {
    // 从 audienceNumView 获取缓存的观众列表
    let audienceList = audienceNumView.getAudienceList()

    // 将观众列表转换为麦位项
    let audienceSeatItems = audienceList.map { audience in
      let seatItem = NELiveStreamSeatItem()
      seatItem.user = audience.userUuid
      seatItem.userName = audience.nickName
      seatItem.icon = audience.icon
      seatItem.status = .initial
      return seatItem
    }
    return audienceSeatItems
  }

  func onAccept(with seatItem: NELiveStreamSeatItem!) {
    guard let user = seatItem.user else {
      NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed to approve seat request, user is null.")
      return
    }

    guard status != .coHosting else {
      NELiveStreamToast.show("当前正在PK中，无法进行连麦, 将拒绝连麦申请")
      onReject(with: seatItem)
      return
    }

    NELiveStreamKit.getInstance().approveSeatRequest(account: user) { [weak self] code, msg, _ in
      DispatchQueue.main.async { [weak self] in
        if code == 0 {
          NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully approve seat request.")
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag,
                                     desc: "Failed to approve seat request. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(msg ?? "")
        }

        self?.updateSeatRequestList()
      }
    }
  }

  func onReject(with seatItem: NELiveStreamSeatItem!) {
    guard let user = seatItem.user else {
      NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed to reject seat request, user is null.")
      return
    }

    NELiveStreamKit.getInstance().rejectSeatRequest(account: user) { [weak self] code, msg, _ in
      DispatchQueue.main.async { [weak self] in
        if code == 0 {
          NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully reject seat request.")
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag,
                                     desc: "Failed to reject seat request. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(msg ?? "")
        }
        self?.updateSeatRequestList()
      }
    }
  }

  func onKick(with seatItem: NELiveStreamSeatItem!) {
    guard let user = seatItem.user else {
      NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed to kick seat request, user is null.")
      return
    }

    NELiveStreamKit.getInstance().kickSeat(account: user) { [weak self] code, msg, _ in
      DispatchQueue.main.async { [weak self] in
        if code == 0 {
          NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully kick seat request.")
          NELiveStreamToast.show("已踢下麦")
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag,
                                     desc: "Failed to kick seat request. Code: \(code). Msg: \(msg ?? "")")

          NELiveStreamToast.show(msg ?? "")
        }

        self?.updateSeatRequestList()
      }
    }
  }

  func onInvite(with seatItem: NELiveStreamSeatItem!) {
    guard let user = seatItem.user else {
      NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed to invite seat request, user is null.")
      return
    }

    guard status != .coHosting else {
      NELiveStreamToast.show("当前正在PK中，无法进行连麦")
      return
    }

    NELiveStreamKit.getInstance().sendSeatInvitation(account: user) { [weak self] code, msg, _ in
      DispatchQueue.main.async { [weak self] in
        if code == 0 {
          NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully invite seat request.")
          NELiveStreamToast.show("连麦邀请已发送")
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag,
                                     desc: "Failed to invite seat request. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(msg ?? "")
        }

        self?.updateSeatRequestList()
      }
    }
  }
}

extension NELiveAnchorViewController {
  func onUsersJoin(_ seatItems: [NELiveStreamSeatItem]) {
    // 处理新上麦的观众
    for newSeat in seatItems {
      if let userId = newSeat.user {
        // 为新上麦的观众设置视频视图
        let videoView = mutiConnectView.getVideoView(userId)
        // 设置远程视频视图
        NELiveStreamKit.getInstance().setRemoteVideoCanvas(view: videoView, userUuid: userId)
        NELiveStreamKit.getInstance().subscribeRemoteVideoStream(userUuid: userId)
      }
    }
  }

  func onUsersLeave(_ seatItems: [NELiveStreamSeatItem]) {
    // 处理离开的观众
    for leftSeat in seatItems {
      if let userId = leftSeat.user {
        // 移除离开观众的远程视频
        NELiveStreamKit.getInstance().setRemoteVideoCanvas(view: nil, userUuid: userId)
      }
    }
  }
}

extension NELiveAnchorViewController {
  func onSeatListChanged(_ seatItems: [NELiveStreamSeatItem]) {
    // 更新本地麦位列表
    let oldSeatItems = NELiveStreamKit.getInstance().localSeats ?? []

    // 检查是否有观众上麦
    let hasConnectedAudience = seatItems.contains { $0.status == .taken }

    // 更新连麦视图的显示状态
    UIView.animate(withDuration: 0.3) {
      self.mutiConnectView.isHidden = !hasConnectedAudience
    }

    // 过滤掉主播自己，只获取麦上的观众
    let audienceOnSeat = seatItems.filter {
      $0.status == .taken && $0.user != liveInfo?.anchor?.userUuid
    }

    // 重新加载数据源
    mutiConnectView.reloadDataSource(audienceOnSeat)

    // 找出新上麦的观众
    let newAudience = audienceOnSeat.filter { newSeat in
      !oldSeatItems.contains { oldSeat in
        oldSeat.user == newSeat.user && oldSeat.status == .taken
      }
    }

    // 找出离开的观众
    let leftAudience = oldSeatItems.filter { oldSeat in
      oldSeat.status == .taken && oldSeat.user != liveInfo?.anchor?.userUuid &&
        !seatItems.contains { newSeat in
          newSeat.user == oldSeat.user && newSeat.status == .taken
        }
    }

    // 处理上麦
    onUsersJoin(newAudience)

    // 处理下麦
    onUsersLeave(leftAudience)

    // 刷新UI
    DispatchQueue.main.async {
      self.updateSeatViews()
    }
  }

  // 上麦申请
  func onSeatRequestSubmitted(_ seatIndex: Int, account: String) {
    updateSeatRequestList()
    // 显示红点
    footerView.showLinkMicRedDot(true)
  }

  // 上麦申请取消
  func onSeatRequestCancelled(_ seatIndex: Int, account: String) {
    updateSeatRequestList()
  }

  private func updateSeatViews() {
    micVc.refreshData()
  }

  func updateSeatRequestList() {
    NELiveStreamKit.getInstance().getSeatRequestList { [weak self] code, msg, list in
      DispatchQueue.main.async { [weak self] in
        if code == 0 {
          self?.applySeatItems = list
          self?.micVc.refreshData()
          // 根据申请列表是否为空来显示/隐藏红点
          self?.footerView.showLinkMicRedDot(!(list?.isEmpty ?? true))
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed to get seat request list. Code: \(code). Msg: \(msg ?? "")")
          NELiveStreamToast.show(msg ?? "")
        }
      }
    }
  }

  // 对方已拒绝你的连麦邀请
  func onSeatInvitationRejected(_ seatIndex: Int, account: String) {
    NELiveStreamToast.show("对方已拒绝你的连麦邀请")
  }

  func onMemberRoleChanged(_ member: NELiveStreamMember, oldRole: NELiveStreamRoomRole, newRole: NELiveStreamRoomRole) {
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "onMemberRoleChanged: \(member.account) oldRole: \(oldRole) newRole: \(newRole)")
  }
}

extension NELiveAnchorViewController: NELiveStreamMutiConnectViewDelegate {
  func disconnectRoom(withUserId userId: String) {}
}
