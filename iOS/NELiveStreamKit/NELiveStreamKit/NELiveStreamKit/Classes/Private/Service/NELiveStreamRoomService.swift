//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

class NELiveStreamRoomService {
  /// 获取房间列表
  /// - Parameters:
  ///   - type: 房间类型，默认为2：ChatRoom
  ///   - liveType: 房间类型
  ///   - pageNum: 每页数量
  ///   - pageSize: 页号
  ///   - callback: 回调
  ///  liveType类型：
  func getRoomList(_ type: Int = 2,
                   pageNum: Int,
                   pageSize: Int,
                   success: ((NELiveStreamRoomList?) -> Void)? = nil,
                   failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "pageNum": pageNum,
      "pageSize": pageSize,
      "liveType": type,
    ]
    NEAPI.Room.roomList.request(params,
                                returnType: _NELiveStreamRoomListResponse.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      let roomList = NELiveStreamRoomList(data)
      success?(roomList)
    } failed: { error in
      failure?(error)
    }
  }

  /// 获取房间信息
  /// - Parameters:
  ///   - liveRecordId: 房间ID
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func getRoomInfo(_ liveRecordId: Int,
                   success: ((_NECreateLiveResponse?) -> Void)? = nil,
                   failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "liveRecordId": liveRecordId,
    ]
    NEAPI.Room.info.request(params, returnType: _NECreateLiveResponse.self,
                            success: { data in
                              guard let data = data else {
                                success?(nil)
                                return
                              }
                              success?(data)
                            }, failed: failure)
  }

  func getDefaultLiveInfo(success: ((_NECreateRoomDefaultInfo?) -> Void)? = nil,
                          failure: ((NSError) -> Void)? = nil) {
    NEAPI.Room.liveInfo.request(
      returnType: _NECreateRoomDefaultInfo.self,
      success: { defaultInfo in
        guard let data = defaultInfo else {
          success?(nil)
          return
        }
        success?(data)
      },
      failed: failure
    )
  }

  func authenticate(name: String,
                    cardNo: String,
                    success: (() -> Void)? = nil,
                    failure: ((NSError) -> Void)? = nil) {
    let param: [String: String] = [
      "name": name,
      "cardNo": cardNo,
    ]
    NEAPI.Room.auth.request(param) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  /// 创建房间
  /// - Parameters:
  ///   - params: 创建房间参数
  ///   - isDebug: 是否为debug模式
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func startLiveStreamRoom(_ params: NECreateLiveStreamRoomParams,
                           success: ((_NECreateLiveResponse?) -> Void)? = nil,
                           failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "liveType": params.liveType.rawValue,
      "liveTopic": params.liveTopic ?? "",
      "cover": params.cover ?? "",
      "configId": params.configId,
      "roomName": params.roomName ?? "",
      "seatCount": params.seatCount,
      "seatApplyMode": params.seatApplyMode.rawValue,
      "seatInviteMode": params.seatInviteMode,
      "roomProfile": NERoomProfile.liveBroadcasting.rawValue, // 直播模式
    ]
    NEAPI.Room.create.request(param,
                              returnType: _NECreateLiveResponse.self) { resp in
      success?(resp)
    } failed: { error in
      failure?(error)
    }
  }

  /// 结束房间
  /// - Parameters:
  ///   - liveRecordId: 直播记录编号
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func endRoom(_ liveRecordId: Int,
               success: (() -> Void)? = nil,
               failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "liveRecordId": liveRecordId,
    ]
    NEAPI.Room.destroy.request(param,
                               success: { _ in
                                 success?()
                               }, failed: failure)
  }

  /// 批量礼物打赏
  /// - Parameters:
  ///   - liveRecordId: 直播编号
  ///   - giftId: 礼物id
  ///   - giftCount: 礼物个数
  ///   - userUuids: 打赏给主播或者麦上观众
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func batchReward(_ liveRecordId: Int,
                   giftId: Int,
                   giftCount: Int,
                   userUuids: [String],
                   success: (() -> Void)? = nil,
                   failure: ((NSError) -> Void)? = nil) {
    let param = [
      "liveRecordId": liveRecordId,
      "giftId": giftId,
      "giftCount": giftCount,
      "targets": userUuids,
    ] as [String: Any]
    NEAPI.Room.batchReward.request(param, success: { _ in
      success?()
    }, failed: failure)
  }

  /// 查询直播间观众列表
  /// - Parameters:
  ///   - liveRecordId: 直播记录编号
  ///   - pageNum: 页码
  ///   - pageSize: 页大小
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func getAudienceList(_ liveRecordId: Int64,
                       pageNum: Int,
                       pageSize: Int,
                       success: ((_NELiveStreamAudienceListResponse?) -> Void)? = nil,
                       failure: ((NSError) -> Void)? = nil) {
    NEAPI.Room.audienceList(liveRecordId, page: pageNum, size: pageSize).request(returnType: _NELiveStreamAudienceListResponse.self) { resp in
      success?(resp)
    } failed: { error in
      failure?(error)
    }
  }

  /// 查询直播间前用户未结束的直播
  /// - callback: 回调
  func getOngoingLive(_ success: ((_NELiveStreamRoomInfoResponse?) -> Void)? = nil,
                      failure: ((NSError) -> Void)? = nil) {
    NEAPI.Room.ongoingLive.request(returnType: _NELiveStreamRoomInfoResponse.self) { resp in
      success?(resp)
    } failed: { error in
      failure?(error)
    }
  }

  func pauseLive(_ liveRecordId: Int64,
                 notifyMessage: String?,
                 success: (() -> Void)? = nil,
                 failure: ((NSError) -> Void)? = nil) {
    let param = [
      "liveRecordId": liveRecordId,
      "notifyMessage": notifyMessage ?? "",
    ] as [String: Any]

    NEAPI.Room.pauseLive.request(param, success: { _ in
      success?()
    }, failed: failure)
  }

  func resumeLive(_ liveRecordId: Int64,
                  notifyMessage: String?,
                  success: (() -> Void)? = nil,
                  failure: ((NSError) -> Void)? = nil) {
    let param = [
      "liveRecordId": liveRecordId,
      "notifyMessage": notifyMessage ?? "",
    ] as [String: Any]

    NEAPI.Room.resumeLive.request(param, success: { _ in
      success?()
    }, failed: failure)
  }

  func getCoLiveRooms(_ pageNum: Int,
                      pageSize: Int,
                      success: ((_NELiveStreamRoomListResponse?) -> Void)? = nil,
                      failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "pageNum": pageNum,
      "pageSize": pageSize,
      "liveType": NELiveStreamLiveRoomType.liveStream.rawValue,
      "liveStatus": [1],
    ]
    NEAPI.Room.coHostList.request(params, returnType: _NELiveStreamRoomListResponse.self) { resp in
      success?(resp)
    } failed: { error in
      failure?(error)
    }
  }
}
