//
//  NETSLiveListVM.m
//  NLiteAVDemo
//
//  Created by Ease on 2020/11/9.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import "NETSLiveListVM.h"


@interface NETSLiveListVM ()

@property (nonatomic, assign)   int32_t   pageNum;
@property (nonatomic, assign)   int32_t   pageSize;

@end

@implementation NETSLiveListVM

- (instancetype)init
{
    self = [super init];
    if (self) {
        _pageNum = 1;
        _pageSize = 20;
        _datas = @[];
    }
    return self;
}

- (void)load {
    self.pageNum = 1;
    self.isLoading = YES;
    
    [[NELiveKit shared] fetchLiveListWithPageNum:_pageNum pageSize:_pageSize liveStatus:NELiveStatusLiving liveType:NEliveRoomTypePkLive callback:^(NSInteger code, NSString * _Nullable msg, NELiveList * _Nullable list) {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.isLoading = NO;
            if (code == 0) {
                self.datas = list.list;
                self.isEnd = ([list.list count] < self.pageSize);
                self.error = nil;
            } else {
                self.datas = @[];
                self.isEnd = YES;
                self.error = [[NSError alloc] initWithDomain:NSCocoaErrorDomain code:code userInfo:@{NSLocalizedDescriptionKey:msg}];
            }
        });
    }];
}

- (void)loadMore {
    if (_isEnd) {
        return;
    }
    
    self.pageNum += 1;
    self.isLoading = YES;
    
    [[NELiveKit shared] fetchLiveListWithPageNum:_pageNum pageSize:_pageSize liveStatus:NELiveStatusLiving liveType:NEliveRoomTypePkLive callback:^(NSInteger code, NSString * _Nullable msg, NELiveList * _Nullable list) {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.isLoading = NO;
            if (code == 0) {
                self.datas = list.list;
                self.isEnd = ([list.list count] < self.pageSize);
                self.error = nil;
            } else {
                self.datas = @[];
                self.isEnd = YES;
                self.error = [[NSError alloc] initWithDomain:NSCocoaErrorDomain code:code userInfo:@{NSLocalizedDescriptionKey:msg}];
            }
        });
    }];
}

@end
