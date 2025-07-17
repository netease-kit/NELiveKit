//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 上麦模式
@objc public enum NELiveStreamSeatApplyMode: Int {
  /// 自由上麦模式
  case free = 0
  /// 管理员审批上麦模式
  case managerApproval = 1
}

/// 创建直播房房间参数
@objcMembers
public class NECreateLiveStreamRoomParams: NSObject {
  /// 直播类型
  public var liveType: NELiveStreamLiveRoomType = .liveStream
  /// 直播主题
  public var liveTopic: String?
  /// 直播封面图
  public var cover: String?
  /// 模版 ID, 获取方法请参考(https://doc.yunxin.163.com/neroom/concept/DQzNTEwMDE?platform=client#%E8%8E%B7%E5%8F%96%E7%BB%84%E4%BB%B6%E6%A8%A1%E6%9D%BF-id)
  public var configId: Int = 0
  /// 麦位数量。如果设置为大于**0**的值，则会在创建的房间中启用麦位管理，最大20
  public var seatCount: Int = 4

  /// 麦位模式，0：自由模式，1：管理员控制模式，不传默认为自由模式
  /// 0申请上麦时不需要管理员同意，直接上麦；1:申请上麦时需要管理员同意
  public var seatApplyMode: NELiveStreamSeatApplyMode = .managerApproval
  /// 默认0）0抱麦时不需要对方同意；1:抱麦时需要对方同意
  var seatInviteMode: Int = 1
}
