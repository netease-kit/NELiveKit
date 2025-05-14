// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamUIChatroomMicCell.h"
#import <NEUIKit/UIColor+NEUIExtension.h>
#import <NEUIKit/UIView+NEUIExtension.h>
#import "NELiveStreamLocalized.h"
#import "NELiveStreamUIKit/NELiveStreamUIKit-Swift.h"

@implementation NEUIChatroomMicCell

@synthesize nameLabel = _nameLabel;
@synthesize connectBtn = _connectBtn;
@synthesize avatar = _avatar;
@synthesize smallIcon = _smallIcon;
@synthesize singIco = _singIco;
@synthesize loadingIco = _loadingIco;
@synthesize giftLabal = _giftLabal;
@synthesize lottieView = _lottieView;

- (void)layoutSubviews {
  [super layoutSubviews];

  self.connectBtn.top = 5;
  self.connectBtn.left = 5;
  self.connectBtn.width = self.width - 10;
  self.connectBtn.height = self.width - 10;
  self.connectBtn.layer.cornerRadius = self.connectBtn.width / 2;

  self.avatar.frame = CGRectMake(self.connectBtn.left + 0.5, self.connectBtn.top + 0.5,
                                 self.connectBtn.width - 1, self.connectBtn.height - 1);
  self.avatar.layer.cornerRadius = (self.connectBtn.width - 1) * 0.5;
  self.avatar.layer.masksToBounds = YES;

  self.nameLabel.top = self.connectBtn.bottom + 6.0;
  self.nameLabel.left = 0;
  self.nameLabel.width = self.width;
  self.nameLabel.height = 18;

  self.smallIcon.width = 17;
  self.smallIcon.height = 17;
  self.smallIcon.right = self.connectBtn.right;
  self.smallIcon.bottom = self.connectBtn.bottom;

  self.singIco.frame = self.smallIcon.frame;

  self.loadingIco.frame = self.connectBtn.frame;
  self.loadingIco.layer.cornerRadius = self.connectBtn.layer.cornerRadius;

  self.giftLabal.height = 15;
  self.giftLabal.width = self.width;
  self.giftLabal.top = self.nameLabel.bottom + 2.0;

  self.lottieView.frame = CGRectMake(0, 0, self.connectBtn.width, self.connectBtn.height);
  self.lottieView.center = self.connectBtn.center;
  self.lottieView.animationViewFrame =
      CGRectMake(-25, -25, self.connectBtn.width + 50, self.connectBtn.height + 50);
}

+ (NEUIChatroomMicCell *)cellWithCollectionView:(UICollectionView *)collectionView
                                           data:(NELiveStreamSeatItem *)data
                                      indexPath:(NSIndexPath *)indexPath {
  NEUIChatroomMicCell *cell =
      [collectionView dequeueReusableCellWithReuseIdentifier:[self description]
                                                forIndexPath:indexPath];
  //    cell.delegate = self;
  //    cell.clipsToBounds = NO;
  [cell refresh:data];
  return cell;
}

+ (CGSize)size {
  CGFloat paddingW = [self cellPaddingW];
  CGFloat width =
      (UIScreen.mainScreen.bounds.size.width - [NELiveStreamUI margin] * 2 - 3 * paddingW) / 4;
  // 最后的18是波谱加的collectionView contentInset
  CGFloat height = width + 6 + 18 + 20 + 18;
  return CGSizeMake(width, height);
}

+ (CGFloat)cellPaddingH {
  return [NELiveStreamUI seatLineSpace];
}

+ (CGFloat)cellPaddingW {
  return [NELiveStreamUI seatItemSpace];
}

#pragma mark - lazy load

- (UILabel *)nameLabel {
  if (!_nameLabel) {
    _nameLabel = [[UILabel alloc] init];
    [_nameLabel setTextColor:[UIColor whiteColor]];
    [_nameLabel setTextAlignment:NSTextAlignmentCenter];
    _nameLabel.font = [UIFont systemFontOfSize:12];
    [_nameLabel setText:NELocalizedString(@"用户")];
    [_nameLabel sizeToFit];
  }
  return _nameLabel;
}

- (NEUIAnimationButton *)connectBtn {
  if (!_connectBtn) {
    NEUIAnimationButton *connectBtn = [NEUIAnimationButton buttonWithType:UIButtonTypeCustom];
    [connectBtn addTarget:self
                   action:@selector(onConnectBtnPressed)
         forControlEvents:UIControlEventTouchUpInside];
    UIImage *img = [NELiveStreamUI ne_livestream_imageName:@"mic_none_ico"];
    [connectBtn setImage:img forState:UIControlStateNormal];
    _connectBtn = connectBtn;
    _connectBtn.backgroundColor = [UIColor colorWithWhite:0 alpha:0.5];
    _connectBtn.layer.masksToBounds = YES;
    _connectBtn.layer.borderColor = [UIColor whiteColor].CGColor;
  }
  return _connectBtn;
}

- (UIImageView *)avatar {
  if (!_avatar) {
    _avatar = [[UIImageView alloc] init];
    _avatar.contentMode = UIViewContentModeScaleAspectFill;
  }
  return _avatar;
}

- (UIImageView *)smallIcon {
  if (!_smallIcon) {
    UIImageView *smallIcon = [[UIImageView alloc] initWithFrame:CGRectZero];
    smallIcon.hidden = YES;
    _smallIcon = smallIcon;
  }
  return _smallIcon;
}

- (UIImageView *)singIco {
  if (!_singIco) {
    UIImage *img = [NELiveStreamUI ne_livestream_imageName:@"mic_sing_ico"];
    _singIco = [[UIImageView alloc] initWithImage:img];
    _singIco.hidden = YES;
  }
  return _singIco;
}

- (LOTAnimationView *)loadingIco {
  if (!_loadingIco) {
    NSBundle *bundle =
        [NELiveStreamUI ne_livestream_sourceBundle];  //[NSBundle bundleWithPath:path];
    _loadingIco = [LOTAnimationView animationNamed:@"apply_on_mic.json" inBundle:bundle];
    _loadingIco.loopAnimation = YES;
    [_loadingIco play];
    _loadingIco.hidden = YES;
    _loadingIco.backgroundColor = [UIColor colorWithWhite:0 alpha:0.5];
    _loadingIco.userInteractionEnabled = NO;
  }
  return _loadingIco;
}
- (UILabel *)giftLabal {
  if (!_giftLabal) {
    _giftLabal = [[UILabel alloc] init];
    //        _giftLabal.text = @"Text";
    //        _giftLabal.backgroundColor = [UIColor redColor];
    _giftLabal.textColor = [UIColor greenColor];
    _giftLabal.textAlignment = NSTextAlignmentCenter;
  }
  return _giftLabal;
}

- (NELottieView *)lottieView {
  if (!_lottieView) {
    _lottieView = [[NELottieView alloc] initWithFrame:CGRectMake(0, 0, self.width, self.height)
                                               lottie:@"speak_wave"
                                               bundle:[NSBundle bundleForClass:[self class]]];
    [_lottieView stop];
  }
  return _lottieView;
}

@end
