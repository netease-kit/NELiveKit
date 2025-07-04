//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
public protocol NECoHostListener: NSObjectProtocol {
  /// 收到连线用户列表发生变化
  /// - Parameters:
  ///   - connectedList: 已连线的用户列表
  ///   - joinedList: 新加入连线的用户列表
  ///   - leavedList: 退出连线的用户列表
  @objc optional func onConnectionUserListChanged(connectedList: [NEConnectionUser], joinedList: [NEConnectionUser], leavedList: [NEConnectionUser])

  /// 接收端收到连线邀请的回调
  /// - Parameters:
  ///   - inviter: 邀请者信息
  ///   - inviteeList: 被邀请连线的用户列表
  ///   - ext: 透传信息
  @objc optional func onConnectionRequestReceived(inviter: NEConnectionUser, inviteeList: [NEConnectionUser], ext: String?)

  /// 邀请取消回调
  /// - Parameter inviter: 邀请者信息
  @objc optional func onConnectionRequestCancelled(inviter: NEConnectionUser)

  /// 邀请被接受回调
  /// - Parameter invitee: 被邀请者的用户信息
  @objc optional func onConnectionRequestAccept(invitee: NEConnectionUser)

  /// 邀请被拒绝回调
  /// - Parameter invitee: 被邀请者的用户信息
  @objc optional func onConnectionRequestReject(invitee: NEConnectionUser)

  /// 邀请超时回调
  /// - Parameters:
  ///   - inviter: 邀请者信息
  ///   - inviteeList: 被邀请者的用户信息
  @objc optional func onConnectionRequestTimeout(inviter: NEConnectionUser, inviteeList: [NEConnectionUser])
}
