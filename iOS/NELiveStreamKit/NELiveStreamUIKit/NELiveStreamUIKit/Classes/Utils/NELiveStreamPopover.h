// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NELiveStreamPopoverOption.h"

typedef void (^NELiveStreamPopoverBlock)(void);

@interface NELiveStreamPopover : UIView

@property(nonatomic, copy) NELiveStreamPopoverBlock willShowHandler;
@property(nonatomic, copy) NELiveStreamPopoverBlock willDismissHandler;
@property(nonatomic, copy) NELiveStreamPopoverBlock didShowHandler;
@property(nonatomic, copy) NELiveStreamPopoverBlock didDismissHandler;

@property(nonatomic, strong) NELiveStreamPopoverOption *option;

- (instancetype)initWithOption:(NELiveStreamPopoverOption *)option;

- (void)dismiss;

- (void)show:(UIView *)contentView fromView:(UIView *)fromView;
- (void)show:(UIView *)contentView fromView:(UIView *)fromView inView:(UIView *)inView;
- (void)show:(UIView *)contentView atPoint:(CGPoint)point;
- (void)show:(UIView *)contentView atPoint:(CGPoint)point inView:(UIView *)inView;

- (CGPoint)originArrowPointWithView:(UIView *)contentView fromView:(UIView *)fromView;
- (CGPoint)arrowPointWithView:(UIView *)contentView
                     fromView:(UIView *)fromView
                       inView:(UIView *)inView
                  popoverType:(NELiveStreamPopoverType)type;

@end
