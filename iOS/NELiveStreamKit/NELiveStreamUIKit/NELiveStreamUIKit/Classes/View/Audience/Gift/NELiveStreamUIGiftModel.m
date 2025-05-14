// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamUIGiftModel.h"
#import "NELiveStreamLocalized.h"

@implementation NELiveStreamUIGiftModel

- (instancetype)initWithGiftId:(int32_t)giftId
                          icon:(NSString *)icon
                       display:(NSString *)display
                         price:(int32_t)price {
  self = [super init];
  if (self) {
    _giftId = giftId;
    _icon = icon;
    _display = display;
    _price = price;
  }
  return self;
}

+ (NSArray<NELiveStreamUIGiftModel *> *)defaultGifts {
  NELiveStreamUIGiftModel *gift1 =
      [[NELiveStreamUIGiftModel alloc] initWithGiftId:1
                                                 icon:@"gift03_ico"
                                              display:NELocalizedString(@"荧光棒")
                                                price:9];
  NELiveStreamUIGiftModel *gift2 =
      [[NELiveStreamUIGiftModel alloc] initWithGiftId:2
                                                 icon:@"gift04_ico"
                                              display:NELocalizedString(@"安排")
                                                price:99];
  NELiveStreamUIGiftModel *gift3 =
      [[NELiveStreamUIGiftModel alloc] initWithGiftId:3
                                                 icon:@"gift02_ico"
                                              display:NELocalizedString(@"跑车")
                                                price:199];
  NELiveStreamUIGiftModel *gift4 =
      [[NELiveStreamUIGiftModel alloc] initWithGiftId:4
                                                 icon:@"gift01_ico"
                                              display:NELocalizedString(@"火箭")
                                                price:999];
  return @[ gift1, gift2, gift3, gift4 ];
}

+ (nullable NELiveStreamUIGiftModel *)getRewardWithGiftId:(NSInteger)giftId {
  NELiveStreamUIGiftModel *gift = nil;
  for (NELiveStreamUIGiftModel *tmp in [NELiveStreamUIGiftModel defaultGifts]) {
    if (tmp.giftId == giftId) {
      gift = tmp;
      break;
    }
  }
  return gift;
}

@end
