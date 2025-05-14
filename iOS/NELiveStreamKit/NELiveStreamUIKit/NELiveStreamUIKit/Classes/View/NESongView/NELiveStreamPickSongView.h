// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NELiveStreamKit/NELiveStreamKit-Swift.h>
#import <NEOrderSong/NEOrderSong-Swift.h>

#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN
@protocol NELiveStreamPickSongViewProtocol <NSObject>

- (void)pauseSong;

- (void)resumeSong;

- (void)nextSong:(NEOrderSongResponseOrderSongModel *_Nullable)orderSongModel;

- (void)volumeChanged:(float)volume;

@end

typedef void (^ApplyOnSeat)(void);

@interface NELiveStreamPickSongView : UIView

- (instancetype)initWithFrame:(CGRect)frame detail:(nullable NELiveStreamRoomInfo *)detail;

@property(nonatomic, copy) ApplyOnSeat applyOnseat;

@property(nonatomic, weak) id<NELiveStreamPickSongViewProtocol> delegate;

// 申请连麦相关
- (void)cancelApply;
- (void)applyFaile;
- (void)applySuccess;

- (void)setPlayingStatus:(BOOL)status;

// 数据刷新
- (void)refreshPickedSongView;

/// 设置音量
- (void)setVolume:(float)volume;
- (float)getVolume;

@end

NS_ASSUME_NONNULL_END
