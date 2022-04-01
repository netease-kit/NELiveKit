//
//  NETSLiveApi.h
//  NLiteAVDemo
//
//  Created by Ease on 2020/12/3.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import <Foundation/Foundation.h>
#import "NETSRequest.h"


NS_ASSUME_NONNULL_BEGIN

@interface NETSLiveApi : NSObject

/**
 随机获取直播间主题
 */
+ (void)randowToipcWithCompletionHandle:(nullable NETSRequestCompletion)completionHandle
                            errorHandle:(nullable NETSRequestError)errorHandle;

/**
 随机获取直播间封面
 */
+ (void)randomCoverWithCompletionHandle:(nullable NETSRequestCompletion)completionHandle
                            errorHandle:(nullable NETSRequestError)errorHandle;
@end

NS_ASSUME_NONNULL_END
