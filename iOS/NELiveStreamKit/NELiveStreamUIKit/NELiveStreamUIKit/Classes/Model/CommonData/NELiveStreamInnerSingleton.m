// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamInnerSingleton.h"

static NELiveStreamInnerSingleton *singleton = nil;
@implementation NELiveStreamInnerSingleton
+ (instancetype)sharedInstance {
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    singleton = [NELiveStreamInnerSingleton new];
  });
  return singleton;
}
- (NSArray<NELiveStreamSeatItem *> *)fetchAudienceSeatItems:
    (NSArray<NELiveStreamSeatItem *> *)seatItems {
  NSMutableArray *tempArr = @[].mutableCopy;
  for (NELiveStreamSeatItem *item in seatItems) {
    if (![item.user isEqualToString:self.roomInfo.anchor.userUuid]) {
      [tempArr addObject:item];
    }
  }
  return tempArr.copy;
}
- (NELiveStreamSeatItem *)fetchAnchorItem:(NSArray<NELiveStreamSeatItem *> *)seatItems {
  NELiveStreamSeatItem *anchorItem = nil;
  for (NELiveStreamSeatItem *item in seatItems) {
    if ([item.user isEqualToString:self.roomInfo.anchor.userUuid]) {
      anchorItem = item;
    }
  }
  return anchorItem;
}
@end
