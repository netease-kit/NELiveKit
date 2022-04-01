//
//  NETSLiveApi.m
//  NLiteAVDemo
//
//  Created by Ease on 2020/12/3.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import "NETSLiveApi.h"


@implementation NETSLiveApi

+ (void)randowToipcWithCompletionHandle:(nullable NETSRequestCompletion)completionHandle
                            errorHandle:(nullable NETSRequestError)errorHandle
{
    NETSApiOptions *options = [[NETSApiOptions alloc] init];
    options.baseUrl = @"/v1/room/getRandomRoomTopic";
    options.apiMethod = NETSRequestMethodPOST;
    options.params = @{@"accountId": [NELiveKit shared].userUuid ?: @""};
    options.modelMapping = @[
        [NETSApiModelMapping mappingWith:@"/data" mappingClass:[NSString class] isArray:NO]
    ];
    
    NETSRequest *resuest = [[NETSRequest alloc] initWithOptions:options];
    resuest.completionBlock = completionHandle;
    resuest.errorBlock = errorHandle;
    [resuest asyncRequest];
}

+ (void)randomCoverWithCompletionHandle:(nullable NETSRequestCompletion)completionHandle
                            errorHandle:(nullable NETSRequestError)errorHandle
{
    NETSApiOptions *options = [[NETSApiOptions alloc] init];
    options.baseUrl = @"/v1/room/getRandomLivePic";
    options.apiMethod = NETSRequestMethodPOST;
    options.modelMapping = @[
        [NETSApiModelMapping mappingWith:@"/data" mappingClass:[NSString class] isArray:NO]
    ];
    
    NETSRequest *resuest = [[NETSRequest alloc] initWithOptions:options];
    resuest.completionBlock = completionHandle;
    resuest.errorBlock = errorHandle;
    [resuest asyncRequest];
}

@end
