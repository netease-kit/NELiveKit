//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

struct NEAPIItem: NEAPIProtocol {
  let urlPath: String
  var url: String { NE.config.baseUrl + urlPath }
  let description: String
  let extra: String?
  var method: NEHttpMethod
  init(_ url: String,
       desc: String,
       method: NEHttpMethod = .post,
       extra: String? = nil) {
    urlPath = url
    self.method = method
    description = desc
    self.extra = extra
  }
}

enum NEAPI {
  // 房间模块
  enum Room {
    static let prePath = "/nemo/entertainmentLive/live"
    static let create = NEAPIItem("\(prePath)/createLiveV3", desc: "创建房间")
    static let roomList = NEAPIItem("\(prePath)/list", desc: "获取房间列表")
    static let destroy = NEAPIItem("\(prePath)/destroyLive", desc: "结束房间")
    static let batchReward = NEAPIItem("\(prePath)/batch/reward", desc: "批量打赏功能")
    static let info = NEAPIItem("\(prePath)/info", desc: "获取房间详情")
    static let auth = NEAPIItem("/nemo/entertainmentLive/real-name-authentication", desc: "实名认证")
    static let liveInfo = NEAPIItem("\(prePath)/getDefaultLiveInfo", desc: "获取直播主题及背景图", method: .get)
    static let pauseLive = NEAPIItem("\(prePath)/pauseLive", desc: "暂停直播")
    static let resumeLive = NEAPIItem("\(prePath)/resumeLive", desc: "恢复直播")
    static let ongoingLive = NEAPIItem("\(prePath)/ongoing", desc: "查询当前用户未结束的直播", method: .get)

    static func audienceList(_ liveRecordId: Int64, page: Int, size: Int) -> NEAPIItem {
      NEAPIItem("\(prePath)/audience/list?liveRecordId=\(liveRecordId)&page=\(page)&size=\(size)", desc: "查询直播间观众列表", method: .get)
    }

    static let coHostList = NEAPIItem("\(prePath)/available_connection_list", desc: "获取PK列表")
  }

  enum CoLive {
    static let prePath = "/nemo/entertainmentLive/live"
    static let requestHostConnection = NEAPIItem("\(prePath)/request_connection", desc: "申请主播连线")
    static let cancelRequestHostConnection = NEAPIItem("\(prePath)/cancel_connection", desc: "取消主播连线申请")
    static let acceptRequestHostConnection = NEAPIItem("\(prePath)/accept_connection", desc: "接受主播连线")
    static let rejectRequestHostConnection = NEAPIItem("\(prePath)/reject_connection", desc: "拒绝主播连线")
    static let disconnectHostConnection = NEAPIItem("\(prePath)/disconnect_connection", desc: "结束主播连线")
  }
}
