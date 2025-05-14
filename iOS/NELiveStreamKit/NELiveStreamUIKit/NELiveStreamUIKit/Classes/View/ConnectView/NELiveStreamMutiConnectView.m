// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELiveStreamMutiConnectView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/UIView+NEUIExtension.h>
#import "NELiveStreamMultiConnectCollectionCell.h"

@interface NELiveStreamMutiConnectView () <UICollectionViewDelegate,
                                           UICollectionViewDataSource,
                                           NELiveStreamMultiConnectCollectionDelegate>
@property(nonatomic, strong) UICollectionView *mutiConnectCollectionView;
@property(nonatomic, strong) NSArray<NELiveStreamSeatItem *> *dataArray;
@property(nonatomic, strong) NSMutableDictionary *videoViewMap;
@end

@implementation NELiveStreamMutiConnectView
static int buttonWH = 20;

- (instancetype)initWithDataSource:(NSArray *)dataArray frame:(CGRect)frame {
  if (self = [super initWithFrame:frame]) {
    _dataArray = dataArray;
    _videoViewMap = [NSMutableDictionary dictionary];
    [self loadSubviews];
  }
  return self;
}

// 加载子控件
- (void)loadSubviews {
  [self addSubview:self.mutiConnectCollectionView];
  [self.mutiConnectCollectionView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];
}

- (void)reloadDataSource:(NSArray<NELiveStreamSeatItem *> *)updateDataArray {
  NSMutableSet *currentUserIds = [NSMutableSet set];
  for (NELiveStreamSeatItem *item in updateDataArray) {
    if (item.user) {
      [currentUserIds addObject:item.user];
    }
  }

  NSMutableArray *keysToRemove = [NSMutableArray array];
  for (NSString *userId in self.videoViewMap.allKeys) {
    if (![currentUserIds containsObject:userId]) {
      [keysToRemove addObject:userId];
    }
  }
  [self.videoViewMap removeObjectsForKeys:keysToRemove];

  self.dataArray = updateDataArray;

  [self.mutiConnectCollectionView reloadData];
}

#pragma mark - UICollectionViewDataSource,Delegate

- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section {
  return self.dataArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                  cellForItemAtIndexPath:(NSIndexPath *)indexPath {
  NELiveStreamSeatItem *memberModel = self.dataArray[indexPath.row];
  NELiveStreamMultiConnectCollectionCell *multiVideoCell =
      [NELiveStreamMultiConnectCollectionCell settingCellWithCollectionView:collectionView
                                                                  indexPath:indexPath];
  __weak typeof(self) weakSelf = self;
  multiVideoCell.getVideoViewBlock = ^UIView *(NSString *userId) {
    return [weakSelf getVideoView:userId];
  };
  multiVideoCell.roleType = self.roleType;
  multiVideoCell.memberModel = memberModel;
  multiVideoCell.delegate = self;

  // 设置获取 videoView 的 block

  return multiVideoCell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView
                    layout:(UICollectionViewLayout *)collectionViewLayout
    sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
  return CGSizeMake(self.width, (self.height - 30) / 4);
}

#pragma mark - lazyMethod

- (UICollectionView *)mutiConnectCollectionView {
  if (!_mutiConnectCollectionView) {
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    flowLayout.minimumLineSpacing = 10;
    flowLayout.minimumInteritemSpacing = 15;
    _mutiConnectCollectionView = [[UICollectionView alloc] initWithFrame:CGRectZero
                                                    collectionViewLayout:flowLayout];
    _mutiConnectCollectionView.backgroundColor = [UIColor clearColor];
    _mutiConnectCollectionView.delegate = self;
    _mutiConnectCollectionView.dataSource = self;
    _mutiConnectCollectionView.showsVerticalScrollIndicator = NO;
    _mutiConnectCollectionView.scrollEnabled = NO;
    _mutiConnectCollectionView.keyboardDismissMode = UIScrollViewKeyboardDismissModeOnDrag;
    [_mutiConnectCollectionView registerClass:[NELiveStreamMultiConnectCollectionCell class]
                   forCellWithReuseIdentifier:[NELiveStreamMultiConnectCollectionCell description]];
  }
  return _mutiConnectCollectionView;
}

#pragma mark - NETSMultiConnectCollectionDelegate

- (void)didCloseConnectRoom:(NSString *)userId {
  if ([self.delegate respondsToSelector:@selector(disconnectRoomWithUserId:)]) {
    [self.delegate disconnectRoomWithUserId:userId];
  }
}

- (UIView *)getVideoView:(NSString *)userId {
  UIView *videoView = self.videoViewMap[userId];
  if (!videoView) {
    videoView = [[UIView alloc] init];
    videoView.backgroundColor = [UIColor blackColor];
    self.videoViewMap[userId] = videoView;
  }
  return videoView;
}

@end
