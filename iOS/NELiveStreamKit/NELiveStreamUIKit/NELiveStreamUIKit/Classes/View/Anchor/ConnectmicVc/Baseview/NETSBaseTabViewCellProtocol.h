// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>

@protocol NETSBaseTabViewCellProtocol <NSObject>

@optional

/**
 子视图添加
 */
- (void)nets_setupViews;

/**
 业务逻辑绑定
 */
- (void)nets_bindViewModel;

@end
