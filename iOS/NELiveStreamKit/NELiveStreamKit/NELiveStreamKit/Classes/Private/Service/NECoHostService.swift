//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 连麦相关服务
class NECoHostService {
  /// 申请主播连线
  /// - Parameters:
  ///   - toRoomIds: 房间ID列表
  ///   - timeout: 邀请超时时间，单位秒
  ///   - ext: 扩展参数
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func requestHostConnection(toRoomIds: [String],
                             timeout: Int,
                             ext: String?,
                             success: ((_NECoHostRequestConnectionResponse?) -> Void)? = nil,
                             failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "roomUuids": toRoomIds,
      "timeout": timeout,
      "ext": ext ?? "",
    ]
    NEAPI.CoLive.requestHostConnection.request(params, returnType: _NECoHostRequestConnectionResponse.self, success: { response in
      mainAsyncSafe {
        success?(response)
      }
    }, failed: { error in
      mainAsyncSafe {
        failure?(error)
      }
    })
  }

  /// 取消主播连线申请
  /// - Parameters:
  ///   - toRoomIds: 房间ID列表
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func cancelRequestHostConnection(toRoomIds: [String],
                                   success: (() -> Void)? = nil,
                                   failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "roomUuids": toRoomIds,
    ]
    NEAPI.CoLive.cancelRequestHostConnection.request(params, success: { _ in
      mainAsyncSafe {
        success?()
      }
    }, failed: { error in
      mainAsyncSafe {
        failure?(error)
      }
    })
  }

  /// 接受主播连线
  /// - Parameters:
  ///   - toRoomId: 房间ID
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func acceptRequestHostConnection(toRoomId: String,
                                   success: (() -> Void)? = nil,
                                   failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "roomUuid": toRoomId,
    ]
    NEAPI.CoLive.acceptRequestHostConnection.request(params, success: { _ in
      mainAsyncSafe {
        success?()
      }
    }, failed: { error in
      mainAsyncSafe {
        failure?(error)
      }
    })
  }

  /// 拒绝主播连线
  /// - Parameters:
  ///   - toRoomId: 房间ID
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func rejectRequestHostConnection(toRoomId: String,
                                   success: (() -> Void)? = nil,
                                   failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "roomUuid": toRoomId,
    ]
    NEAPI.CoLive.rejectRequestHostConnection.request(params, success: { _ in
      mainAsyncSafe {
        success?()
      }
    }, failed: { error in
      mainAsyncSafe {
        failure?(error)
      }
    })
  }

  /// 结束主播连线
  /// - Parameters:
  ///   - toRoomId: 房间ID
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func disconnectHostConnection(toRoomId: String,
                                success: (() -> Void)? = nil,
                                failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "roomUuid": toRoomId,
    ]
    NEAPI.CoLive.disconnectHostConnection.request(params, success: { _ in
      mainAsyncSafe {
        success?()
      }
    }, failed: { error in
      mainAsyncSafe {
        failure?(error)
      }
    })
  }
}
