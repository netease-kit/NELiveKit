// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NETSMicManageViewController.h"
#import <Masonry/Masonry.h>
#import "NELiveStreamGlobalMacro.h"
#import "NELiveStreamUIKit/NELiveStreamUIKit-Swift.h"
#import "NETSConnectManageCell.h"
#import "NTESConstantKey.h"

@interface NETSMicManageViewController () <UITableViewDelegate,
                                           UITableViewDataSource,
                                           NETSMicManageViewDelegate>
@property(nonatomic, strong) UITableView *tableView;
@property(nonatomic, strong) NSMutableArray<NELiveStreamSeatItem *> *dataArray;
@property(nonatomic, copy) NSString *roomId;
@property(nonatomic, strong) NELiveStreamEmptyView *emptyView;

@end

@implementation NETSMicManageViewController

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
  self.dataArray = [[self.delegate didRequestMicManagerData] mutableCopy];
  [self updateEmptyView];
}

// 关闭视屏
- (void)didCloseVideo:(BOOL)isClose accountId:(nonnull NSString *)accountId {
}

// 关闭麦克风
- (void)didCloseMicrophone:(BOOL)isClose accountId:(nonnull NSString *)accountId {
}

- (void)didHangUpConnectAccountId:(NELiveStreamSeatItem *)hangUpModel {
  // 挂断连麦前弹出确认框
  NSString *userName = hangUpModel.userName ?: @"该用户";
  UIAlertController *alert = [UIAlertController
      alertControllerWithTitle:[NSString stringWithFormat:@"是否挂断与%@的连麦？", userName]
                       message:nil
                preferredStyle:UIAlertControllerStyleAlert];
  UIAlertAction *cancel = [UIAlertAction actionWithTitle:@"取消"
                                                   style:UIAlertActionStyleCancel
                                                 handler:nil];
  UIAlertAction *confirm = [UIAlertAction actionWithTitle:@"结束"
                                                    style:UIAlertActionStyleDestructive
                                                  handler:^(UIAlertAction *_Nonnull action) {
                                                    // 挂断连麦
                                                    [self.delegate onKickWithSeatItem:hangUpModel];
                                                  }];
  [alert addAction:cancel];
  [alert addAction:confirm];
  [self presentViewController:alert animated:YES completion:nil];
}

- (void)refreshData:(NSNotification *)notification {
  NSInteger index = [notification.userInfo[@"index"] integerValue];
  if (index == 2) {
    // 更新数据源
    [self nets_getNewData];
    [self.tableView reloadData];
  }
}

- (void)updateEmptyView {
  self.emptyView.hidden = self.dataArray.count > 0;
  self.tableView.hidden = self.dataArray.count == 0;
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
  NETSConnectManageCell *managerCell =
      [NETSConnectManageCell loadConnectManageCellWithTableView:tableView];
  managerCell.cellIndexPath = indexPath;
  managerCell.userModel = userModel;
  managerCell.delegate = self;
  return managerCell;
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
