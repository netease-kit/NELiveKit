// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NETSInviteMicViewController.h"
#import <Masonry/Masonry.h>
#import "NELiveStreamGlobalMacro.h"
#import "NELiveStreamUIKit/NELiveStreamUIKit-Swift.h"
#import "NETSInviteMicCell.h"
#import "NTESConstantKey.h"

@interface NETSInviteMicViewController () <UITableViewDelegate,
                                           UITableViewDataSource,
                                           NETSInviteMicViewDelegate>
@property(nonatomic, strong) UITableView *tableView;
@property(nonatomic, copy) NSString *roomId;
@property(nonatomic, strong) NSMutableArray<NELiveStreamSeatItem *> *dataArray;
@property(nonatomic, strong) NELiveStreamEmptyView *emptyView;
@end

@implementation NETSInviteMicViewController

- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.
  [self nets_initializeConfig];
  [self nets_addSubViews];
  [self nets_getNewData];
}

- (void)nets_initializeConfig {
  self.roomId = (NSString *)self.params;
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(refreshData:)
                                               name:NotificationName_Anchor_RefreshSeats
                                             object:nil];
}

- (void)nets_addSubViews {
  self.view.backgroundColor = [UIColor whiteColor];
  [self.view addSubview:self.tableView];
  [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.left.right.equalTo(self.view);
    make.height.mas_equalTo(UIScreenHeight / 2);
  }];

  // 添加空视图
  [self.view addSubview:self.emptyView];
  [self.emptyView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.view);
    make.centerX.equalTo(self.view);
    make.top.equalTo(self.view).offset(80);
    make.height.mas_equalTo(160);
  }];
  self.emptyView.hidden = YES;
}

- (void)nets_getNewData {
  self.dataArray = [[self.delegate didRequestInviteMicData] mutableCopy];
  [self updateEmptyView];
}

- (void)refreshData:(NSNotification *)notification {
  NSInteger index = [notification.userInfo[@"index"] integerValue];
  if (index == 0) {
    // 更新数据源
    [self nets_getNewData];
    [self.tableView reloadData];
  }
}

- (void)updateEmptyView {
  self.emptyView.hidden = self.dataArray.count > 0;
  self.tableView.hidden = self.dataArray.count == 0;
}

#pragma mark - NETSMicRequestViewDelegate
- (void)didInviteAudienceConnectMic:(NELiveStreamSeatItem *)userModel {
  [self.delegate onInviteWithSeatItem:userModel];
}

#pragma mark - UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return self.dataArray.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return 52;
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  NELiveStreamSeatItem *userModel = self.dataArray[indexPath.row];
  NETSInviteMicCell *inviteCell = [NETSInviteMicCell loadInviteMicCellWithTableView:tableView];
  inviteCell.cellIndexPath = indexPath;
  inviteCell.delegate = self;
  inviteCell.userModel = userModel;
  return inviteCell;
}

#pragma mark - Get
- (UITableView *)tableView {
  if (!_tableView) {
    _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _tableView.delegate = self;
    _tableView.showsVerticalScrollIndicator = NO;
    _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    _tableView.dataSource = self;
    _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];
    _tableView.backgroundColor = [UIColor whiteColor];
  }
  return _tableView;
}

- (NELiveStreamEmptyView *)emptyView {
  if (!_emptyView) {
    _emptyView = [[NELiveStreamEmptyView alloc] init];
  }
  return _emptyView;
}

- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
