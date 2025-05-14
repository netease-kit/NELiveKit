// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
@class NELiveStreamSongPlayControlView;
NS_ASSUME_NONNULL_BEGIN

@protocol NELiveStreamSongPlayControlViewDelegate <NSObject>

- (void)pauseSong:(NELiveStreamSongPlayControlView *)view;

- (void)resumeSong:(NELiveStreamSongPlayControlView *)view;

- (void)nextSong:(NELiveStreamSongPlayControlView *)view;

- (void)volumeChanged:(float)volume view:(NELiveStreamSongPlayControlView *)view;

@end

@interface NELiveStreamSongPlayControlView : UIView

/// 通过设置该值来决定显示播放按钮还是暂停按钮
@property(nonatomic, assign) BOOL isPlaying;
@property(nonatomic, assign) float volume;
@property(nonatomic, weak) id<NELiveStreamSongPlayControlViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
