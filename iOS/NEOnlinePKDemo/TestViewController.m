//
//  TestViewController.m
//  NEOnlinePKDemo
//
//  Created by Ginger on 2022/3/10.
//

#import "TestViewController.h"

@interface TestViewController () <NELiveListener>
@property (weak, nonatomic) IBOutlet UIView *preview;
@property (nonatomic, strong) UIView *preview1;

@end

@implementation TestViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.preview1.frame = CGRectMake(0, 0, self.preview.frame.size.width, self.preview.frame.size.height);
    [self.preview addSubview:self.preview1];
    
    [NELiveKit shared].listener = self;
}

- (IBAction)login:(id)sender {
    [[NELiveKit shared] loginWithAccount:@"jinjie" token:@"jinjie" callback:^(NSInteger code, NSString * _Nullable msg) {
        dispatch_async(dispatch_get_main_queue(),^{
            if (code == 0) {
                [NETSToast showToast:@"登录成功"];
            } else {
                [NETSToast showToast:[NSString stringWithFormat:@"登录失败 %zd %@", code, msg]];
            }
        });
    }];
}

- (IBAction)startLive:(id)sender {
    
    [[NELiveKit shared].liveMediaController startPreviewWithCanvas:self.preview1];
    
    [[NELiveKit shared] startLiveWithLiveTopic:@"直播主题" liveType:NEliveRoomTypePkLive nickName:@"测试主播" cover:@"https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Flivecover%2Fpexels-ichad-windhiagiri-3981477.jpg" callback:^(NSInteger code, NSString * _Nullable msg, NELiveDetail * _Nullable detail) {
        dispatch_async(dispatch_get_main_queue(),^{
            if (code == 0) {
                [NETSToast showToast:@"开播成功"];
            } else {
                [NETSToast showToast:[NSString stringWithFormat:@"开播失败 %zd %@", code, msg]];
            }
        });

    }];
}

- (IBAction)stopLive:(id)sender {
    [[NELiveKit shared].liveMediaController stopPreview];
    [[NELiveKit shared] stopLiveWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        dispatch_async(dispatch_get_main_queue(),^{
            if (code == 0) {
                [NETSToast showToast:@"关播成功"];
            } else {
                [NETSToast showToast:[NSString stringWithFormat:@"关播失败 %zd %@", code, msg]];
            }
        });
    }];
}

- (IBAction)pkInvite:(id)sender {
    NEPKRule *rule = [[NEPKRule alloc] init];
    rule.dogfallTime = 10;
    rule.pkGameTime = 10;
    rule.rewardsPunishmentsTime = 10;
    [[NELiveKit shared] invitePKWithTargetAccountId:@"202203111507250600010000" rule:rule callback:^(NSInteger code, NSString * _Nullable msg) {
        if (code == 0) {
            [NETSToast showToast:@"发起PK成功"];
        } else {
            [NETSToast showToast:[NSString stringWithFormat:@"发起PK失败 %zd %@", code, msg]];
        }
    }];
}

- (IBAction)cancelInvite:(id)sender {
    [[NELiveKit shared] cancelPKInviteWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        if (code == 0) {
            [NETSToast showToast:@"取消PK成功"];
        } else {
            [NETSToast showToast:[NSString stringWithFormat:@"取消PK失败 %zd %@", code, msg]];
        }
    }];
}
- (IBAction)acceptInvite:(id)sender {
    [[NELiveKit shared] acceptPKWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        if (code == 0) {
            [NETSToast showToast:@"接受PK成功"];
        } else {
            [NETSToast showToast:[NSString stringWithFormat:@"接受PK失败 %zd %@", code, msg]];
        }
    }];
}
- (IBAction)rejectInvite:(id)sender {
    [[NELiveKit shared] rejectPKWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        if (code == 0) {
            [NETSToast showToast:@"拒绝PK成功"];
        } else {
            [NETSToast showToast:[NSString stringWithFormat:@"拒绝PK失败 %zd %@", code, msg]];
        }
    }];
}
- (IBAction)stopPK:(id)sender {
    [[NELiveKit shared] stopPKWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        if (code == 0) {
            [NETSToast showToast:@"结束PK成功"];
        } else {
            [NETSToast showToast:[NSString stringWithFormat:@"结束PK失败 %zd %@", code, msg]];
        }
    }];
}
- (IBAction)gift:(id)sender {
    [[NELiveKit shared] rewardWithGiftId:2 callback:^(NSInteger code, NSString * _Nullable msg) {
            
    }];
}

- (UIView *)preview1 {
    if (!_preview1) {
        _preview1 = [[UIView alloc] init];
    }
    return _preview1;
}

- (void)onPKCanceledWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKInvitCanceled %@", actionAnchor.userUuid]];
}

- (void)onPKInvitedWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKInvitReceived %@", actionAnchor.userUuid]];
}

- (void)onPKRejectedWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKInvitRejected %@", actionAnchor.userUuid]];
}

- (void)onPKAcceptedWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKInvitAccepted %@", actionAnchor.userUuid]];
}

- (void)onPKTimeoutWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKInvitTimeout %@", actionAnchor.userUuid]];
}
- (void)onMessagesReceivedWithMessages:(NSArray<NERoomMessage *> *)messages {
    [NETSToast showToast:[NSString stringWithFormat:@"onReceiveMessages %@", messages.firstObject.text]];
}

- (void)onPKStartWithPkStartTime:(NSInteger)pkStartTime pkCountDown:(NSInteger)pkCountDown inviter:(NELivePKAnchor *)inviter invitee:(NELivePKAnchor *)invitee {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKStart pkStartTime:%zd pkCountDown:%zd inviter:%@ invitee:%@", pkStartTime, pkCountDown, inviter.userUuid, invitee.userUuid]];
}

- (void)onPKPunishingStartWithPkPenaltyCountDown:(NSInteger)pkPenaltyCountDown inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKPunishingStart pkPenaltyCountDown:%zd inviterRewards:%zd", pkPenaltyCountDown, inviterRewards]];
}

- (void)onRewardReceivedWithRewarderAccountId:(NSString *)rewarderAccountId rewarderUserName:(NSString *)rewarderUserName giftId:(NSInteger)giftId anchorReward:(NELiveAnchorReward *)anchorReward otherAnchorReward:(NELiveAnchorReward *)otherAnchorReward {
    [NETSToast showToast:[NSString stringWithFormat:@"onRewardReceive rewarderAccountId:%@ rewarderNickname:%@ giftId:%zd anchorReward:%zd otherAnchorReward:%zd", rewarderAccountId, rewarderUserName, giftId, anchorReward.rewardTotal, otherAnchorReward.rewardTotal]];
}

- (void)onPKEndWithReason:(NSInteger)reason pkEndTime:(NSInteger)pkEndTime inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards countDownEnd:(BOOL)countDownEnd {
    [NETSToast showToast:[NSString stringWithFormat:@"onPKEnd reason:%zd pkEndTime:%zd inviterRewards:%zd inviteeRewards:%zd countDownEnd:%d", reason, pkEndTime, inviterRewards, inviteeRewards, countDownEnd]];
}

@end
