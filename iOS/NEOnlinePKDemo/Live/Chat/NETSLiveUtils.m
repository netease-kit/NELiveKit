//
//  NETSLiveUtils.m
//  NLiteAVDemo
//
//  Created by Ease on 2020/12/9.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import "NETSLiveUtils.h"

@implementation NETSLiveUtils

/// 默认赠送礼物
+ (NSArray <NETSGiftModel *> *)defaultGifts
{
    NETSGiftModel *gift1 = [[NETSGiftModel alloc] initWithGiftId:1 icon:@"gift03_ico" display:NSLocalizedString(@"荧光棒", nil) price:9];
    NETSGiftModel *gift2 = [[NETSGiftModel alloc] initWithGiftId:2 icon:@"gift04_ico" display:NSLocalizedString(@"安排", nil) price:99];
    NETSGiftModel *gift3 = [[NETSGiftModel alloc] initWithGiftId:3 icon:@"gift02_ico" display:NSLocalizedString(@"跑车", nil) price:199];
    NETSGiftModel *gift4 = [[NETSGiftModel alloc] initWithGiftId:4 icon:@"gift01_ico" display:NSLocalizedString(@"火箭", nil) price:999];
    
    return @[gift1, gift2, gift3, gift4];
}

+ (nullable NETSGiftModel *)getRewardWithGiftId:(NSInteger)giftId
{
    NETSGiftModel *gift = nil;
    for (NETSGiftModel *tmp in [NETSLiveUtils defaultGifts]) {
        if (tmp.giftId == giftId) {
            gift = tmp;
            break;
        }
    }
    return gift;
}

+ (nullable NSDictionary *)gitInfo
{
    NSDictionary *infoDict = [[NSBundle mainBundle] infoDictionary];
    NSString *gitSHA = [infoDict objectForKey:@"GitCommitSHA"];
    NSString *gitBranch = [infoDict objectForKey:@"GitCommitBranch"];
    NSString *gitCommitUser = [infoDict objectForKey:@"GitCommitUser"];
    NSString *gitCommitDate = [infoDict objectForKey:@"GitCommitDate"];
    
    NSDictionary *gitDict = @{
        @"gitSHA" : gitSHA ?: @"nil",
        @"gitBranch" : gitBranch ?: @"nil",
        @"gitCommitUser" : gitCommitUser ?: @"nil",
        @"gitCommitDate" : gitCommitDate ?: @"nil"
    };
    return gitDict;
}

@end
