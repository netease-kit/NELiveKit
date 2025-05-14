// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NELiveStreamUIGiftModel.h"
#import "NELiveStreamUIMicQueueViewProtocol.h"
NS_ASSUME_NONNULL_BEGIN

@protocol NELiveStreamSendGiftViewtDelegate <NSObject>

- (void)didSendGift:(NELiveStreamUIGiftModel *)gift
          giftCount:(int)giftCount
          userUuids:(NSArray *)userUuids;

@end

@interface NELiveStreamSendGiftViewController : UIViewController <NEUIMicQueueViewProtocol>

+ (NELiveStreamSendGiftViewController *)showWithTarget:(id<NELiveStreamSendGiftViewtDelegate>)target
                                        viewController:(UIViewController *)viewController;

@end

NS_ASSUME_NONNULL_END
