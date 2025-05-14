// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NELiveStreamUIGiftModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface NELiveStreamSendGiftCell : UICollectionViewCell

/// 实例化直播列表页cell
+ (NELiveStreamSendGiftCell *)cellWithCollectionView:(UICollectionView *)collectionView
                                           indexPath:(NSIndexPath *)indexPath
                                               datas:(NSArray<NELiveStreamUIGiftModel *> *)datas;

/// 计算直播列表页cell size
+ (CGSize)size;

@end

NS_ASSUME_NONNULL_END
