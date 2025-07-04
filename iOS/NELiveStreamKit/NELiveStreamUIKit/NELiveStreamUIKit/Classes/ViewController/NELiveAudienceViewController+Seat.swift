//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NELiveStreamKit

extension NELiveAudienceViewController {
  func onUsersJoin(_ seatItems: [NELiveStreamSeatItem]) {
    // 处理新上麦的观众
    for newSeat in seatItems {
      if let userId = newSeat.user {
        // 为新上麦的观众设置视频视图
        let videoView = mutiConnectView.getVideoView(userId)
        // 设置远程视频视图
        NELiveStreamKit.getInstance().setRemoteVideoView(view: videoView, userUuid: userId)
        NELiveStreamKit.getInstance().subscribeRemoteVideoStream(userUuid: userId)
      }
    }
  }

  func onUsersLeave(_ seatItems: [NELiveStreamSeatItem]) {
    // 处理离开的观众
    for leftSeat in seatItems {
      if let userId = leftSeat.user {
        // 移除离开观众的远程视频
        NELiveStreamKit.getInstance().setRemoteVideoView(view: nil, userUuid: userId)

        // 如果是自己离开,默认其他人都下麦
        if userId == NELiveStreamKit.getInstance().localMember?.account {
          leaveSeat()
          localRender.isHidden = true
          localPlayerRender.isHidden = false

          // 数据置空
          mutiConnectView.reloadDataSource([])
          break
        }
      }
    }
  }
}

extension NELiveAudienceViewController {
  func onSeatListChanged(_ seatItems: [NELiveStreamSeatItem]) {
    // 如果是观众角色，直接返回
    guard role != NELiveStreamRoomRole.audience else {
      return
    }

    // 更新本地麦位列表
    let oldSeatItems = self.seatItems ?? []
    self.seatItems = seatItems

    // 检查是否有观众上麦
    let hasConnectedAudience = seatItems.contains { $0.status == .taken }

    // 更新连麦视图的显示状态
    UIView.animate(withDuration: 0.3) {
      self.mutiConnectView.isHidden = !hasConnectedAudience
    }

    // 获取所有上麦的用户，包括主播
    var audienceOnSeat = seatItems.filter { $0.status == .taken }

    // 将主播移到第一个位置
    if let anchorIndex = audienceOnSeat.firstIndex(where: { $0.user == roomInfo.anchor?.userUuid }) {
      let anchor = audienceOnSeat.remove(at: anchorIndex)
      audienceOnSeat.insert(anchor, at: 0)
    }

    // 检查自己是否上麦
    let localUserUuid = NELiveStreamKit.getInstance().localMember?.account
    let isLocalUserOnSeat = audienceOnSeat.contains { $0.user == localUserUuid }

    if isLocalUserOnSeat {
      // 设置本地视频视图
      NELiveStreamKit.getInstance().setLocalVideoView(view: localRender)
      localRender.isHidden = false
      localPlayerRender.isHidden = true

      // dimiss 当前弹窗
      dismiss(animated: false)

      // 从 audienceOnSeat 中移除自己
      if let localIndex = audienceOnSeat.firstIndex(where: { $0.user == localUserUuid }) {
        audienceOnSeat.remove(at: localIndex)
      }
    }

    // 重新加载数据源
    mutiConnectView.reloadDataSource(audienceOnSeat)

    // 找出新上麦的观众
    let newAudience = audienceOnSeat.filter { newSeat in
      newSeat.status == .taken &&
        !oldSeatItems.contains { oldSeat in
          oldSeat.user == newSeat.user && oldSeat.status == .taken
        }
    }

    // 找出离开的观众
    let leftAudience = oldSeatItems.filter { oldSeat in
      oldSeat.status == .taken &&
        !seatItems.contains { newSeat in
          newSeat.user == oldSeat.user && oldSeat.status == .taken
        }
    }

    // 处理上麦
    onUsersJoin(newAudience)

    // 处理下麦
    onUsersLeave(leftAudience)
  }

  // 收到邀请
  func onSeatInvitationReceived(_ seatIndex: Int, account: String, operateBy: String) {
    NELiveStreamUILog.infoLog(audienceControllerTag, desc: "on Seat Invitation Received. account: \(account), operateBy: \(account)")

    guard account == NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.successLog(audienceControllerTag, desc: "on Seat Invitation Receivedd. is not me so ignore")
      return
    }

    // 先关闭已存在的弹框
    currentInviteAlert?.dismiss(animated: false)

    // 从 allMemberList 中查找主播信息
    if let member = NELiveStreamKit.getInstance().allMemberList.first(where: { $0.account == operateBy }) {
      // 创建并显示新的邀请弹框
      let inviteVC = NELiveStreamConnectInviteViewController(
        hostName: member.name,
        hostIcon: member.avatar
      )
      inviteVC.delegate = self
      currentInviteAlert = inviteVC
      dismiss(animated: true)
      present(inviteVC, animated: true)
    } else {
      NELiveStreamUILog.errorLog(audienceControllerTag,
                                 desc: "Failed to find host member info in allMemberList. account: \(operateBy)")
    }
  }

  // 收到邀请取消
  func onSeatInvitationCancelled(_ seatIndex: Int, account: String, operateBy: String) {
    NELiveStreamUILog.successLog(audienceControllerTag, desc: "on Seat Invitation Cancelled. account: \(account), operateBy: \(account)")

    guard account == NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.successLog(audienceControllerTag, desc: "on Seat Invitation Cancelled. is not me so ignore")
      return
    }

    // 关闭当前显示的邀请弹框
    currentInviteAlert?.dismiss(animated: true)
    currentInviteAlert = nil
  }

  // 收到主播同意
  func onSeatRequestApproved(_ seatIndex: Int, account: String, operateBy: String, isAutoAgree: Bool) {
    NELiveStreamUILog.successLog(audienceControllerTag, desc: "on Seat Request Approved. account: \(account), operateBy: \(operateBy)")

    guard account == NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.successLog(audienceControllerTag, desc: "on Seat Request Approved. is not me so ignore")
      return
    }

    takeSeat()
  }

  func onSeatKicked(_ seatIndex: Int, account: String, operateBy: String) {
    guard account == NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.successLog(audienceControllerTag, desc: "on Seat Kicked. is not me so ignore")
      return
    }

    NELiveStreamToast.show("已被踢下麦")
  }

  private func takeSeat() {
    guard let account = NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.successLog(audienceControllerTag, desc: "cant find account, so ignore")
      return
    }

    NELiveStreamKit.getInstance().changeMemberRole(userUuid: account, role: NELiveStreamRoomRole.audienceMic.toString()) { [weak self] code, msg, _ in
      DispatchQueue.main.async { [weak self] in
        if code == 0 {
          self?.role = .audienceMic
          NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully changeMemberRole.")
          self?.stopStream()

        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag,
                                     desc: "Failed to change Member Role. Code: \(code). Msg: \(msg ?? "")")

          NELiveStreamToast.show("上麦失败 : \(msg ?? "")")
        }
      }
    }
  }

  private func leaveSeat() {
    guard let account = NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.successLog(audienceControllerTag, desc: "cant find account, so ignore")
      return
    }

    NELiveStreamKit.getInstance().changeMemberRole(userUuid: account, role: NELiveStreamRoomRole.audience.toString()) { [weak self] code, msg, _ in
      DispatchQueue.main.async { [weak self] in
        if code == 0 {
          NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully changeMemberRole.")
          // changeMemberRole 下麦调用即认为成功
          self?.role = .audience
          self?.seatItems = nil
          self?.startStream()
        } else {
          NELiveStreamUILog.errorLog(anchorControllerTag,
                                     desc: "Failed to change Member Role. Code: \(code). Msg: \(msg ?? "")")
        }
      }
    }
  }

  func onMemberRoleChanged(_ member: NELiveStreamMember, oldRole: NELiveStreamRoomRole, newRole: NELiveStreamRoomRole) {
    guard member.account == NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.successLog(audienceControllerTag, desc: "onMemberRoleChanged. is not me so ignore")
      return
    }

    // 如果变为 audience 也需要切到 audience 布局
    NELiveStreamUILog.infoLog(audienceControllerTag, desc: "onMemberRoleChanged: \(member.account) oldRole: \(oldRole) newRole: \(newRole)")
    if newRole == NELiveStreamRoomRole.audience {
      role = .audience
      seatItems = nil
      startStream()
    }
  }
}

// 添加 NELiveStreamMutiConnectViewDelegate 实现
extension NELiveAudienceViewController: NELiveStreamMutiConnectViewDelegate {
  func disconnectRoom(withUserId userId: String) {}
}

// 添加 NELiveStreamConnectInviteViewControllerDelegate 实现
extension NELiveAudienceViewController: NELiveStreamConnectInviteViewControllerDelegate {
  func didAcceptSeatInvitation() {
    // 处理接受连麦邀请的逻辑
    guard (NELiveStreamKit.getInstance().localMember?.account) != nil else { return }

    NELiveStreamKit.getInstance().acceptSeatInvitation { [weak self] code, msg, _ in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.successLog(audienceControllerTag, desc: "Successfully accept seat invitation.")
          self?.dismiss(animated: true)
          self?.takeSeat()
        } else {
          NELiveStreamUILog.errorLog(audienceControllerTag,
                                     desc: "Failed to accept seat invitation. Code: \(code). Msg: \(msg ?? "")")

          NELiveStreamToast.show("Failed to accept seat invitation. Code: \(code). Msg: \(msg ?? "")")
        }
      }
    }
  }

  func didRejectSeatInvitation() {
    // 处理拒绝连麦邀请的逻辑
    guard (NELiveStreamKit.getInstance().localMember?.account) != nil else { return }

    NELiveStreamKit.getInstance().rejectSeatInvitation { [weak self] code, msg, _ in
      DispatchQueue.main.async {
        if code == 0 {
          NELiveStreamUILog.successLog(audienceControllerTag, desc: "Successfully reject seat invitation.")
          self?.dismiss(animated: true)
        } else {
          NELiveStreamUILog.errorLog(audienceControllerTag,
                                     desc: "Failed to reject seat invitation. Code: \(code). Msg: \(msg ?? "")")

          NELiveStreamToast.show("Failed to reject seat invitation. Code: \(code). Msg: \(msg ?? "")")
        }
      }
    }
  }
}

extension NELiveAudienceViewController: NELiveStreamConnectListViewControllerDelegate {
  // 观众主动下麦
  func didLeaveSeat() {
    let selfSeatItem = NELiveStreamSeatItem()
    selfSeatItem.user = NELiveStreamKit.getInstance().localMember?.account
    selfSeatItem.userName = NELiveStreamKit.getInstance().localMember?.name
    selfSeatItem.icon = NELiveStreamKit.getInstance().localMember?.avatar
    selfSeatItem.status = .initial

    // 自己离开
    onUsersLeave([selfSeatItem])
  }
}
