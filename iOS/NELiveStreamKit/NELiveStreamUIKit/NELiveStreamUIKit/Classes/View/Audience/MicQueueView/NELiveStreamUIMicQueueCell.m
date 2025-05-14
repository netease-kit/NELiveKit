// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamUIMicQueueCell.h"
#import <SDWebImage/UIImageView+WebCache.h>
#import "NELiveStreamGlobalMacro.h"
#import "NELiveStreamInnerSingleton.h"
#import "NELiveStreamLocalized.h"
#import "NELiveStreamUIKit/NELiveStreamUIKit-Swift.h"
#import "NSArray+NELiveStreamUIExtension.h"

@implementation NEUIMicQueueCell

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self.contentView addSubview:self.lottieView];
    [self.contentView addSubview:self.nameLabel];
    [self.contentView addSubview:self.connectBtn];
    [self.contentView addSubview:self.avatar];
    [self.contentView addSubview:self.smallIcon];
    [self.contentView addSubview:self.singIco];
    [self.contentView addSubview:self.loadingIco];
    [self.contentView addSubview:self.giftLabal];
  }
  return self;
}

- (void)startSoundAnimationWithValue:(NSInteger)value {
  [_connectBtn startCustomAnimation];
  _connectBtn.info = @(value).stringValue;
}

- (void)stopSoundAnimation {
  [_connectBtn stopCustomAnimation];
  _connectBtn.info = nil;
}

- (void)startSpeakAnimation {
  [self.lottieView play];
}

- (void)stopSpeakAnimation {
  [self.lottieView stop];
}

- (void)refresh:(NELiveStreamSeatItem *)micInfo {
  _micInfo = micInfo;
  // 判断直播
  if ([micInfo.user
          isEqualToString:NELiveStreamInnerSingleton.sharedInstance.roomInfo.anchor.userUuid]) {
    [self _anchorRefresh:micInfo];
  } else {
    [self _audienceRefresh:micInfo];
  }
}

/// 刷新主播麦位信息
- (void)_anchorRefresh:(NELiveStreamSeatItem *)micInfo {
  self.nameLabel.text = micInfo.userName ?: NELocalizedString(@"房主");
  [self.avatar sd_setImageWithURL:[NSURL URLWithString:micInfo.icon]];
  self.connectBtn.layer.borderWidth = 1;

  NELiveStreamMember *anchorMember =
      [NELiveStreamKit.getInstance.allMemberList ne_find:^BOOL(NELiveStreamMember *obj) {
        return [obj.account isEqualToString:micInfo.user];
      }];

  if (!anchorMember) return;
  if (anchorMember.isAudioBanned) {
    [self.smallIcon setImage:[NELiveStreamUI ne_livestream_imageName:@"mic_shield_ico"]];
  } else {
    if (anchorMember.isAudioOn) {
      [self.smallIcon setImage:[NELiveStreamUI ne_livestream_imageName:@"mic_open_ico"]];
      self.smallIcon.hidden = NO;
    } else {
      [self.smallIcon setImage:[NELiveStreamUI ne_livestream_imageName:@"mic_close_ico"]];
      self.smallIcon.hidden = NO;
    }
  }
}

- (void)updateGiftLabel:(NSString *)title {
  if (!title || [title isEqualToString:@"0"]) {
    // 1.初始化富文本对象
    NSMutableAttributedString *attributedString =
        [[NSMutableAttributedString alloc] initWithString:@""];
    self.giftLabal.attributedText = attributedString;
    return;
  }
  if (title.intValue > 99999) {
    title = @"99999+";
  }
  title = [NSString stringWithFormat:@" %@", title];
  UIFont *font = [UIFont systemFontOfSize:10];
  // 1.初始化富文本对象
  NSMutableAttributedString *attributedString =
      [[NSMutableAttributedString alloc] initWithString:title];
  [attributedString addAttribute:NSForegroundColorAttributeName
                           value:HEXCOLOR(0xFFC86B)
                           range:NSMakeRange(0, title.length)];
  // 2.字体属性
  [attributedString addAttribute:NSFontAttributeName value:font range:NSMakeRange(0, title.length)];
  // 3.初始化NSTextAttachment对象
  NSTextAttachment *attchment = [[NSTextAttachment alloc] init];
  attchment.image = [NELiveStreamUI ne_livestream_imageName:@"gift_icon"];     // 设置图片
  attchment.bounds = CGRectMake(0, round(font.capHeight - 10) / 2.0, 10, 10);  // 设置frame
  // 4.创建带有图片的富文本
  NSAttributedString *string =
      [NSAttributedString attributedStringWithAttachment:(NSTextAttachment *)(attchment)];
  [attributedString insertAttributedString:string atIndex:0];  // 插入到第几个下标
  self.giftLabal.attributedText = attributedString;
}

/// 刷新观众麦位信息
- (void)_audienceRefresh:(NELiveStreamSeatItem *)micInfo {
  NELiveStreamMember *audienceMember =
      [NELiveStreamKit.getInstance.allMemberList ne_find:^BOOL(NELiveStreamMember *obj) {
        return [obj.account isEqualToString:micInfo.user];
      }];
  switch (micInfo.status) {
    case NELiveStreamSeatItemStatusInitial: {  // 无人
      self.nameLabel.text =
          [NSString stringWithFormat:NELocalizedString(@"麦位%zd"), micInfo.index - 1];
      [self _setAvatarWithUrl:nil];
      self.connectBtn.layer.borderWidth = 0;
      [self.connectBtn stopCustomAnimation];
      self.smallIcon.hidden = YES;
      self.loadingIco.hidden = YES;
      [self.connectBtn setImage:[NELiveStreamUI ne_livestream_imageName:@"mic_none_ico"]
                       forState:UIControlStateNormal];
    } break;
    case NELiveStreamSeatItemStatusWaiting: {  // 等待
      self.nameLabel.text = micInfo.userName ?: @"";
      [self _setAvatarWithUrl:[NSURL URLWithString:micInfo.icon]];
      self.connectBtn.layer.borderWidth = 1;
      [self.connectBtn stopCustomAnimation];
      self.smallIcon.hidden = YES;
      self.loadingIco.hidden = NO;
    } break;
    case NELiveStreamSeatItemStatusTaken: {  // 占用
      if (audienceMember.isAudioBanned) {    // 禁麦
        [self.connectBtn stopCustomAnimation];
        self.nameLabel.text = micInfo.userName ?: @"";
        [self _setAvatarWithUrl:[NSURL URLWithString:micInfo.icon]];
        self.connectBtn.layer.borderWidth = 1;
        [self.smallIcon setImage:[NELiveStreamUI ne_livestream_imageName:@"mic_shield_ico"]];
        self.smallIcon.hidden = NO;
        self.loadingIco.hidden = YES;
        [self.connectBtn stopCustomAnimation];
      } else if (audienceMember.isAudioOn) {  // 话筒打开
        self.nameLabel.text = micInfo.userName ?: @"";
        [self _setAvatarWithUrl:[NSURL URLWithString:micInfo.icon]];
        self.connectBtn.layer.borderWidth = 1;
        [self.smallIcon setImage:[NELiveStreamUI ne_livestream_imageName:@"mic_open_ico"]];
        self.smallIcon.hidden = NO;
        self.loadingIco.hidden = YES;
        [self.connectBtn stopCustomAnimation];
      } else {
        [self.connectBtn startCustomAnimation];
        self.nameLabel.text = micInfo.userName ?: @"";
        [self _setAvatarWithUrl:[NSURL URLWithString:micInfo.icon]];
        self.connectBtn.layer.borderWidth = 1;
        [self.smallIcon setImage:[NELiveStreamUI ne_livestream_imageName:@"mic_close_ico"]];
        self.smallIcon.hidden = NO;
        self.loadingIco.hidden = YES;
      }
    } break;
    default: {  // 关闭
      self.nameLabel.text =
          [NSString stringWithFormat:NELocalizedString(@"麦位%zd"), micInfo.index - 1];
      [self.connectBtn setImage:[NELiveStreamUI ne_livestream_imageName:@"icon_mic_closed_n"]
                       forState:UIControlStateNormal];
      [self _setAvatarWithUrl:nil];
      self.connectBtn.layer.borderWidth = 0;
      [self.connectBtn stopCustomAnimation];
      self.smallIcon.hidden = YES;
      self.loadingIco.hidden = YES;
    } break;
  }
}

- (void)_setAvatarWithUrl:(nullable NSURL *)url {
  if (url) {
    self.avatar.hidden = NO;
    [self.avatar sd_setImageWithURL:url];
  } else {
    self.avatar.hidden = YES;
  }
}

- (void)onConnectBtnPressed {
  if (self.delegate &&
      [self.delegate respondsToSelector:@selector(onConnectBtnPressedWithMicInfo:)]) {
    [self.delegate onConnectBtnPressedWithMicInfo:self.micInfo];
  }
}

+ (NEUIMicQueueCell *)cellWithCollectionView:(UICollectionView *)collectionView
                                        data:(NELiveStreamSeatItem *)data
                                   indexPath:(NSIndexPath *)indexPath {
  // need override
  return [NEUIMicQueueCell new];
}

+ (CGSize)size {
  // need override
  return CGSizeZero;
}

+ (CGFloat)cellPaddingH {
  // need override
  return 0;
}

+ (CGFloat)cellPaddingW {
  // need override
  return 0;
}

@end
