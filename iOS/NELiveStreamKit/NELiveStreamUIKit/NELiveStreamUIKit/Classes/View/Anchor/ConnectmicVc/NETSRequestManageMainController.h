// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamMicListProtocol.h"
#import "NETSBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN
@class NELiveStreamSeatItem;

@interface NETSRequestManageMainController : NETSBaseViewController

/// 构造函数
/// @param roomId 房间id
- (instancetype)initWithRoomId:(NSString *)roomId;

- (void)refreshData;
@end

NS_ASSUME_NONNULL_END
