// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

func convertToLiveStreamRole(_ role: NERoomRole?) -> NELiveStreamRoomRole {
  guard let name = role?.name else {
    return .audience // 默认返回 audience
  }
  switch name {
  case "host":
    return .host
  case "audience":
    return .audienceMic
  case NERoomBuiltinRole.Observer:
    return .audience
  default:
    return .audience // 未知类型默认 audience
  }
}
