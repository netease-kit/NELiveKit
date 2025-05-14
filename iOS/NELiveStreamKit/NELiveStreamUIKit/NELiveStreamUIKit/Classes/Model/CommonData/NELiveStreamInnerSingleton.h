// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <NELiveStreamKit/NELiveStreamKit-Swift.h>

NS_ASSUME_NONNULL_BEGIN

/// 内部单例
@interface NELiveStreamInnerSingleton : NSObject
/// 房间信息
@property(nonatomic, strong, nullable) NELiveStreamRoomInfo *roomInfo;
/// 单例初始化
+ (instancetype)sharedInstance;

/// 获取 去除 主播麦位的 麦位列表
- (NSArray<NELiveStreamSeatItem *> *)fetchAudienceSeatItems:
    (NSArray<NELiveStreamSeatItem *> *)seatItems;
/// 获取主播麦位信息
- (NELiveStreamSeatItem *_Nullable)fetchAnchorItem:(NSArray<NELiveStreamSeatItem *> *)seatItems;
@end

NS_ASSUME_NONNULL_END
