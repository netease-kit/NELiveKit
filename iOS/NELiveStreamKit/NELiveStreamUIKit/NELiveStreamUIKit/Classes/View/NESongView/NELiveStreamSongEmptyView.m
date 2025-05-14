// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamSongEmptyView.h"
#import "Masonry/Masonry.h"
#import "NELiveStreamLocalized.h"
#import "NELiveStreamPickSongColorDefine.h"
// #import "NEVoiceRoomUI.h"
// #import "NTESGlobalMacro.h"
#import <NELiveStreamUIKit/NELiveStreamUIKit-Swift.h>

@interface NELiveStreamSongEmptyView ()
@property(nonatomic, strong) UIView *emptyImageView;
@property(nonatomic, strong) UILabel *emptyLabel;

@end
@implementation NELiveStreamSongEmptyView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self initView];
    self.backgroundColor = [UIColor clearColor];
  }
  return self;
}

- (void)initView {
  self.emptyImageView = [[UIImageView alloc]
      initWithImage:[NELiveStreamUI ne_livestream_imageName:@"listen_together_song_empty"]];

  [self addSubview:self.emptyImageView];
  [self.emptyImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.equalTo(self.mas_centerX);
    make.top.equalTo(self.mas_top).offset(80);
    make.width.height.equalTo(@50);
  }];

  self.emptyLabel = [[UILabel alloc] init];
  self.emptyLabel.text = NELocalizedString(@"还没有人点歌哦");
  self.emptyLabel.textColor = HEXCOLOR(0xBFBFBF);
  self.emptyLabel.font = [UIFont systemFontOfSize:14];
  [self addSubview:self.emptyLabel];
  [self.emptyLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.emptyImageView.mas_bottom).offset(16);
    make.centerX.equalTo(self);
  }];
}
@end
