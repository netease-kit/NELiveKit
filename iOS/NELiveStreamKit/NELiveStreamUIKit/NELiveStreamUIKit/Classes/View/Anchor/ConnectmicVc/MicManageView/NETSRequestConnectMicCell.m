// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NETSRequestConnectMicCell.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/UIView+NEUIExtension.h>
#import <SDWebImage/SDWebImage.h>
#import "NELiveStreamGlobalMacro.h"
#import "UIView+NELiveStreamGradient.h"

@interface NETSRequestConnectMicCell ()
@property(nonatomic, strong) UILabel *rakeLabel;
@property(nonatomic, strong) UIImageView *headImageView;
@property(nonatomic, strong) UILabel *nickNameLabel;
@property(nonatomic, strong) UIButton *acceptButton;
@property(nonatomic, strong) UIButton *refuseButton;

@end

@implementation NETSRequestConnectMicCell

- (void)awakeFromNib {
  [super awakeFromNib];
  // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
  [super setSelected:selected animated:animated];

  // Configure the view for the selected state
}

+ (instancetype)loadRequestConnectMicCellWithTableView:(UITableView *)tableView {
  static NSString *contactServiceCellId = @"NETSRequestConnectMicCell";
  NETSRequestConnectMicCell *cell =
      [tableView dequeueReusableCellWithIdentifier:contactServiceCellId];
  if (!cell) {
    cell = [[NETSRequestConnectMicCell alloc] initWithStyle:UITableViewCellStyleDefault
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
  [self.contentView addSubview:self.acceptButton];
  [self.contentView addSubview:self.refuseButton];

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

  [self.acceptButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.contentView).offset(-20);
    make.centerY.equalTo(self.contentView);
    make.size.mas_equalTo(CGSizeMake(53, 28));
  }];

  [self.refuseButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.acceptButton.mas_left).offset(-10);
    make.centerY.equalTo(self.contentView);
    make.size.mas_equalTo(CGSizeMake(53, 28));
  }];
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
- (void)dealConnectAudience:(UIButton *)sender {
  BOOL isAccept = (sender == self.acceptButton);
  if (self.delegate && [self.delegate respondsToSelector:@selector(dealMicRequestAccept:
                                                                              userModel:)]) {
    [self.delegate dealMicRequestAccept:isAccept userModel:self.userModel];
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

- (UIButton *)acceptButton {
  if (!_acceptButton) {
    _acceptButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_acceptButton setTitle:NSLocalizedString(@"接受", nil) forState:UIControlStateNormal];
    [_acceptButton setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
    _acceptButton.titleLabel.font = [UIFont systemFontOfSize:14];
    _acceptButton.layer.cornerRadius = 4;
    [_acceptButton setGradientBackgroundWithColors:@[ HEXCOLOR(0xF359E2), HEXCOLOR(0xFF7272) ]
                                         locations:nil
                                        startPoint:CGPointMake(0, 0)
                                          endPoint:CGPointMake(1, 0)];
    [_acceptButton addTarget:self
                      action:@selector(dealConnectAudience:)
            forControlEvents:UIControlEventTouchUpInside];
  }
  return _acceptButton;
}

- (UIButton *)refuseButton {
  if (!_refuseButton) {
    _refuseButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_refuseButton setTitle:NSLocalizedString(@"拒绝", nil) forState:UIControlStateNormal];
    [_refuseButton setTitleColor:HEXCOLOR(0x333333) forState:UIControlStateNormal];
    _refuseButton.titleLabel.font = [UIFont systemFontOfSize:14];
    _refuseButton.layer.cornerRadius = 4;
    _refuseButton.layer.borderColor = UIColorFromRGBA(0x000000, 0.24).CGColor;
    _refuseButton.layer.borderWidth = 1.0;
    [_refuseButton addTarget:self
                      action:@selector(dealConnectAudience:)
            forControlEvents:UIControlEventTouchUpInside];
  }
  return _refuseButton;
}
@end
