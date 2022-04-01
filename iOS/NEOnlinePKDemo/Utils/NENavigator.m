//
//  NENavigator.m
//  NLiteAVDemo
//
//  Created by Think on 2020/8/28.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import "NETSLiveListViewController.h"
#import "NETSAudienceViewController.h"
#import "NEPkLiveViewController.h"
#import "NETabbarController.h"

@interface NENavigator ()

@end

@implementation NENavigator

+ (NENavigator *)shared
{
    static NENavigator *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[NENavigator alloc] init];
    });
    return instance;
}

- (void)setUpRootWindowCtrl {
    NETabbarController *tabBarVc = [[NETabbarController alloc]init];
    [NENavigator shared].navigationController = tabBarVc.menuNavController;
    [UIApplication sharedApplication].keyWindow.rootViewController = tabBarVc;
}

- (void)showLiveListVC {
    NETSLiveListViewController *vc = [[NETSLiveListViewController alloc] init];
    vc.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)showAnchorVC {
        NEPkLiveViewController *ctrl = [[NEPkLiveViewController alloc] init];
        ctrl.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:ctrl animated:YES];
}

- (void)showLivingRoom:(NSArray<NELiveDetail *> *)roomDataList withIndex:(int)roomIndex
{
    NETSAudienceViewController *vc = [[NETSAudienceViewController alloc] initWithScrollData:roomDataList currentRoom:roomIndex];
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)showRootNavWitnIndex:(NSInteger)index
{
    UITabBarController *tab = (UITabBarController *)[UIApplication sharedApplication].delegate.window.rootViewController;
    if (index >= [tab.viewControllers count]) {
//        YXAlogInfo(@"索引越界");
    }
    for (UIViewController *vc in tab.viewControllers) {
        if (![vc isKindOfClass:[UINavigationController class]]) {
            continue;
        }
        UINavigationController *nav = (UINavigationController *)vc;
        [nav popToRootViewControllerAnimated:NO];
    }
    
    [tab setSelectedIndex:index];
    [UIApplication sharedApplication].delegate.window.rootViewController = tab;
    UINavigationController *nav = tab.viewControllers[index];
    self.navigationController = nav;
}

@end
