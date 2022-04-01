//
//  NENavCustomView.m
//  NLiteAVDemo
//
//  Created by I am Groot on 2020/8/20.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import "NENavCustomView.h"

@implementation NENavCustomView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self initUI];
    }
    return self;
}

- (void)initUI {
    self.backgroundColor = [UIColor blackColor];
    UIView *bgView = [[UIView alloc] init];
    [self addSubview:bgView];
    CGFloat statusHeight = [[UIApplication sharedApplication] statusBarFrame].size.height;
    [bgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(UIEdgeInsetsMake(statusHeight, 0, 0, 0));
    }];
    UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"云信Logo"]];
    [bgView addSubview:imageView];
    [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(bgView);
    }];
}

@end

