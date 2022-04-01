//
//  NENavigator.h
//  NLiteAVDemo
//
//  Created by Think on 2020/8/28.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface NENavigator: NSObject

@property (nonatomic, weak) UINavigationController  *navigationController;
@property (nonatomic, weak) UINavigationController  *loginNavigationController;

+ (NENavigator *)shared;

/// 设置根控制器
- (void)setUpRootWindowCtrl;

/// 展示直播列表页
- (void)showLiveListVC;


/// 进入主播直播间
- (void)showAnchorVC;

/**
 进入直播间
 @param roomData 点击时候的数据源
 */
- (void)showLivingRoom:(NSArray<NELiveDetail *> *)roomDataList withIndex:(int)roomIndex;

/**
 回到根tabBar控制器
 @param index   - 根导航控制器索引
 */
- (void)showRootNavWitnIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
