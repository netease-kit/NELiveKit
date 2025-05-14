// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NELiveStreamConnectDefine.h"
#import "NELiveStreamGlobalMacro.h"

NS_ASSUME_NONNULL_BEGIN

@protocol NELiveStreamMutiConnectViewDelegate <NSObject>

// 断开连麦
- (void)disconnectRoomWithUserId:(NSString *)userId;

@end

@interface NELiveStreamMutiConnectView : UIView

/// 构造函数
/// @param dataArray collectionview的数据源
- (instancetype)initWithDataSource:(NSArray *)dataArray frame:(CGRect)frame;

@property(nonatomic, weak) id<NELiveStreamMutiConnectViewDelegate> delegate;

// 角色类型
@property(nonatomic, assign) NTESUserMode roleType;

/// 刷新本地数据状态
/// @param updateDataArray 更新的数据
- (void)reloadDataSource:(NSArray<NELiveStreamSeatItem *> *)updateDataArray;

/// 获取视频渲染 view
/// @param userId 用户id
- (UIView *)getVideoView:(NSString *)userId;
@end

NS_ASSUME_NONNULL_END
