// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 批量礼物消息模型
@objcMembers
public class NELiveStreamBatchGiftModel: NSObject {
  /// 发送者账号
  public var sendAccount: String = ""

  public var rewarderUserUuid: String = ""
  /// 发送者昵称
  public var rewarderUserName: String = ""
  /// 礼物ID
  public var giftId: Int
  /// 礼物数量
  public var giftCount: Int

  /// 麦上主播或者观众打赏信息
  public var seatUserReward: [NELiveStreamBatchSeatUserReward]
  ///
  public var rewardeeUsers: [NELiveStreamBatchSeatUserRewardee]

  init(_ rewardMsg: _NELiveStreamBatchRewardMessage) {
    sendAccount = rewardMsg.senderUserUuid ?? ""
    giftId = rewardMsg.giftId ?? 0
    giftCount = rewardMsg.giftCount ?? 0
    rewarderUserName = rewardMsg.userName ?? ""
    rewarderUserUuid = rewardMsg.userUuid ?? ""
    seatUserReward = rewardMsg.seatUserReward.map { NELiveStreamBatchSeatUserReward($0) }
    rewardeeUsers = rewardMsg.targets.map { NELiveStreamBatchSeatUserRewardee($0) }
  }
}

@objcMembers
public class NELiveStreamBatchSeatUserReward: NSObject {
  public var seatIndex: Int = 0
  public var userUuid: String?
  public var userName: String?
  public var rewardTotal: Int = 0
  public var icon: String?

  init(_ batchSeatUserReward: _NELiveStreamBatchSeatUserReward?) {
    if let batchSeatUserReward = batchSeatUserReward {
      seatIndex = batchSeatUserReward.seatIndex
      userUuid = batchSeatUserReward.userUuid
      userName = batchSeatUserReward.userName
      rewardTotal = batchSeatUserReward.rewardTotal
      icon = batchSeatUserReward.icon
    }
  }
}

@objcMembers
public class NELiveStreamBatchSeatUserRewardee: NSObject {
  public var userUuid: String?
  public var userName: String?
  public var icon: String?

  init(_ batchSeatUserReward: _NELiveStreamBatchSeatUserRewardee?) {
    if let batchSeatUserRewardee = batchSeatUserReward {
      userUuid = batchSeatUserRewardee.userUuid
      userName = batchSeatUserRewardee.userName
      icon = batchSeatUserRewardee.icon
    }
  }
}

@objcMembers
class _NELiveStreamBatchRewardMessage: NSObject, Codable {
  /// 消息发送者用户编号
  var senderUserUuid: String?
  /// 发送消息时间
  var sendTime: Int?
  /// 打赏者昵称
  var userName: String?
  /// 打赏者id
  var userUuid: String?
  /// 礼物编号
  var giftId: Int?
  /// 礼物个数
  var giftCount: Int?
  /// 麦上所有人的被打赏信息
  var seatUserReward: [_NELiveStreamBatchSeatUserReward]
  /// 被打赏者信息列表
  var targets: [_NELiveStreamBatchSeatUserRewardee]
}

class _NELiveStreamBatchSeatUserReward: NSObject, Codable {
  var seatIndex: Int
  var userUuid: String?
  var userName: String?
  var rewardTotal: Int
  var icon: String?
}

class _NELiveStreamBatchSeatUserRewardee: NSObject, Codable {
  var userUuid: String?
  var userName: String?
  var icon: String?
}
