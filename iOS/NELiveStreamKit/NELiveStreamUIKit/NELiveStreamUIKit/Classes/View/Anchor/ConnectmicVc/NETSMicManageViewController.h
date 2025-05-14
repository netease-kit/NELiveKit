// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamMicListProtocol.h"
#import "NETSBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface NETSMicManageViewController : NETSBaseViewController

@property(nonatomic, weak) id<NEliveStreamMicListDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
