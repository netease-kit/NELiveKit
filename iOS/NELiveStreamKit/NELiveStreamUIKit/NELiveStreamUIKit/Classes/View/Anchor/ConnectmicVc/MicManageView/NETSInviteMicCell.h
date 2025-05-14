// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NETSBaseTabViewCell.h"

NS_ASSUME_NONNULL_BEGIN

@class NELiveStreamSeatItem;
@protocol NETSInviteMicViewDelegate <NSObject>

/// 邀请上麦
/// @param audienceId 员工id
- (void)didInviteAudienceConnectMic:(NELiveStreamSeatItem *)userModel;

@end

@interface NETSInviteMicCell : NETSBaseTabViewCell

/// 加载cell
/// @param tableView tableview
+ (instancetype)loadInviteMicCellWithTableView:(UITableView *)tableView;

@property(nonatomic, weak) id<NETSInviteMicViewDelegate> delegate;

@property(nonatomic, strong) NSIndexPath *cellIndexPath;

@property(nonatomic, strong) NELiveStreamSeatItem *userModel;
@end

NS_ASSUME_NONNULL_END
