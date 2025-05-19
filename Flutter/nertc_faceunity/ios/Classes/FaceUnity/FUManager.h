// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <AVFoundation/AVFoundation.h>
#import <Flutter/Flutter.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <libCNamaSDK/CNamaSDK.h>
#import <libCNamaSDK/FURenderer.h>
#import "FUBeautyParam.h"

@class FULiveModel;

/*
 items 保存加载到Nama中bundle的操作句柄集
 注意：道具句柄数组位置可以调整，道具渲染顺序更具数组顺序渲染
 */
typedef NS_ENUM(NSUInteger, FUNamaHandleType) {
  FUNamaHandleTypeBeauty = 0, /* items[0] ------ 放置 美颜道具句柄 */
  FUNamaHandleTypeItem =
      1,                          /* items[1] ------ 放置
                                     普通道具句柄（包含很多，如：贴纸，aoimoji...若不单一存在，可放句柄集其他位置） */
  FUNamaHandleTypeFxaa = 2,       /* items[2] ------ fxaa抗锯齿道具句柄 */
  FUNamaHandleTypeGesture = 3,    /* items[3] ------ 手势识别道具句柄 */
  FUNamaHandleTypeChangeface = 4, /* items[4] ------ 海报换脸道具句柄 */
  FUNamaHandleTypeComic = 5,      /* items[5] ------ 动漫道具句柄 */
  FUNamaHandleTypeMakeup = 6,     /* items[6] ------ 美妆道具句柄 */
  FUNamaHandleTypePhotolive = 7,  /* items[7] ------ 异图道具句柄 */
  FUNamaHandleTypeAvtarHead = 8,  /* items[8] ------ Avtar头*/
  FUNamaHandleTypeAvtarHiar = 9,  /* items[9] ------ Avtar头发 */
  FUNamaHandleTypeAvtarbg = 10,   /* items[10] ------ Avtar背景 */
  FUNamaHandleTypeBodySlim = 11,  /* items[11] ------ 美体道具 */
  FUNamaHandleTypeBodyAvtar = 12, /* 全身avtar */
  FUNamaHandleTotal = 13,
};

@interface FUManager : NSObject

+ (FUManager *)shareManager;

- (void)setupWithKey:(FlutterStandardTypedData *)key;

- (BOOL)isInitBeauty;

/**加载美颜道具*/
- (void)loadFilter:(NSDictionary *)fliterParams;

/**加载美颜参数*/
- (int)setParamItemAboutType:(FUNamaHandleType)type name:(NSString *)paramName value:(id)value;

/**将道具绘制到pixelBuffer*/
- (CVPixelBufferRef)renderItemsToPixelBuffer:(CVPixelBufferRef)pixelBuffer;

/**销毁道具*/
- (void)destroyAllItems;
@end
