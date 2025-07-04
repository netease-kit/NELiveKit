//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers
public class NELiveStreamRoomInfo: NSObject {
  /// 主播信息
  public var anchor: NELiveStreamRoomAnchor?
  /// 直播信息
  public var liveModel: NELiveStreamRoomLiveModel?

  init(create: _NECreateLiveResponse?) {
    if let create = create {
      anchor = NELiveStreamRoomAnchor(create.anchor)
      liveModel = NELiveStreamRoomLiveModel(create.live)
    }
  }

  init(liveInfo: _NELiveStreamRoomInfoResponse?) {
    if let info = liveInfo {
      anchor = NELiveStreamRoomAnchor(info.anchor)
      liveModel = NELiveStreamRoomLiveModel(info.live)
    }
  }
}

/// 主播信息
///
@objcMembers
public class NELiveStreamRoomAnchor: NSObject {
  /// 用户编号
  public var userUuid: String?
  /// 房间用户编号
  public var rtcUid: Int = 0
  /// 昵称
  public var userName: String?
  /// 头像地址
  public var icon: String?
  init(_ anchor: _NECreateLiveAnchor?) {
    if let anchor = anchor {
      userUuid = anchor.userUuid
      userName = anchor.userName
      icon = anchor.icon
      rtcUid = anchor.rtcUid ?? 0
    }
  }
}

/// 直播信息
@objcMembers
public class NELiveStreamLiveConfig: NSObject, Codable {
  /// 推流地址
  public var pushUrl: String?
  /// hls拉流地址
  public var pullHlsUrl: String?
  /// rtmp拉流地址
  public var pullRtmpUrl: String?
  /// http拉流地址
  public var pullHttpUrl: String?
}

/// 直播信息
@objcMembers

public class NELiveStreamRoomLiveModel: NSObject {
  /// 直播记录编号
  public var liveRecordId: Int = 0
  /// 房间号
  public var roomUuid: String?
  //
  public var roomId: String?
  /// 创建人账号
  public var userUuid: String?
  /// 直播类型
  public var liveType: NELiveStreamLiveRoomType = .liveStream
  /// 直播记录是否有效 1: 有效 -1 无效
  public var status: Int = 1
  /// 直播状态
  public var live: NELiveStreamLiveStatus = .idle
  /// 直播主题
  public var liveTopic: String?
  /// 背景图地址
  public var cover: String?
  /// 打赏总额
  public var rewardTotal: Int = 0
  /// 观众人数
  public var audienceCount: Int = 0
  /// 麦位人数
  public var onSeatCount: Int = 0
  /// 打赏信息
//  public var seatUserReward: [NELiveStreamRoomBatchSeatUserReward]?
  /// 房间名称
  public var roomName: String?
  /// 当前在玩的游戏
  public var gameName: String?

  public var externalLiveConfig: NELiveStreamLiveConfig?

  public var connectionStatus: Int = -1

  init(_ live: _NECreateLiveLive?) {
    if let live = live {
      roomUuid = live.roomUuid
      userUuid = live.userUuid
      liveRecordId = live.liveRecordId ?? 0
      liveType = .liveStream
      status = live.status ?? 1
      liveTopic = live.liveTopic
      cover = live.cover
      rewardTotal = live.rewardTotal ?? 0
      audienceCount = live.audienceCount ?? 0
      self.live = NELiveStreamLiveStatus(rawValue: Int(live.live ?? 0)) ?? .idle
      onSeatCount = live.onSeatCount ?? 0
      roomName = live.roomName
      if let infoSeatUserReward = live.seatUserReward {
//        seatUserReward = infoSeatUserReward.map { NELiveStreamRoomBatchSeatUserReward($0) }
      }
      gameName = live.gameName
      externalLiveConfig = live.externalLiveConfig
      connectionStatus = live.connectionStatus ?? -1
    }
  }
}

/// 直播房 房间列表
@objcMembers

public class NELiveStreamRoomList: NSObject {
  /// 数据列表
  public var list: [NELiveStreamRoomInfo]?
  /// 当前页
  public var pageNum: Int = 0
  /// 是否有下一页
  public var hasNextPage: Bool = false
  init(_ list: _NELiveStreamRoomListResponse?) {
    if let list = list {
      pageNum = list.pageNum ?? 1
      hasNextPage = list.hasNextPage
      if let details = list.list {
        self.list = details.compactMap { detail in
          NELiveStreamRoomInfo(liveInfo: detail)
        }
      }
    }
  }
}

// MARK: Private

// MARK: 直播列表

/// 直播信息
@objcMembers

class _NELiveStreamRoomListResponse: NSObject, Codable {
  var pageNum: Int?
  var pageSize: Int?
  var size: Int?
  var startRow: Int?
  var endRow: Int?
  var pages: Int?
  var prePage: Int?
  var nextPage: Int?
  var isFirstPage: Bool?
  var isLastPage: Bool?
  var hasPreviousPage: Bool = false
  var hasNextPage: Bool = false
  var navigatePages: Int?
  var navigatepageNums: [Int]?
  var navigateFirstPage: Int?
  var navigateLastPage: Int?
  var total: Int?
  var list: [_NELiveStreamRoomInfoResponse]?
}

@objcMembers
class _NELiveStreamRoomInfoResponse: NSObject, Codable {
  var anchor: _NECreateLiveAnchor?
  var live: _NECreateLiveLive?
}

@objcMembers
class _NECreateLiveResponse: NSObject, Codable {
  var anchor: _NECreateLiveAnchor?
  var live: _NECreateLiveLive?
}

// MARK: 开播

@objcMembers
class _NECreateLiveAnchor: NSObject, Codable {
  /// 用户编号
  var userUuid: String?
  /// 房间用户编号
  var rtcUid: Int?
  /// 昵称
  var userName: String?
  /// 头像地址
  var icon: String?
}

/// 创建房间所需的主题与背景图片
@objcMembers
class _NECreateRoomDefaultInfo: NSObject, Codable {
  /// 房间主题
  var topic: String?
  /// 默认背景图
  var livePicture: String?
  /// 可选背景图列表
  var defaultPictures: [String]?
}

/// 直播信息
@objcMembers
class _NECreateLiveLive: NSObject, Codable {
  /// 直播记录编号
  var liveRecordId: Int?
  /// 房间号
  var roomUuid: String?
  /// 创建人账号
  var userUuid: String?
  /// 直播类型
  var liveType: Int?
  /// 直播记录是否有效 1: 有效 -1 无效
  var status: Int?
  /// 直播状态，0.未开始，1.直播中，2.PK中 3. 惩罚中  4.连麦中  5.等待PK中  6.直播结束
  var live: Int?
  /// 直播主题
  var liveTopic: String?
  /// 背景图地址
  var cover: String?
  /// 打赏总额
  var rewardTotal: Int?
  /// 观众人数
  var audienceCount: Int?
  /// 麦位人数
  var onSeatCount: Int?
  /// 房间名称
  var roomName: String?
  /// 打赏信息
  var seatUserReward: [_NELiveStreamRoomBatchSeatUserReward]?
  /// 当前在玩的游戏
  var gameName: String?
  /// 直播频道配置信息
  var externalLiveConfig: NELiveStreamLiveConfig?
  /// 主播连麦状态 (0:空闲, 1:申请中, 2:已接受, 3:已拒绝, 4:已取消, 6:已断开, 7:已连线, 8:已超时)
  var connectionStatus: Int?
}

@objcMembers
class _NELiveStreamRoomBatchSeatUserReward: NSObject, Codable {
  var seatIndex: Int
  var userUuid: String?
  var userName: String?
  var rewardTotal: Int
  var icon: String?
}

@objcMembers
class _NELiveStreamAudienceListResponse: NSObject, Codable {
  var total: Int?
  var list: [_NELiveStreamAudienceResponse]?
}

@objcMembers
class _NELiveStreamAudienceResponse: NSObject, Codable {
  var userUuid: String?
  var userName: String?
  var icon: String?
}

@objc
public class NELiveStreamAudienceList: NSObject {
  /// 总数
  @objc public private(set) var total: Int = 0
  /// 观众列表
  @objc public private(set) var list: [NELiveStreamAudience] = []

  override public init() {
    super.init()
  }

  init(_ response: _NELiveStreamAudienceListResponse?) {
    total = response?.total ?? 0
    if let audiences = response?.list {
      list = audiences.map { NELiveStreamAudience($0) }
    }
  }
}

@objc
public class NELiveStreamAudience: NSObject {
  /// 观众Id
  @objc public private(set) var userUuid: String = ""
  /// 昵称
  @objc public private(set) var nickName: String = ""
  /// 头像地址
  @objc public private(set) var icon: String = ""

  override public init() {
    super.init()
  }

  init(_ response: _NELiveStreamAudienceResponse?) {
    super.init()
    if let response = response {
      userUuid = response.userUuid ?? ""
      nickName = response.userName ?? ""
      icon = response.icon ?? ""
    }
  }
}
