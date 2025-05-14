// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NELiveStreamKit/NELiveStreamKit-Swift.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface NELiveStreamGiftToCell : UICollectionViewCell

/// cell size
+ (CGSize)size;
+ (NELiveStreamGiftToCell *)cellWithCollectionView:(UICollectionView *)collectionView
                                         indexPath:(NSIndexPath *)indexPath
                                        anchorData:(NELiveStreamSeatItem *)anchorData
                                             datas:(NSArray<NELiveStreamSeatItem *> *)datas;
@end

NS_ASSUME_NONNULL_END
