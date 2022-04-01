//
//  NETSAudienceMask.m
//  NLiteAVDemo
//
//  Created by Ease on 2020/11/25.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import "NETSGiftAnimationView.h"
#import "LOTAnimationView.h"
#import "NETSInviteeInfoView.h"
#import "NTESKeyboardToolbarView.h"
#import "NETSToast.h"
#import "NETSPkStatusBar.h"
#import "NETSLiveChatView.h"
#import "NETSAudienceSendGiftSheet.h"
#import "NETSAudienceMask.h"
#import "NETSAnchorTopInfoView.h"
#import "TopmostView.h"
#import "NETSAudienceBottomBar.h"


#import <NELivePlayerFramework/NELivePlayerNotication.h>
#import "NETSLiveUtils.h"
#import "NETSLiveApi.h"
#import "NENavigator.h"
#import "NETSAudienceNum.h"
#import "NETSFUManger.h"
#import <ReactiveObjC/ReactiveObjC.h>

@interface NETSAudienceMask ()
<
    NELiveListener,
    NETSAudienceBottomBarDelegate,
    NETSAudienceSendGiftSheetDelegate,
    NTESKeyboardToolbarDelegate
>

/// 主播信息
@property (nonatomic, strong)   NETSAnchorTopInfoView   *anchorInfo;
/// 直播中 观众数量视图
@property (nonatomic, strong)   NETSAudienceNum         *audienceInfo;
/// 聊天视图
@property (nonatomic, strong)   NETSLiveChatView        *chatView;
/// 底部视图
@property (nonatomic, strong)   NETSAudienceBottomBar   *bottomBar;
/// 键盘工具条
@property (nonatomic, strong)   NTESKeyboardToolbarView *toolBar;
/// pk状态条
@property (nonatomic, strong)   NETSPkStatusBar         *pkStatusBar;
/// 被邀请者信息视图
@property (nonatomic, strong)   NETSInviteeInfoView     *inviteeInfo;

/// pk胜利图标
@property (nonatomic, strong)   UIImageView     *pkSuccessIco;
/// pk失败图标
@property (nonatomic, strong)   UIImageView     *pkFailedIco;
/// 礼物动画控件
@property (nonatomic, strong)   NETSGiftAnimationView   *giftAnimation;
//当前主播角色类型，邀请者还是被邀请者
@property(nonatomic, assign) bool isInviter;

@end

@implementation NETSAudienceMask

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self addSubview:self.anchorInfo];
        [self addSubview:self.audienceInfo];
        [self addSubview:self.chatView];
        [self addSubview:self.bottomBar];
        [self addSubview:self.toolBar];
        [self bringSubviewToFront:self.toolBar];
        [self _bindEvent];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didPlayerFrameChanged:) name:NELivePlayerVideoSizeChangedNotification object:nil];
    }
    return self;
}


- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [NELiveKit shared].listener = nil;
}

- (void)_bindEvent {
    @weakify(self);
    RACSignal *roomSignal = RACObserve(self, room);
    [roomSignal subscribeNext:^(NELiveDetail *x) {
        @strongify(self);
        if (x == nil) { return; }
        // TODO: chatroomid
//        [self _refreshAudienceInfoWitHRoomId:x.live.chatRoomId];
        self.anchorInfo.nickname = x.anchor.userUuid;
        self.anchorInfo.avatarUrl = x.anchor.avatar;
    }];

    [[roomSignal zipWith:RACObserve(self, info)] subscribeNext:^(RACTuple *tuple) {
        @strongify(self);
        NELiveDetail *room = (NELiveDetail *)tuple.first;
        NELiveDetail *info = (NELiveDetail *)tuple.second;
        if (room && info) {
            // 更新主播云币
            self.anchorInfo.wealth = info.live.rewardTotal;
        }
    }];
}

- (void)layoutSubviews
{
    self.anchorInfo.frame = CGRectMake(8, (kIsFullScreen ? 44 : 20) + 4, 124, 36);
    self.audienceInfo.frame = CGRectMake(kScreenWidth - 8 - 195, self.anchorInfo.top + (36 - 28) / 2.0, 195, 28);
    CGFloat chatViewHeight = [self _chatViewHeight];
    self.chatView.frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - chatViewHeight, kScreenWidth - 16 - 60 - 20, chatViewHeight);
    self.bottomBar.frame = CGRectMake(0, kScreenHeight - (kIsFullScreen ? 34 : 0) - 36 - 14, kScreenWidth, 36);
}

- (void)setRoom:(NELiveDetail *)room {
    _room = room;
//    self.bottomBar.roomType = self.room.live.type;

}

-(void)setInfo:(NELiveDetail *)info  {
    [NELiveKit shared].listener = self;
    [_audienceInfo reloadWithDatas:[NELiveKit shared].members];
    self.anchorInfo.wealth = info.live.rewardTotal;
    if (info.live.live == NELiveStatusPking || info.live.live == NELiveStatusPunishing) {
        [[NELiveKit shared] fetchPKInfoWithLiveRecordId:info.live.liveRecordId callback:^(NSInteger code2, NSString * _Nullable msg2, NEPKDetail * _Nullable detail) {
            // 进去已经在PK了
            ntes_main_async_safe(^{
                [self refreshWithRoom:detail];
            });
        }];
    }else {
        [self.inviteeInfo removeFromSuperview];
        [self.pkStatusBar removeFromSuperview];
        [self.pkSuccessIco removeFromSuperview];
        [self.pkFailedIco removeFromSuperview];
    }
}

- (void)refreshWithRoom:(NEPKDetail *)currentPkInfo {

    //判断当前房间是否是邀请者
    if ([self.room.anchor.userUuid isEqualToString:currentPkInfo.inviter.userUuid]) {
        self.isInviter = YES;
    }else {
        self.isInviter = NO;
    }

    [self _layoutPkStatusBar];

    // TODO: 改成枚举
    if (currentPkInfo.state == 1) {
        // pk状态栏变更
        // pk开始: 启动倒计时,刷新内容
        [self.pkStatusBar countdownWithSeconds:currentPkInfo.countDown prefix:@"PK "];

        // TODO: 头像
        if (_isInviter) {
            [self.pkStatusBar refreshWithLeftRewardCoins:currentPkInfo.inviterReward.rewardCoinTotal leftRewardAvatars:@[] rightRewardCoins:currentPkInfo.inviteeReward.rewardCoinTotal rightRewardAvatars:@[]];
            [self _layoutOtherAnchorWithAvatar:currentPkInfo.invitee.avatar nickname:currentPkInfo.invitee.userUuid];
        }else {
            [self.pkStatusBar refreshWithLeftRewardCoins:currentPkInfo.inviteeReward.rewardCoinTotal leftRewardAvatars:@[] rightRewardCoins:currentPkInfo.inviterReward.rewardCoinTotal rightRewardAvatars:@[]];
            [self _layoutOtherAnchorWithAvatar:currentPkInfo.inviter.avatar nickname:currentPkInfo.inviter.userUuid];
        }

    }else if(currentPkInfo.state == 7){

        if (_isInviter) {
            [self.pkStatusBar refreshWithLeftRewardCoins:currentPkInfo.inviterReward.rewardCoinTotal leftRewardAvatars:@[] rightRewardCoins:currentPkInfo.inviteeReward.rewardCoinTotal rightRewardAvatars:@[]];
            [self _layoutOtherAnchorWithAvatar:currentPkInfo.invitee.avatar nickname:currentPkInfo.invitee.userUuid];
        }else {
            [self.pkStatusBar refreshWithLeftRewardCoins:currentPkInfo.inviteeReward.rewardCoinTotal leftRewardAvatars:@[] rightRewardCoins:currentPkInfo.inviterReward.rewardCoinTotal rightRewardAvatars:@[]];
            [self _layoutOtherAnchorWithAvatar:currentPkInfo.inviter.avatar nickname:currentPkInfo.inviter.userUuid];
        }

        // 获取pk结果
        NETSPkResult res = NETSPkUnknownResult;
        if (currentPkInfo.invitee.rewardTotal == currentPkInfo.inviter.rewardTotal) {
            res = NETSPkTieResult;
        }else if ((currentPkInfo.invitee.rewardTotal > currentPkInfo.inviter.rewardTotal && !self.isInviter) ||
                 (currentPkInfo.invitee.rewardTotal < currentPkInfo.inviter.rewardTotal && self.isInviter)) {
            res = NETSPkCurrentAnchorWin;
        }else {
            res = NETSPkOtherAnchorWin;
        }

        if (res == NETSPkTieResult) {
            [self.pkStatusBar stopCountdown];
        } else {
            [self.pkStatusBar countdownWithSeconds:currentPkInfo.countDown prefix:@"惩罚 "];
        }
        //显示pk结果
        [self _layoutPkResultWhenGetCurrentAnchorWin:res];
    }
}


#pragma mark - private method


/// 获取聊天视图高度
- (CGFloat)_chatViewHeight
{
    if (kScreenHeight <= 568) {
        return 100;
    } else if (kScreenHeight <= 736) {
        return 130;
    }
    return 204;
}

/// 布局另一个主播信息视图
- (void)_layoutOtherAnchorWithAvatar:(NSString *)avatar nickname:(NSString *)nickname {
    
    if ([NELiveKit shared].isPKing)  {
        CGFloat topOffset = 72 + (kIsFullScreen ? 44 : 20);
        self.inviteeInfo.frame = CGRectMake(self.right - 8 - 82, topOffset, 82, 24);
        [self.inviteeInfo reloadAvatar:avatar nickname:nickname];
        [self addSubview:self.inviteeInfo];
    } else {
        [self.inviteeInfo removeFromSuperview];
    }
}

/// 布局pk状态条
- (void)_layoutPkStatusBar {
    if ([NELiveKit shared].isPKing) {
        CGFloat topOffset = (kIsFullScreen ? 44 : 20) + 44 + 20 + kScreenWidth * 640 / 720.0;
        CGRect rect = CGRectMake(0, topOffset, self.width, 58);
        self.pkStatusBar.frame = rect;
        [self addSubview:self.pkStatusBar];
        
        [self bringSubviewToFront:self.pkStatusBar];
        [self bringSubviewToFront:self.toolBar];
    }
}


/// 布局胜负标志: pk阶段结束,返回pk结果
- (void)_layoutPkResultWhenGetCurrentAnchorWin:(NETSPkResult)pkResult {
    
    CGFloat top = 64 + (kIsFullScreen ? 44 : 20) + kScreenWidth * 0.5 * 640 / 360.0 - 100;
    CGRect leftIcoFrame = CGRectMake((kScreenWidth * 0.5 - 100) * 0.5, top, 100, 100);
    CGRect rightIcoFrame = CGRectMake(kScreenWidth * 0.5 + (kScreenWidth * 0.5 - 100) * 0.5, top, 100, 100);
    
    self.pkSuccessIco.image = [UIImage imageNamed:@"pk_succeed_ico"];
    self.pkFailedIco.image = [UIImage imageNamed:@"pk_failed_ico"];
    
    switch (pkResult) {
        case NETSPkCurrentAnchorWin:
        {
            self.pkSuccessIco.frame = leftIcoFrame;
            self.pkFailedIco.frame = rightIcoFrame;
        }
            break;
        case NETSPkOtherAnchorWin:
        {
            self.pkSuccessIco.frame = rightIcoFrame;
            self.pkFailedIco.frame = leftIcoFrame;
        }
            break;
        case NETSPkTieResult:
        {
            self.pkSuccessIco.image = [UIImage imageNamed:@"pk_tie_ico"];
            self.pkFailedIco.image = [UIImage imageNamed:@"pk_tie_ico"];
            
            self.pkSuccessIco.frame = leftIcoFrame;
            self.pkFailedIco.frame = rightIcoFrame;
        }
            break;
            
        default:
            break;
    }
    
    [self addSubview:self.pkSuccessIco];
    [self addSubview:self.pkFailedIco];
}

/// 布局胜负标志: 惩罚结束(pk结束)
- (void)_layoutPkResultWhenPunishmentEnd
{
    [self.pkSuccessIco removeFromSuperview];
    [self.pkFailedIco removeFromSuperview];
    [self.inviteeInfo removeFromSuperview];
}

// 获取打赏列表
- (void)_fetchRewardListWithLiveCid:(NSInteger)liveCid pkId:(NSString *)pkId successBlock:(void(^)(NEPKRewardTop *))successBlock failedBlock:(void(^)(NSInteger, NSString *))failedBlock
{
    [[NELiveKit shared] fetchRewardTopListWithLiveRecordId:liveCid pkId:pkId callback:^(NSInteger code, NSString * _Nullable msg, NEPKRewardTop * _Nullable top) {
        if (top) {
            successBlock(top);
        } else {
            failedBlock(code, msg);
        }
    }];
}

/// 点击屏幕收起键盘
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    [self endEditing:true];
    [self.toolBar resignFirstResponder];
    [self.bottomBar resignFirstResponder];
}

/// 播放礼物动画
- (void)_playGiftWithName:(NSString *)name
{
    [self addSubview:self.giftAnimation];
    [self bringSubviewToFront:self.giftAnimation];
    [self.giftAnimation addGift:name];
}


#pragma mark - 当键盘事件

- (void)keyboardWillShow:(NSNotification *)aNotification
{
    NSDictionary *userInfo = [aNotification userInfo];
    NSValue *aValue = [userInfo objectForKey:UIKeyboardFrameEndUserInfoKey];
    CGRect keyboardRect = [aValue CGRectValue];
    float keyBoardHeight = keyboardRect.size.height;
    CGFloat chatViewHeight = [self _chatViewHeight];
    [UIView animateWithDuration:0.1 animations:^{
        self.chatView.frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - chatViewHeight - keyBoardHeight - 50, kScreenWidth - 16 - 60 - 20, chatViewHeight);
        self.toolBar.frame = CGRectMake(0, kScreenHeight - keyBoardHeight - 50, kScreenWidth, 50);
    }];
    [self bringSubviewToFront:self.toolBar];
}

- (void)keyboardWillHide:(NSNotification *)aNotification {
    CGFloat chatViewHeight = [self _chatViewHeight];
    [UIView animateWithDuration:0.1 animations:^{
        self.chatView.frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - chatViewHeight, kScreenWidth - 16 - 60 - 20, chatViewHeight);
        self.toolBar.frame = CGRectMake(0, kScreenHeight + 50, kScreenWidth, 50);
    }];
}

/// 刷新观众信息
- (void)_refreshAudienceInfoWitHRoomId:(NSString *)roomId {
    // TODO: 观众信息
//    [NETSChatroomService fetchMembersRoomId:roomId limit:10 successBlock:^(NSArray<NIMChatroomMember *> * _Nullable members) {
//        YXAlogInfo(@"members: %@", members);
//        [self.audienceInfo reloadWithDatas:members];
//    } failedBlock:^(NSError * _Nonnull error) {
//        YXAlogInfo(@"观众端获取IM聊天室成员失败, error: %@", error);
//    }];
}

#pragma mark - 播放器通知

- (void)didPlayerFrameChanged:(NSNotification *)notification
{
    NSDictionary *userInfo = [notification userInfo];

    if (self.delegate && [self.delegate respondsToSelector:@selector(didChangeRoomStatus:)]) {
        CGFloat height = [userInfo[NELivePlayerVideoHeightKey] floatValue];
        CGFloat width = [userInfo[NELivePlayerVideoWidthKey] floatValue];

        NETSAudienceStreamStatus status = NETSAudienceStreamDefault;
        if (height == 640 && width == 720) {
            status = NETSAudienceStreamMerge;
        }
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.25 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self.delegate didChangeRoomStatus:status];
        });
    }
//    YXAlogInfo(@"video size changed, width: %@, height: %@", userInfo[NELivePlayerVideoWidthKey] ?: @"-", userInfo[NELivePlayerVideoHeightKey] ?: @"-");
}

#pragma mark - NEPkChatroomMsgHandleDelegate 聊天室代理

- (void)onMembersJoinWithMembers:(NSArray<NERoomMember *> *)members {
    [self.audienceInfo reloadWithDatas:[NELiveKit shared].members];
    NSMutableString *nick = [NSMutableString string];
    for (NERoomMember *member in members) {
        //        [nick appendFormat:@"%@,", member.name.length ? member.name : member.userUuid];
        // 先全部使用userUuid
        [nick appendFormat:@"%@,", member.userUuid];
    }
    if ([nick hasSuffix:@","]) {
        nick = [nick substringToIndex:nick.length - 1];
    }
    if (nick.length) {
        // 提示非聊天室创建者 加入/离开 消息
        NETSMessageModel *message = [[NETSMessageModel alloc] init];
        message.type = NETSMessageNotication;
        message.notication = [NSString stringWithFormat:NSLocalizedString(@"\"%@\" 加入房间", nil), nick];
        [self.chatView addMessages:@[message]];
    }
}

- (void)onMembersLeaveWithMembers:(NSArray<NERoomMember *> *)members {
    [self.audienceInfo reloadWithDatas:[NELiveKit shared].members];
    NSMutableString *nick = [NSMutableString string];
    for (NERoomMember *member in members) {
        //        [nick appendFormat:@"%@,", member.name.length ? member.name : member.userUuid];
        // 先全部使用userUuid
        [nick appendFormat:@"%@,", member.userUuid];
    }
    if ([nick hasSuffix:@","]) {
        nick = [nick substringToIndex:nick.length - 1];
    }
    if (nick.length) {
        // 提示非聊天室创建者 加入/离开 消息
        NETSMessageModel *message = [[NETSMessageModel alloc] init];
        message.type = NETSMessageNotication;
        message.notication = [NSString stringWithFormat:NSLocalizedString(@"\"%@\" 离开房间", nil), nick];
        [self.chatView addMessages:@[message]];
    }
}

- (void)onLiveEndWithReason:(NSInteger)reason {
    [self _liveRoomClosed];
}

- (void)onPKStartWithPkStartTime:(NSInteger)pkStartTime pkCountDown:(NSInteger)pkCountDown inviter:(NELivePKAnchor *)inviter invitee:(NELivePKAnchor *)invitee {
    // pk开始:通知外围变更播放器frame
    if (self.delegate && [self.delegate respondsToSelector:@selector(didChangeRoomStatus:)]) {
        [self.delegate didChangeRoomStatus:NETSAudienceIMPkStart];
    }
    if ([inviter.userUuid isEqualToString:[NELiveKit shared].liveDetail.anchor.userUuid]) {
        self.isInviter = true;
        [self _layoutOtherAnchorWithAvatar:invitee.avatar nickname:invitee.userName.length ? invitee.userName : invitee.userUuid];
    } else {
        self.isInviter = false;
        [self _layoutOtherAnchorWithAvatar:inviter.avatar nickname:inviter.userName.length ? inviter.userName : inviter.userUuid];
    }
    
    // pk状态栏变更
    [self _layoutPkStatusBar];
    // pk开始: 启动倒计时,刷新内容
    [self.pkStatusBar countdownWithSeconds:pkCountDown prefix:@"PK "];
    [self.pkStatusBar refreshWithLeftRewardCoins:0 leftRewardAvatars:@[] rightRewardCoins:0 rightRewardAvatars:@[]];
}

- (void)onPKPunishingStartWithPkPenaltyCountDown:(NSInteger)pkPenaltyCountDown inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards {
    // 获取pk结果
    NETSPkResult res = NETSPkUnknownResult;
    if (inviteeRewards == inviterRewards) {
        res = NETSPkTieResult;
    }else if ((inviteeRewards > inviterRewards && !self.isInviter) ||
             (inviteeRewards < inviterRewards && self.isInviter)) {
        res = NETSPkCurrentAnchorWin;
    }else {
        res = NETSPkOtherAnchorWin;
    }
    
    if (res == NETSPkTieResult) {
        [self.pkStatusBar stopCountdown];
    } else {
        [self.pkStatusBar countdownWithSeconds:pkPenaltyCountDown prefix:@"惩罚 "];
    }
    //显示pk结果
    [self _layoutPkResultWhenGetCurrentAnchorWin:res];
}

- (void)onPKEndWithReason:(NSInteger)reason pkEndTime:(NSInteger)pkEndTime inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards countDownEnd:(BOOL)countDownEnd {
    [self.pkStatusBar stopCountdown];
    [self.pkStatusBar removeFromSuperview];
    if (self.delegate && [self.delegate respondsToSelector:@selector(didChangeRoomStatus:)]) {
        ntes_main_async_safe(^{
            [self.delegate didChangeRoomStatus:NETSAudienceStreamDefault];
        });
    }
    [self _layoutPkResultWhenPunishmentEnd];
}

- (void)onRewardReceivedWithRewarderUserUuid:(NSString *)rewarderUserUuid rewarderUserName:(NSString *)rewarderUserName giftId:(NSInteger)giftId anchorReward:(NELiveAnchorReward *)anchorReward otherAnchorReward:(NELiveAnchorReward *)otherAnchorReward {
    if ([[NELiveKit shared].liveDetail.anchor.userUuid isEqualToString:anchorReward.userUuid]) {
        // 打赏给当前主播的
        if ([NELiveKit shared].pkStatus == NELivePKStatusPking) {
            [self.pkStatusBar refreshWithLeftRewardCoins:anchorReward.pkRewardTotal leftRewardAvatars:anchorReward.rewardAvatars rightRewardCoins:otherAnchorReward.pkRewardTotal rightRewardAvatars:otherAnchorReward.rewardAvatars];
        }
        // 展示礼物动画
        NETSGiftModel *giftModel = [NETSLiveUtils getRewardWithGiftId:giftId];
        if (giftModel) {
            NSString *giftName = [NSString stringWithFormat:@"anim_gift_0%lld", giftId];
            [self _playGiftWithName:giftName];
        }
    
        // 更新用户信息栏(云币值)
        self.anchorInfo.wealth = anchorReward.rewardTotal;
        
        //如果打赏的是当前主播,向聊天室发送打赏消息
        NETSMessageModel *message = [[NETSMessageModel alloc] init];
        message.type = NETSMessageReward;
        message.giftId = giftId;
        message.giftFrom = rewarderUserName.length ? rewarderUserName : rewarderUserUuid;
        [self.chatView addMessages:@[message]];
    } else if ([[NELiveKit shared].liveDetail.anchor.userUuid isEqualToString:otherAnchorReward.userUuid]) {
        // 打赏给其他主播的
        if ([NELiveKit shared].pkStatus == NELivePKStatusPking) {
            [self.pkStatusBar refreshWithLeftRewardCoins:otherAnchorReward.pkRewardTotal leftRewardAvatars:otherAnchorReward.rewardAvatars rightRewardCoins:anchorReward.pkRewardTotal rightRewardAvatars:anchorReward.rewardAvatars];
        }
    }
}

- (void)onMessagesReceivedWithMessages:(NSArray<NERoomMessage *> *)messages {
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:messages.count];
    for (NERoomMessage *message in messages) {
        NETSMessageModel *model = [[NETSMessageModel alloc] init];
        model.type = NETSMessageNormal;
        model.text = message.text;
        model.sender = message.from.userUuid;
        if ([message.from.userUuid isEqualToString:[NELiveKit shared].liveDetail.anchor.userUuid]) {
            model.isAnchor = true;
        }
        [array addObject:model];
    }
    [self.chatView addMessages:array];
}


/// 直播间关闭
- (void)_liveRoomClosed
{
    // 调用代理
    if (_delegate && [_delegate respondsToSelector:@selector(didLiveRoomClosed)]) {
        [_delegate didLiveRoomClosed];
    }
    self.chatRoomAvailable = NO;
}

#pragma mark - NETSAudienceBottomBarDelegate 底部工具条代理

- (void)clickTextLabel:(UILabel *)label
{
    NSLog(@"点击输入框");
    [self.toolBar becomeFirstResponse];
}

- (void)clickGiftBtn
{
    NSLog(@"点击礼物");
    NSArray *gifts = [NETSLiveUtils defaultGifts];
    [NETSAudienceSendGiftSheet showWithTarget:self gifts:gifts];
    
}

- (void)clickCloseBtn {
    [[NELiveKit shared] leaveLiveWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        ntes_main_async_safe(^{
            if (code == 0) {
                
            } else {
                [NETSToast showToast:[NSString stringWithFormat:@"离开直播失败 code:%zd msg:%@", code, msg]];
            }
            [[NENavigator shared].navigationController popViewControllerAnimated:YES];
        });
    }];
}

#pragma mark -  NETSAudienceSendGiftSheetDelegate 打赏面板代理事件

- (void)didSendGift:(NETSGiftModel *)gift onSheet:(NETSAudienceSendGiftSheet *)sheet
{
    [sheet dismiss];
    [[NELiveKit shared] rewardWithGiftId:gift.giftId callback:^(NSInteger code, NSString * _Nullable msg) {
        if (code != 0) {
            [NETSToast showToast:[NSString stringWithFormat:@"打赏失败 code:%zd msg:%@", code, msg]];
        }
    }];
}

#pragma mark - NETSKeyboardToolbarDelegate

- (void)didToolBarSendText:(NSString *)text
{
    if (isEmptyString(text)) {
        [NETSToast showToast:NSLocalizedString(@"所发消息为空", nil)];

        return;
    }
    [[NELiveKit shared] sendTextMessageWithMessage:text callback:^(NSInteger code, NSString * _Nullable msg) {
        if (code != 0) {
            [NETSToast showToast:[NSString stringWithFormat:@"发送消息失败 code:%zd msg:%@", code, msg]];
        } else {
            NETSMessageModel *model = [[NETSMessageModel alloc] init];
            model.type = NETSMessageNormal;
            model.text = text;
            model.sender = [NELiveKit shared].userUuid;
            model.isAnchor = false;
            [_chatView addMessages:@[model]];
        }
    }];
}

// 关闭直播间
- (void)closeChatRoom {
    [self _liveRoomClosed];
}

- (void)closeConnectMicRoom {
    
//    if (_requestConnectMicBar) {
//        [self.requestConnectMicBar dismiss];
//    }
//    [[NENavigator shared].navigationController dismissViewControllerAnimated:YES completion:nil];

}

- (void)clearCurrentLiveRoomData {
    [self.chatView clearData];
    if ([NELiveKit shared].pkStatus == NELivePKStatusPking) {
        [self.inviteeInfo removeFromSuperview];
        [self.pkStatusBar removeFromSuperview];
    }else if ([NELiveKit shared].pkStatus == NELivePKStatusPunishing) {
        [self.pkSuccessIco removeFromSuperview];
        [self.pkFailedIco removeFromSuperview];
    }
}



#pragma mark - lazy load

- (NETSAnchorTopInfoView *)anchorInfo
{
    if (!_anchorInfo) {
        _anchorInfo = [[NETSAnchorTopInfoView alloc] init];
    }
    return _anchorInfo;
}

- (NETSAudienceNum *)audienceInfo
{
    if (!_audienceInfo) {
        _audienceInfo = [[NETSAudienceNum alloc] initWithFrame:CGRectZero];
    }
    return _audienceInfo;
}

- (NETSLiveChatView *)chatView
{
    if (!_chatView) {
        CGRect frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - 204, kScreenWidth - 16 - 60 - 20, 204);
        _chatView = [[NETSLiveChatView alloc] initWithFrame:frame];
    }
    return _chatView;
}

- (NETSAudienceBottomBar *)bottomBar
{
    if (!_bottomBar) {
        _bottomBar = [[NETSAudienceBottomBar alloc] init];
        _bottomBar.delegate = self;
    }
    return _bottomBar;
}

- (NTESKeyboardToolbarView *)toolBar
{
    if (!_toolBar) {
        _toolBar = [[NTESKeyboardToolbarView alloc] initWithFrame:CGRectMake(0, kScreenHeight, kScreenWidth, 50)];
        _toolBar.backgroundColor = UIColor.whiteColor;
        _toolBar.cusDelegate = self;
    }
    return _toolBar;
}

- (NETSPkStatusBar *)pkStatusBar
{
    if (!_pkStatusBar) {
        _pkStatusBar = [[NETSPkStatusBar alloc] init];
    }
    return _pkStatusBar;
}

- (NETSInviteeInfoView *)inviteeInfo
{
    if (!_inviteeInfo) {
        _inviteeInfo = [[NETSInviteeInfoView alloc] init];
    }
    return _inviteeInfo;
}

- (UIImageView *)pkSuccessIco
{
    if (!_pkSuccessIco) {
        _pkSuccessIco = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 100, 100)];
    }
    return _pkSuccessIco;
}

- (UIImageView *)pkFailedIco
{
    if (!_pkFailedIco) {
        _pkFailedIco = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 100, 100)];
    }
    return _pkFailedIco;
}

- (NETSGiftAnimationView *)giftAnimation
{
    if (!_giftAnimation) {
        _giftAnimation = [[NETSGiftAnimationView alloc] init];
    }
    return _giftAnimation;
}

@end
