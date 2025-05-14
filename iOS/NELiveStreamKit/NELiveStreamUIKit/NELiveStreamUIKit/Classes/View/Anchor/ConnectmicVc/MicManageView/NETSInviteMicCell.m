// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NETSInviteMicCell.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/UIView+NEUIExtension.h>
#import <SDWebImage/SDWebImage.h>
#import "NELiveStreamGlobalMacro.h"
#import "UIView+NELiveStreamGradient.h"

@interface NETSInviteMicCell ()
@property(nonatomic, strong) UILabel *rakeLabel;
@property(nonatomic, strong) UIImageView *headImageView;
@property(nonatomic, strong) UILabel *nickNameLabel;
@property(nonatomic, strong) UIButton *inviteButton;

@end

@implementation NETSInviteMicCell

+ (instancetype)loadInviteMicCellWithTableView:(UITableView *)tableView {
  static NSString *contactServiceCellId = @"NETSInviteMicCell";
  NETSInviteMicCell *cell = [tableView dequeueReusableCellWithIdentifier:contactServiceCellId];
  if (!cell) {
    cell = [[NETSInviteMicCell alloc] initWithStyle:UITableViewCellStyleDefault
                                    reuseIdentifier:contactServiceCellId];
  }
  cell.selectionStyle = UITableViewCellSelectionStyleNone;
  return cell;
}

- (void)nets_setupViews {
  [super nets_setupViews];
  [self.contentView addSubview:self.rakeLabel];
  [self.contentView addSubview:self.headImageView];
  [self.contentView addSubview:self.nickNameLabel];
  [self.contentView addSubview:self.inviteButton];

  [self.rakeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.contentView).offset(20);
    make.centerY.equalTo(self.contentView);
  }];

  [self.headImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.rakeLabel.mas_right).offset(15);
    make.centerY.equalTo(self.contentView);
    make.size.mas_equalTo(CGSizeMake(36, 36));
  }];

  [self.nickNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.headImageView.mas_right).offset(12);
    make.centerY.equalTo(self.contentView);
  }];

  [self.inviteButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.contentView).offset(-20);
    make.centerY.equalTo(self.contentView);
    make.size.mas_equalTo(CGSizeMake(81, 28));
  }];
}

- (void)awakeFromNib {
  [super awakeFromNib];
  // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
  [super setSelected:selected animated:animated];

  // Configure the view for the selected state
}

#pragma mark - setter
- (void)setCellIndexPath:(NSIndexPath *)cellIndexPath {
  _cellIndexPath = cellIndexPath;
  if (cellIndexPath.row == 0) {
    self.rakeLabel.textColor = HEXCOLOR(0xF24957);
  } else if (cellIndexPath.row == 1) {
    self.rakeLabel.textColor = HEXCOLOR(0xFF791A);
  } else if (cellIndexPath.row == 2) {
    self.rakeLabel.textColor = HEXCOLOR(0xFFAA00);
  } else {
    self.rakeLabel.textColor = HEXCOLOR(0xBFBFBF);
  }
  self.rakeLabel.text = [NSString stringWithFormat:@"%ld", cellIndexPath.row + 1];
}

- (void)setUserModel:(NELiveStreamSeatItem *)userModel {
  _userModel = userModel;
  self.nickNameLabel.text = userModel.userName;
  [self.headImageView sd_setImageWithURL:[NSURL URLWithString:userModel.icon]
                        placeholderImage:[UIImage imageNamed:@"avator"]];
}

#pragma mark - privateMethod
- (void)inviteConnectMicAction:(UIButton *)sender {
  if (self.delegate && [self.delegate respondsToSelector:@selector(didInviteAudienceConnectMic:)]) {
    [self.delegate didInviteAudienceConnectMic:self.userModel];
  }
}

#pragma mark - lazyMethod
- (UILabel *)rakeLabel {
  if (!_rakeLabel) {
    _rakeLabel = [[UILabel alloc] init];
    _rakeLabel.font = [UIFont systemFontOfSize:14];
    _rakeLabel.textColor = HEXCOLOR(0xF24957);
    _rakeLabel.text = @"1";
  }
  return _rakeLabel;
  ;
}

- (UIImageView *)headImageView {
  if (!_headImageView) {
    _headImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"avator"]];
    _headImageView.contentMode = UIViewContentModeScaleAspectFit;
    _headImageView.layer.cornerRadius = 18;
    _headImageView.layer.masksToBounds = true;
  }
  return _headImageView;
}

- (UILabel *)nickNameLabel {
  if (!_nickNameLabel) {
    _nickNameLabel = [[UILabel alloc] init];
    _nickNameLabel.font = [UIFont systemFontOfSize:14];
    _nickNameLabel.textColor = HEXCOLOR(0x0F0C0A);
    _nickNameLabel.text = @"";
  }
  return _nickNameLabel;
  ;
}

- (UIButton *)inviteButton {
  if (!_inviteButton) {
    _inviteButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_inviteButton setTitle:NSLocalizedString(@"邀请上麦", nil) forState:UIControlStateNormal];
    [_inviteButton setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
    _inviteButton.titleLabel.font = [UIFont systemFontOfSize:14];
    _inviteButton.layer.cornerRadius = 4;
    [_inviteButton setGradientBackgroundWithColors:@[ HEXCOLOR(0xF359E2), HEXCOLOR(0xFF7272) ]
                                         locations:nil
                                        startPoint:CGPointMake(0, 0)
                                          endPoint:CGPointMake(1, 0)];
    [_inviteButton addTarget:self
                      action:@selector(inviteConnectMicAction:)
            forControlEvents:UIControlEventTouchUpInside];
  }
  return _inviteButton;
}
@end
