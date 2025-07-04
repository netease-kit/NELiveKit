//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NELiveStreamKit

// MARK: - PK 相关回调处理

extension NELiveAnchorViewController: NECoHostListener {
  func onConnectionUserListChanged(connectedList: [NEConnectionUser], joinedList: [NEConnectionUser], leavedList: [NEConnectionUser]) {
    // 目前只有两人PK
    for user in joinedList {
      if user.userUuid != NELiveStreamKit.getInstance().localMember?.account {
        NELiveStreamKit.getInstance().setRemoteVideoView(view: pkUIView.remoteRenderView, userUuid: user.userUuid)
      }

      // 更新 PKUI 布局
      layoutPKUI()
    }

    if !leavedList.isEmpty {
      // 如果其他人离开，断开连接
      guard let roomUuid = leavedList.first?.roomUuid, let userUuid = leavedList.first?.userUuid else {
        NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed disconnect pk: roomUuid or userUuid is nil")
        return
      }
      NELiveStreamKit.getInstance().coHostManager.disconnect(roomUuid: roomUuid)
      NELiveStreamKit.getInstance().setRemoteVideoView(view: nil, userUuid: userUuid)

      // 恢复单人直播布局
      layoutSingleUI()
    }
  }

  func onConnectionRequestReceived(inviter: NEConnectionUser, inviteeList: [NEConnectionUser], ext: String?) {
    showPKInvite(inviter: inviter)
  }

  func onConnectionRequestCancelled(inviter: NEConnectionUser) {
    currentPKInviteView?.dismiss(animated: true)
    currentPKInviteView = nil
  }

  func onConnectionRequestReject(invitee: NEConnectionUser) {
    NELiveStreamToast.show("PK邀请被拒绝")
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "on PK Request Reject")
  }

  func onConnectionRequestTimeout(inviter: NEConnectionUser, inviteeList: [NEConnectionUser]) {
    guard inviter.userUuid == NELiveStreamKit.getInstance().localMember?.account else {
      NELiveStreamUILog.errorLog(anchorControllerTag, desc: "on PK Request Timeout,is not me so ignore")
      return
    }

    NELiveStreamToast.show("PK 邀请超时")
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "on PK Request Timeout")
  }
}

extension NELiveAnchorViewController: NELiveStreamPKInviteViewControllerDelegate {
  func didAcceptPKInvitation(inviter: NEConnectionUser) {
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "Did accept pk invitation, inviter: \(inviter.userUuid)")

    guard let roomUuid = inviter.roomUuid else {
      NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed accept pk invitation: roomUuid is nil")
      return
    }

    NELiveStreamKit.getInstance().coHostManager.accept(roomUuid: roomUuid) { code, msg, _ in
      if code == 0 {
        NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully accept pk invitation .")
      } else {
        NELiveStreamUILog.errorLog(anchorControllerTag,
                                   desc: "Failed accept pk invitation. Code: \(code). Msg: \(msg ?? "")")
        NELiveStreamToast.show(msg ?? "")
      }
    }
  }

  func didRejectPKInvitation(inviter: NEConnectionUser) {
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "Did reject pk invitation, inviter: \(inviter.userUuid)")

    guard let roomUuid = inviter.roomUuid else {
      NELiveStreamUILog.errorLog(anchorControllerTag, desc: "Failed reject pk invitation: roomUuid is nil")
      return
    }

    NELiveStreamKit.getInstance().coHostManager.reject(roomUuid: roomUuid) { code, msg, _ in
      if code == 0 {
        NELiveStreamUILog.successLog(anchorControllerTag, desc: "Successfully reject pk invitation .")
      } else {
        NELiveStreamUILog.errorLog(anchorControllerTag,
                                   desc: "Failed reject pk invitation. Code: \(code). Msg: \(msg ?? "")")
        NELiveStreamToast.show(msg ?? "")
      }
    }
  }
}

extension NELiveAnchorViewController: NELiveStreamPKUIViewDelegate {
  func pkCountdownDidFinish(_ pkUIView: NELiveStreamPKUIView) {
    // 这里处理PK倒计时结束后的业务逻辑
    NELiveStreamUILog.infoLog(anchorControllerTag, desc: "PK countdown finished,  End PK logic.")
    // 实现结束PK逻辑
    if let roomUuid = NELiveStreamKit.getInstance().coHostManager.coHostUserList.first?.roomUuid {
      NELiveStreamKit.getInstance().coHostManager.disconnect(roomUuid: roomUuid)
      layoutSingleUI()
    }
  }
}

extension NELiveAnchorViewController {
  private func showPKInvite(inviter: NEConnectionUser) {
    let pkInviteView = NELiveStreamPKInviteViewController(inviter: inviter)
    pkInviteView.delegate = self
    currentPKInviteView = pkInviteView
    dismiss(animated: true) {
      self.present(pkInviteView, animated: true)
    }
  }
}
