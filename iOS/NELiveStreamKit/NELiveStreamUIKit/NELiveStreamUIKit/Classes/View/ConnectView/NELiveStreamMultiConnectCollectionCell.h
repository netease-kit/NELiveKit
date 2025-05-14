// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NELiveStreamConnectDefine.h"
#import "NELiveStreamGlobalMacro.h"

NS_ASSUME_NONNULL_BEGIN

@class NETSConnectMicMemberModel;
@protocol NELiveStreamMultiConnectCollectionDelegate <NSObject>

/// 退出连麦
- (void)didCloseConnectRoom:(NSString *)userId;

@end

@interface NELiveStreamMultiConnectCollectionCell : UICollectionViewCell
/**
 注册Cell

 @param collectionView 注册的表格视图对象
 */
+ (void)registerForCollectionView:(UICollectionView *)collectionView;

/**
 获取注册的Cell

 @param collectionView 表格视图对象
 @param indexPath indexPath
 @return 注册的Cell
 */
+ (instancetype)settingCellWithCollectionView:(UICollectionView *)collectionView
                                    indexPath:(NSIndexPath *)indexPath;

@property(nonatomic, weak) id<NELiveStreamMultiConnectCollectionDelegate> delegate;

@property(nonatomic, strong) NELiveStreamSeatItem *memberModel;
// 角色类型
@property(nonatomic, assign) NTESUserMode roleType;

@property(nonatomic, copy) UIView * (^getVideoViewBlock)(NSString *userId);

@end

NS_ASSUME_NONNULL_END
