// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
public enum NELiveStreamRole: Int {
  case audience = 0 // 观众
  case host = 1 // 主播
  case manager = 2 // 管理员
}
