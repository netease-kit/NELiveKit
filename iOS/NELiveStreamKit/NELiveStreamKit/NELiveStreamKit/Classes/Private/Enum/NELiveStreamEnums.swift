// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 连麦请求类型
/// @Private
enum NELiveCoHostRequestType: Int {
  /// unkown
  case unknown = -1
  /// 请求主播连麦
  case request = 10001
  /// 取消请求主播连麦
  case cancel = 10002
  /// 同意请求主播连麦
  case accept = 10003
  /// 拒绝请求主播连麦
  case reject = 10004
  /// 退出主播连麦
  case exit = 10005
  /// 主播请求连麦超时
  case timeout = 10008
}
