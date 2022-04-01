//
//  NETSAudienceChatRoomCell.m
//  NLiteAVDemo
//
//  Created by 徐善栋 on 2021/1/7.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import "NETSAudienceChatRoomCell.h"
#import <NELivePlayerFramework/NELivePlayerFramework.h>
#import "NTESKeyboardToolbarView.h"
#import "NETSAnchorTopInfoView.h"
#import "NETSAudienceNum.h"
#import "NETSLiveChatView.h"
#import "NETSPkStatusBar.h"
#import "NETSInviteeInfoView.h"
#import "NETSGiftAnimationView.h"
#import "NETSAudienceBottomBar.h"
#import "NETSLiveUtils.h"
#import "NETSAudienceSendGiftSheet.h"
#import "NETSAudienceMask.h"
#import "NETSPullStreamErrorView.h"
#import "NETSLiveEndView.h"
#import "TopmostView.h"
#import "Reachability.h"

/// 观众端直播间状态
typedef NS_ENUM(NSInteger, NETSAudienceRoomStatus) {
    NETSAudienceRoomInit        = 0,    // 直播间初始化
    NETSAudienceRoomPullStream  = 1,    // 直播正在拉流
    NETSAudienceRoomPlaying     = 2,    // 直播中
    NETSAudienceRoomLiveClosed  = 3,    // 直播结束
    NETSAudienceRoomLiveError   = 4     // 发生错误
};

@interface NETSAudienceChatRoomCell ()<NETSAudienceBottomBarDelegate, NETSPullStreamErrorViewDelegate, NETSAudienceMaskDelegate>

@property (nonatomic, strong) NELiveDetail     *roomInfo;
/// 蒙层
@property (nonatomic, strong) NETSAudienceMask          *mask;
/// 播放器
@property(nonatomic, strong) NELivePlayerController     *player;
/// 断网视图
@property(nonatomic, strong) NETSPullStreamErrorView    *networkFailureView;
/// 直播结束蒙层
@property(nonatomic, strong) NETSLiveEndView            *liveClosedMask;
/// 网络监测类
@property(nonatomic, strong) Reachability               *reachability;
/// 直播间状态
@property(nonatomic, assign) NETSAudienceRoomStatus     roomStatus;
//断网标记
@property(nonatomic, assign) BOOL isBrokenNetwork;

@property (nonatomic, copy) NSString *rtmpPullUrl;

@end

@implementation NETSAudienceChatRoomCell

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {

        self.clipsToBounds = YES;
        self.backgroundColor = [UIColor blackColor];
        
        self.reachability = [Reachability reachabilityWithHostName:@"www.baidu.com"];
        [self.reachability startNotifier];
        self.mask.chatRoomAvailable = [self.reachability isReachable];
        
        // 播放器视图添加向左轻扫动作
        UISwipeGestureRecognizer *swipeLeft = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(_swipeShowMask:)];
        swipeLeft.direction = UISwipeGestureRecognizerDirectionLeft;
        [self.contentView addGestureRecognizer:swipeLeft];

        UISwipeGestureRecognizer *swipeRight = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(_swipeDismissMask:)];
        swipeRight.direction = UISwipeGestureRecognizerDirectionRight;
        [self.contentView addGestureRecognizer:swipeRight];
        
        // 播放器相关通知
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerPlaybackFinishedNotification:) name:NELivePlayerPlaybackFinishedNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerRetryNotification:) name:NELivePlayerRetryNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerFirstVideoDisplayedNotification:) name:NELivePlayerFirstVideoDisplayedNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerLoadStateChangedNotification:) name:NELivePlayerLoadStateChangedNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerPlaybackStateChangedNotification:) name:NELivePlayerPlaybackStateChangedNotification object:nil];
        // 监测网络
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reachabilityChanged:) name:kReachabilityChangedNotification object:nil];
    }
    return self;
}

//切换到观众视图
- (void)changeToAudience {
    //重新添加播放器视图
    [self _layoutPlayerWithY:0];
    // TODO: rtmp
    [self _playWithUrl:self.rtmpPullUrl];
}

- (void)prepareForReuse {
    [super prepareForReuse];
    [self shutdownPlayer];
}

#pragma mark - public method

- (void)resetPageUserinterface
{
    // 添加mask之前先移除关闭直播的蒙版
    self.roomStatus = NETSAudienceRoomPullStream;
    [self.liveClosedMask removeFromSuperview];
    self.mask.left = 0;
    [self.mask clearCurrentLiveRoomData];
}

- (void)shutdownPlayer {
    if (!_player) { return; }
    [self.player pause];
    [self.player shutdown];
    [self.player.view removeFromSuperview];
    self.player = nil;
}

- (void)closeConnectMicRoomAction {
//    [self.mask removeFromSuperview];
//    if (_connectMicView) {
//        [self disconnectRoomWithUserId:self.roomModel.anchor.accountId];
//        [self userLeaveRtcRoomAction];
//    }
}

#pragma mark - get/set

- (void)setRoomModel:(NELiveDetail *)roomModel {
    _roomModel = roomModel;
    [self.contentView addSubview:self.mask];
    self.mask.room = roomModel;
    
    [self _obtainChatroomInfo:roomModel isNeedEnterChatRoom:YES];
}

#pragma mark - NETSPullStreamErrorViewDelegate

/// 点击返回
- (void)clickBackAction {
    [[NENavigator shared].navigationController popViewControllerAnimated:YES];
}

/// 重新连接
- (void)clickRetryAction {
    
    if (self.isBrokenNetwork) {
        [NETSToast showToast:NSLocalizedString(@"网络异常", nil)];
        return;
    }
    [self.networkFailureView removeFromSuperview];
    // TODO: 状态
//    self.mask.roomStatus = NETSAudienceRoomPullStream;
    [self _obtainChatroomInfo:self.roomModel isNeedEnterChatRoom:YES];
}

#pragma mark - Notification Method

- (void)reachabilityChanged:(NSNotification *)note {
    Reachability *currentReach = [note object];
    NSCParameterAssert([currentReach isKindOfClass:[Reachability class]]);
    NetworkStatus netStatus = [currentReach currentReachabilityStatus];
    switch (netStatus) {
        case NotReachable:{// 网络不可用
//            YXAlogInfo(@"断网了");
            [self _showLiveRoomErrorView];
            [self shutdownPlayer];
            self.isBrokenNetwork = YES;
        }
            break;

        default:{
            self.isBrokenNetwork = NO;
        }
            break;
    }
}

#pragma mark - player notification

/// 播放器播放完成或播放发生错误时的消息通知
- (void)playerPlaybackFinishedNotification:(NSNotification *)notification
{
    NSDictionary *info = notification.userInfo;
    [self _playerLoadErrorInfo:info];
    NSLog(@"观众端 播放器播放完成或播放发生错误时的消息通知, info: %@", info);
}

/// 播放器失败重试通知
- (void)playerRetryNotification:(NSNotification *)notification
{
    NSDictionary *info = notification.userInfo;
    [self _playerLoadErrorInfo:info];
    NSLog(@"观众端 播放器重试加载, info: %@", info);
}

/// 播放器第一帧视频显示时的消息通知
- (void)playerFirstVideoDisplayedNotification:(NSNotification *)notification
{
    NSLog(@"观众端 播放器首帧播放");
    self.roomStatus = NETSAudienceRoomPlaying;
}

/// 播放器加载状态发生改变时的消息通知
- (void)playerLoadStateChangedNotification:(NSNotification *)notification
{
    NSLog(@"观众端 播放器加载状态发生改变时的消息通知, info: %ld", (long)_player.loadState);
}

/// 播放器播放状态发生改变时的消息通知
- (void)playerPlaybackStateChangedNotification:(NSNotification *)notification
{
    NSLog(@"观众端 播放器播放状态发生改变时的消息通知, playbackState: %ld", (long)_player.playbackState);
}

/// 播放器加载错误处理
- (void)_playerLoadErrorInfo:(NSDictionary *)info
{
    NSLog(@"观众端 处理播放器通知参数, info: %@", info);
    // 播放器播放结束原因的key
    NELPMovieFinishReason reason = [info[NELivePlayerPlaybackDidFinishReasonUserInfoKey] integerValue];
    if (reason != NELPMovieFinishReasonPlaybackError) {
        return;
    }
    // 播放成功时，此字段为nil。播放器播放结束具体错误码。具体至含义见NELPPLayerErrorCode
    NELPPLayerErrorCode errorCode = [info[NELivePlayerPlaybackDidFinishErrorKey] integerValue];
    if (errorCode != 0) {
//        [self _showLiveRoomErrorView];
    }
}

#pragma mark - NETSAudienceMaskDelegate

- (void)didChangeRoomStatus:(NETSAudienceStreamStatus)status
{
    // 视频上边缘距离设备顶部
    CGFloat top = 64 + (kIsFullScreen ? 44 : 20);
    // 获取播放器视图
    UIView *playerView = self.player.view;
    switch (status) {
        case NETSAudienceIMPkStart:
        {
            CGFloat scale = 1280 / 720.0;
            playerView.frame = CGRectMake(0, top, kScreenWidth * 0.5, kScreenWidth * 0.5 * scale);
        }
            break;
        case NETSAudienceStreamMerge:
        {
            CGFloat y = top - (kScreenHeight - (640 / 720.0 * kScreenWidth)) / 2.0;
            playerView.frame = CGRectMake(0, y, kScreenWidth, kScreenHeight);
        }
            break;
        case NETSAudienceIMPkEnd:
        {
            playerView.frame = CGRectMake(0, 0, kScreenWidth * 2, kScreenHeight * 2);
            playerView.centerX = kScreenWidth;
            playerView.centerY = self.contentView.centerY;
        }
            break;
        case NETSAudienceConnectStart:
        {
            CGFloat scale = 1280 / 720.0;
            playerView.frame = CGRectMake(0, top, kScreenWidth , kScreenWidth * scale);
        }
            break;
            
        default:
        {
            playerView.frame = [self _fillPlayerRect];
        }
            break;
    }
//    YXAlogInfo(@"观众端播放器状态, status: %ld", (long)status);
}

/// 直播间关闭
- (void)didLiveRoomClosed {
    [self _showLiveRoomClosedView];
    [self shutdownPlayer];
}


-(void)joinchannelFailed:(NSInteger)errorCode {
    [self shutdownPlayer];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self changeToAudience];
    });
}

#pragma mark - private mehod

- (void)_showLiveRoomClosedView
{
    ntes_main_async_safe(^{
        // TODO: NETSAudienceRoomLiveClosed
//        self.roomStatus = self.mask.roomStatus = NETSAudienceRoomLiveClosed;
        [self.liveClosedMask installWithAvatar:self.roomModel.anchor.avatar nickname:self.roomModel.anchor.userUuid];
        [self.mask addSubview:self.liveClosedMask];
    });
    
    //清除顶层视图的subview
    UIView *topmostView = [TopmostView viewForApplicationWindow];
    for (UIView *subview in topmostView.subviews) {
        [subview removeFromSuperview];
    }
    topmostView.userInteractionEnabled = NO;
    printf("_showLiveRoomClosedView --- _showLiveRoomClosedView --- _showLiveRoomClosedView");
}
//
- (void)_showLiveRoomErrorView {
    ntes_main_async_safe(^{
        // TODO: NETSAudienceRoomLiveError
//        self.roomStatus = self.mask.roomStatus = NETSAudienceRoomLiveError;
        [self.mask addSubview:self.networkFailureView];
    });
    printf("_showLiveRoomErrorView --- _showLiveRoomErrorView --- _showLiveRoomErrorView");
}

/// 获取直播间详情
- (void)_obtainChatroomInfo:(NELiveDetail *)roomModel isNeedEnterChatRoom:(BOOL)isNeed
{
    [[NELiveKit shared] fetchLiveInfoWithLiveRecordId:roomModel.live.liveRecordId callback:^(NSInteger code, NSString * _Nullable msg, NELiveDetail * _Nullable info) {
        if (!info) {
            if ([self.reachability isReachable]) {
                [self.mask closeChatRoom];
            }
        } else {
            self.roomInfo = info;
            [[NELiveKit shared] joinLiveWithLive:info callback:^(NSInteger code2, NSString * _Nullable msg2, NSString * _Nullable rtmpPullUrl) {
                if (code2 == 0) {
                    ntes_main_async_safe(^{
                        self.mask.info = info;
                        CGFloat y = 0;
                        if ((info.live.live == NELiveStatusPking || info.live.live == NELiveStatusPunishing)) {
                            CGFloat top = 64 + (kIsFullScreen ? 44 : 20);
                            y = top - (kScreenHeight - (640 / 720.0 * kScreenWidth)) / 2.0;
                        }
                        [self _layoutPlayerWithY:y];
                        [self _playWithUrl:rtmpPullUrl];
                        self.rtmpPullUrl = rtmpPullUrl;
                    });
                } else {
                    [self _alertToExitRoomWithError:nil];
                }
            }];
        }
    }];
}

/// 布局播放器
- (void)_layoutPlayerWithY:(CGFloat)y
{
    [self.contentView addSubview:self.player.view];
    [self.contentView sendSubviewToBack:self.player.view];
    
    self.player.view.top = y;
    
    if (y == 0) {
        self.player.view.frame = [self _fillPlayerRect];
    }
}

/// 缩放后播放器尺寸大小
- (CGRect)_fillPlayerRect {
    CGFloat nor = 1280 / 720.0;
    CGFloat cur = kScreenHeight / kScreenWidth * 1.0;
    if (nor == cur) {
        return self.bounds;
    }
    CGFloat xOffset = (kScreenHeight / nor - kScreenWidth) * 0.5;
    return CGRectMake(-xOffset, 0, kScreenHeight / nor, kScreenHeight);
}

/// 播放指定url源
- (void)_playWithUrl:(NSString *)urlStr
{
    NSURL *url = [NSURL URLWithString:urlStr];
    [self.player setPlayUrl:url];
    [self.player prepareToPlay];
}


/// 直播间关闭弹窗
- (void)_alertToExitRoomWithError:(nullable NSError *)error
{
    ntes_main_async_safe(^{
        BOOL accountErr = NO;
        if ([error.domain isEqualToString:@"NIMLocalErrorDomain"] && error.code == 13) {
            accountErr = YES;
        }
        NSString *title = accountErr ? @"您的账号已登出" : @"直播间已关闭";
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:NSLocalizedString(@"点击确定关闭该直播间", nil) preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *confirm = [UIAlertAction actionWithTitle:NSLocalizedString(@"确定", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            if (accountErr) {
                [[NENavigator shared].navigationController popToRootViewControllerAnimated:YES];
            } else {
                [[NENavigator shared].navigationController popViewControllerAnimated:YES];
            }
        }];
        [alert addAction:confirm];
        [[NENavigator shared].navigationController presentViewController:alert animated:YES completion:nil];
    });
}

/// 向左轻扫显示蒙层
- (void)_swipeShowMask:(UISwipeGestureRecognizer *)gesture
{
    NSLog(@"向左轻扫显示蒙层");
    if (self.mask.left > kScreenWidth/2.0) {
        [UIView animateWithDuration:0.3 animations:^{
            self.mask.left = 0;
        }];
    }
}
- (void)_swipeDismissMask:(UISwipeGestureRecognizer *)gesture
{
    if (_roomStatus == NETSAudienceRoomLiveClosed || _roomStatus == NETSAudienceRoomLiveError) {
        NSLog(@"页面故障,阻止向右轻扫隐藏蒙层");
        return;
    }
    NSLog(@"向右轻扫隐藏蒙层");
    if (self.mask.left < kScreenWidth/2.0) {
        [UIView animateWithDuration:0.3 animations:^{
            self.mask.left = kScreenWidth;
        }];
    }
}

- (void)dealloc {
//    NSlog(@"dealloc NETSAudienceChatRoomCell: %p", self);
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self shutdownPlayer];
    NSLog(@"Cell release");
}

#pragma mark - lazy load

- (NETSAudienceMask *)mask
{
    if (!_mask) {
        _mask = [[NETSAudienceMask alloc] initWithFrame:[UIScreen mainScreen].bounds];
        _mask.delegate = self;
    }
    return _mask;
}

- (NETSPullStreamErrorView *)networkFailureView
{
    if (!_networkFailureView) {
        _networkFailureView = [[NETSPullStreamErrorView alloc]init];
        [_networkFailureView installWithAvatar:self.roomModel.anchor.avatar nickname:self.roomModel.anchor.userUuid];
        _networkFailureView.delegate = self;
        _networkFailureView.userInteractionEnabled = YES;
    }
    return _networkFailureView;
}

- (NETSLiveEndView *)liveClosedMask
{
    if (!_liveClosedMask) {
        _liveClosedMask = [[NETSLiveEndView alloc] init];
    }
    return _liveClosedMask;
}

- (NELivePlayerController *)player
{
    if (!_player) {
        _player = [[NELivePlayerController alloc] init];
        [_player setBufferStrategy:NELPTopSpeed];
        [_player setScalingMode:NELPMovieScalingModeNone];
        [_player setShouldAutoplay:YES];
        [_player setHardwareDecoder:YES];
        [_player setPauseInBackground:NO];
        [_player setPlaybackTimeout:(3 * 1000)];
        
        NELPRetryConfig *retryConfig = [[NELPRetryConfig alloc] init];
        retryConfig.count = 1;
        [_player setRetryConfig:retryConfig];
    }
    return _player;
}

@end
