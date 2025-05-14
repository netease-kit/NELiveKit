// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// SDK API的通用回调接口。SDK提供的接口多为异步实现，在调用这些接口时，需要提供一个该接口的实现作为回调参数
@objc
public protocol NELiveStreamListener: NSObjectProtocol {
  /// 成员加入聊天室回调
  /// - Parameter members: 成员信息列表
  ///
  /// 添加监听后，有成员加入聊天室会触发此方法
  @objc
  optional func onMemberJoinChatroom(_ members: [NELiveStreamMember])

  @objc
  optional func onMemberLeaveChatroom(_ members: [NELiveStreamMember])

  /// 成员进入房间回调
  /// - Parameter members: 成员列表
  @objc optional func onMemberJoinRoom(_ members: [NELiveStreamMember])

  /// 成员离开房间回调
  /// - Parameter members: 成员列表
  @objc optional func onMemberLeaveRoom(_ members: [NELiveStreamMember])

  /// 房间结束回调
  /// - Parameter reason: 房间结束原因
  @objc optional func onRoomEnded(_ reason: NELiveStreamEndReason)

  /// Rtc频道错误回调
  /// - Parameter code: 错误码
  @objc optional func onRtcChannelError(_ code: Int)

  /// 提示房间内谁正在说话及说话这瞬时音量的回调，该回调默认为关闭状态
  ///
  /// 开启后 无论房间内是否有人说话，SDK 都会按设置的时间间隔触发该回调。
  /// - 如果有 [NELiveStreamMemberVolumeInfo.userUuid] 出现在上次返回的列表中，但不在本次返回的列表中，则默认该 userId 对应的远端用户没有说话。
  /// - 如果 [NELiveStreamMemberVolumeInfo.volume] 为 0，表示该用户没有说话。
  /// - 如果列表为空，则表示此时远端没有人说话。
  /// - Parameters:
  ///   - volumes: 每个说话者的用户ID和音量信息列表
  ///   - totalVolume: 混合后的总音量，取值范围为 0~100
  @objc optional func onRtcRemoteAudioVolumeIndication(volumes: [NELiveStreamMemberVolumeInfo],
                                                       totalVolume: Int)

  /// 本端瞬时音量回调及是否检测到人声
  /// - Parameter volume: 音量
  ///             enableVad : 是否检测到人声。
  @objc optional func onRtcLocalAudioVolumeIndication(volume: Int, enableVad: Bool)

  /// 本端音频输出设备变更通知，如切换到扬声器、听筒、耳机等
  /// - Parameter device: 音频输出类型
  @objc optional func onAudioOutputDeviceChanged(_ device: NELiveStreamAudioOutputDevice)

  /// 音效播放进度更新
  /// - Parameters:
  ///   - effectId: 音效Id
  ///   - timeStampMS: 播放进度
  @objc optional func onAudioEffectTimestampUpdate(_ effectId: UInt32, timeStampMS: UInt64)

  /// 本地音乐文件播放结束
  @objc optional func onAudioEffectFinished()

  /// 成员音频关闭回调
  /// - Parameters:
  ///   - member: 成员信息
  ///   - mute: 是否关闭
  ///   - operateBy: 操作者信息
  ///
  ///   添加监听后，成员音频状态变更会触发此方法
  @objc optional func onMemberAudioMuteChanged(_ member: NELiveStreamMember,
                                               mute: Bool,
                                               operateBy: NELiveStreamMember?)

  /// 聊天室消息回调
  /// - Parameter message: 文本消息
  ///
  /// 添加监听后，收到聊天室消息会触发此方法
  @objc optional func onReceiveTextMessage(_ message: NELiveStreamChatTextMessage)

  /// 接收到发送批量礼物的回调
  /// - Parameter giftModel: 礼物模型
  @objc optional func onReceiveBatchGift(giftModel: NELiveStreamBatchGiftModel)

  ///  直播暂停
  @objc optional func onLivePause()

  ///  直播恢复
  @objc optional func onLiveResume()

  /// 成员音频禁用事件回调
  /// - Parameters:
  ///   - member: 成员
  ///   - banned: 是否被禁用音频
  @objc optional func onMemberAudioBanned(_ member: NELiveStreamMember, banned: Bool)

  /// 直播状态变更回调
  /// - Parameter state: 直播状态
  @objc optional func onLiveStateChanged(_ state: NELiveStreamState)

  /// PK 状态变更回调
  /// - Parameter state: PK 状态
  @objc optional func onPKStateChanged(_ state: NELiveStreamPKState)

  /// 直播画质变更回调
  /// - Parameter quality: 画质信息
  @objc optional func onLiveQualityChanged(_ quality: NELiveStreamQuality)

  /// 成员[user]提交了位置为[seatIndex]的麦位申请
  /// - Parameters:
  ///   - seatIndex: 麦位位置，**-1**表示未指定位置
  ///   - account: 申请人的用户ID
  @objc optional func onSeatRequestSubmitted(_ seatIndex: Int, account: String)

  /// 成员[user]取消了位置为[seatIndex]的麦位申请
  /// - Parameters:
  ///   - seatIndex: 麦位位置，**-1**表示未指定位置
  ///   - account: 申请人的用户ID
  @objc optional func onSeatRequestCancelled(_ seatIndex: Int, account: String)

  /// 管理员通过了成员[user]的麦位申请，位置为[seatIndex]
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - account: 申请人的用户ID
  ///   - operateBy: 同意该申请的用户ID
  @objc optional func onSeatRequestApproved(_ seatIndex: Int, account: String, operateBy: String,
                                            isAutoAgree: Bool)

  /// 管理员拒绝了成员[user]的麦位申请，位置为[seatIndex]
  /// - Parameters:
  ///   - seatIndex: 麦位位置，**-1**表示未指定位置
  ///   - account: 申请人的用户ID
  ///   - operateBy: 拒绝该申请的用户ID
  @objc optional func onSeatRequestRejected(_ seatIndex: Int, account: String, operateBy: String)

  /// 成员下麦，位置为[seatIndex]
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - account: 下麦成员
  @objc optional func onSeatLeave(_ seatIndex: Int, account: String)

  /// 成员[user]被[operateBy]从位置为[seatIndex]的麦位踢掉
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - account: 成员
  ///   - operateBy: 操作人
  @objc optional func onSeatKicked(_ seatIndex: Int, account: String, operateBy: String)

  /// 当前成员收到了来自[inviter]的上麦邀请，位置为[seatIndex]。
  /// - Parameters:
  ///   - seatIndex 麦位位置，**-1**表示未指定位置
  ///   - account:  邀请人。
  ///   - operateBy:  操作人
  ///
  @objc optional func onSeatInvitationReceived(_ seatIndex: Int, account: String, operateBy: String)

  ///
  /// [inviter]取消了对当前成员的上麦邀请，位置为[seatIndex]。
  /// - Parameters:
  ///   - seatIndex 麦位位置，**-1**表示未指定位置。
  ///   - user  邀请人。
  ///   - operateBy 操作人
  ///
  @objc optional func onSeatInvitationCancelled(_ seatIndex: Int, account: String, operateBy: String)

  ///  成员接受了位置为[seatIndex]的上麦邀请。
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - account: 成员
  @objc optional func onSeatInvitationAccepted(_ seatIndex: Int, account: String,
                                               isAutoAgree: Bool)

  ///
  ///  成员拒绝了位置为[seatIndex]的上麦邀请。
  /// - Parameters:
  ///   - seatIndex 麦位位置，**-1**表示未指定位置
  ///   - account  被邀请人。
  ///
  @objc optional func onSeatInvitationRejected(_ seatIndex: Int, account: String)

  /// 麦位变更通知
  /// - Parameter seatItems: 麦位列表
  @objc optional func onSeatListChanged(_ seatItems: [NELiveStreamSeatItem])

  /// 自己的麦位状态发生变更，只处理未上麦、申请中、已上麦
  @objc optional func onSelfSeatStatusChanged(new: NELiveStreamSeatItemStatus, old: NELiveStreamSeatItemStatus)
}

/// 直播状态
@objc
public enum NELiveStreamState: Int {
  /// 未开始
  case notStart = 0
  /// 直播中
  case living = 1
  /// 已结束
  case ended = 2
}

/// PK 状态
@objc
public enum NELiveStreamPKState: Int {
  /// 空闲
  case idle = 0
  /// PK 中
  case inPK = 1
  /// PK 暂停
  case pause = 2
}

/// 直播画质
@objc
public class NELiveStreamQuality: NSObject {
  /// 视频帧率
  @objc public var fps: Int = 0
  /// 视频码率
  @objc public var videoBitrate: Int = 0
  /// 音频码率
  @objc public var audioBitrate: Int = 0
  /// 网络延迟
  @objc public var rtt: Int = 0
  /// 丢包率
  @objc public var packetLossRate: Float = 0.0
}
