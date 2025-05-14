//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#ifndef NELiveStreamConnectDefine_h
#define NELiveStreamConnectDefine_h

// 角色枚举
typedef NS_ENUM(NSUInteger, NTESUserMode) {
  // 主播
  NTESUserModeAnchor = 0,
  // 观众
  NTESUserModeAudience = 1,
  // 连麦者
  NTESUserModeConnector = 2,
};

#endif /* NELiveStreamConnectDefine_h */
