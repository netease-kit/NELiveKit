// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NETSBaseTabViewCell.h"

@interface NETSBaseTabViewCell ()

@property(nonatomic, readwrite, strong) id model;
@property(nonatomic, strong) UIView *bottomView;  // 底部默认线条

@end

@implementation NETSBaseTabViewCell

- (void)awakeFromNib {
  [super awakeFromNib];
  // Initialization code
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style
              reuseIdentifier:(NSString *)reuseIdentifier
                        model:(id<NETSBaseModelProtocol>)model {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    _model = model;
    self.contentView.backgroundColor = [UIColor whiteColor];
    [self nets_setupViews];
    [self nets_bindViewModel];
  }
  return self;
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style
              reuseIdentifier:(NSString *)reuseIdentifier {
  return [self initWithStyle:style reuseIdentifier:reuseIdentifier model:nil];
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
  return [self initWithStyle:UITableViewCellStyleDefault reuseIdentifier:nil model:nil];
}

- (void)nets_setupViews {
  [self.contentView addSubview:self.bottomView];
  //    [self.bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
  //        make.height.mas_equalTo(0.5);
  //        make.bottom.right.equalTo(self);
  //        make.left.equalTo(self.contentView).offset(15);
  //    }];
}

- (void)nets_bindViewModel {
}

- (void)setSplitLineStyle:(BOOL)splitLineStyle {
  if (splitLineStyle) {
    self.bottomView.hidden = YES;
  } else {
    self.bottomView.hidden = NO;
  }
}

#pragma mark === lazyMethod
- (UIView *)bottomView {
  if (!_bottomView) {
    _bottomView = [[UIView alloc] init];
    //        _bottomView.backgroundColor = HEXCOLOR(0xe5e5e5);
    _bottomView.hidden = YES;
  }
  return _bottomView;
}

@end
