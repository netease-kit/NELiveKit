// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NETSBaseTabViewCell.h"

NS_ASSUME_NONNULL_BEGIN

@class NELiveStreamSeatItem;

@protocol NETSMicRequestViewDelegate <NSObject>

/// 处理连麦申请
/// @param isAccept 是否接受
/// @param accountId 用户id
- (void)dealMicRequestAccept:(BOOL)isAccept userModel:(NELiveStreamSeatItem *)userModel;
@end

@interface NETSRequestConnectMicCell : NETSBaseTabViewCell
/// 加载cell
/// @param tableView tableview
+ (instancetype)loadRequestConnectMicCellWithTableView:(UITableView *)tableView;

@property(nonatomic, weak) id<NETSMicRequestViewDelegate> delegate;

@property(nonatomic, strong) NSIndexPath *cellIndexPath;

@property(nonatomic, strong) NELiveStreamSeatItem *userModel;

@end

NS_ASSUME_NONNULL_END
