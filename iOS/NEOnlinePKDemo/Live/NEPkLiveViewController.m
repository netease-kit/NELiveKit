//
//  NEPkLiveViewController.m
//  NEOnlinePKDemo
//
//  Created by Ginger on 2022/3/1.
//

#import "NEPkLiveViewController.h"
#import "NETSWarnToast.h"
#import "NETSAnchorBottomPanel.h"
#import "NETSAnchorCoverSetting.h"
#import "UIImage+NTES.h"
#import "NETSInputToolBar.h"
#import "NETSAudienceNum.h"
#import "NETSAnchorTopInfoView.h"
#import "NTESKeyboardToolbarView.h"
#import "NETSLiveChatView.h"
#import "NETSMoreSettingActionSheet.h"
#import "NETSChoosePKSheet.h"
#import <ReactiveObjC/ReactiveObjC.h>
#import "NETSInvitingBar.h"
#import "TopmostView.h"
#import "NETSPkStatusBar.h"
#import "NETSInviteeInfoView.h"
#import <AVFoundation/AVFoundation.h>
#import "NETSBeautySettingActionSheet.h"
#import "NETSFilterSettingActionSheet.h"
#import "NETSFUManger.h"
#import "NETSMoreSettingModel.h"
#import "Reachability.h"

@interface NEPkLiveViewController () <NETSAnchorBottomPanelDelegate, NTESKeyboardToolbarDelegate, NETSInputToolBarDelegate, NETSMoreSettingActionSheetDelegate, NETSChoosePKSheetDelegate, NELiveListener, NETSInvitingBarDelegate>

/// 封面设置面板
@property (nonatomic, strong)   NETSAnchorCoverSetting  *settingPanel;
/// 底部面板
@property (nonatomic, strong)   NETSAnchorBottomPanel   *bottomPanel;
/// 试用提示
@property (nonatomic, strong)   NETSWarnToast           *warnToast;
/// 返回按钮
@property (nonatomic, strong)   UIButton                *backBtn;
/// 切换摄像头按钮
@property (nonatomic, strong)   UIButton                *switchCameraBtn;
/// 邀请别人PK按钮
@property (nonatomic, strong)   UIButton                *pkBtn;
/// 直播中 底部工具条
@property (nonatomic, strong)   NETSInputToolBar        *livingInputTool;
/// 直播中 观众数量视图
@property (nonatomic, strong)   NETSAudienceNum         *audienceInfo;
/// 主播信息视图
@property (nonatomic, strong) NETSAnchorTopInfoView     *anchorInfo;
/// 键盘工具条
@property (nonatomic, strong) NTESKeyboardToolbarView   *toolBar;
/// 聊天视图
@property (nonatomic,strong)   NETSLiveChatView  *chatView;

/// 绘制摄像头采集
@property (nonatomic, strong,readwrite)   UIView *localRender;
/// 远端视频面板
@property (nonatomic, strong,readwrite)   UIView *remoteRender;

//记录被邀请者的id
@property(nonatomic, strong) NSString *inviteeAccountId;

/// pk邀请状态条
@property (nonatomic, strong)   NETSInvitingBar  *pkInvitingBar;
/// 是否接受pk邀请对话框
@property (nonatomic, strong)   UIAlertController   *pkAlert;
/// pk状态条
@property(nonatomic, strong)    NETSPkStatusBar  *pkStatusBar;

/// pk胜利图标
@property (nonatomic, strong)   UIImageView     *pkSuccessIco;
/// pk失败图标
@property (nonatomic, strong)   UIImageView     *pkFailedIco;

/// 被邀请者信息视图
@property (nonatomic, strong)   NETSInviteeInfoView     *inviteeInfo;
//pk 静音按钮
@property(nonatomic, strong) UIButton *muteAudioBtn;

/// 直播过程中 更多设置 数据项
@property (nonatomic, strong)   NSArray <NETSMoreSettingModel *>    *moreSettings;

@property (nonatomic, assign) bool micEnabled;
@property (nonatomic, assign) bool cameraEnabled;

/// 网络监测类
@property(nonatomic, strong) Reachability               *reachability;

@end

@implementation NEPkLiveViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.view.backgroundColor = [UIColor blackColor];
    
    self.reachability = [Reachability reachabilityWithHostName:@"www.baidu.com"];
    [self.reachability startNotifier];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    // 监测网络
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reachabilityChanged:) name:kReachabilityChangedNotification object:nil];
    
    [self layoutPreview];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:YES];
    
    if (![NELiveKit shared].isLoggedin) {
        [[AuthorManager shareInstance] startEntranceWithCompletion:^(YXUserInfo * _Nullable userinfo, NSError * _Nullable error) {
            [[NELiveKit shared] loginWithAccount:userinfo.accountId token:userinfo.accessToken callback:^(NSInteger code, NSString * _Nullable msg) {
                dispatch_async(dispatch_get_main_queue(),^{
                    if (code == 0) {
                        [NETSToast showToast:@"登录成功"];
                        setAccessToken(userinfo.accessToken);
                    } else {
                        [NETSToast showToast:[NSString stringWithFormat:@"登录失败 %zd %@", code, msg]];
                    }
                });
            }];
        }];
    } else {
        [[NELiveKit shared].liveMediaController stopPreview];
        [[NELiveKit shared].liveMediaController startPreviewWithCanvas:self.localRender];
        [NELiveKit shared].listener = self;
    }
}

#pragma mark - Notification Method

- (void)reachabilityChanged:(NSNotification *)note {
    Reachability *currentReach = [note object];
    NSCParameterAssert([currentReach isKindOfClass:[Reachability class]]);
    NetworkStatus netStatus = [currentReach currentReachabilityStatus];
    switch (netStatus) {
        case NotReachable:{// 网络不可用
            ntes_main_async_safe(^{
                [NETSToast showToast:@"网络连接已断开"];
                if ([NELiveKit shared].isPKing) {
                } else {
                    [self didSelectCloseLive];
                }
            });
        }
            break;
        default:
            break;
    }
}

/// 单人预览布局
- (void)layoutPreview {
    [self.view addSubview:self.localRender];
    [self.view addSubview:self.remoteRender];
    self.remoteRender.hidden = true;
    [self.view addSubview:self.backBtn];
    [self.view addSubview:self.switchCameraBtn];
    [self.view addSubview:self.settingPanel];
    [self.view addSubview:self.bottomPanel];
    [self.view addSubview:self.warnToast];

    self.localRender.frame = self.view.frame;
    self.backBtn.frame = CGRectMake(20, (kIsFullScreen ? 44 : 20) + 8, 24, 24);
    self.switchCameraBtn.frame = CGRectMake(kScreenWidth - 20 - 24, (kIsFullScreen ? 44 : 20) + 8, 24, 24);
    self.settingPanel.frame = CGRectMake(20, (kIsFullScreen ? 88 : 64) + 20, kScreenWidth - 40, 88);
    self.bottomPanel.frame = CGRectMake(0, kScreenHeight - 128 - (kIsFullScreen ? 54 : 20), kScreenWidth, 128);
    self.warnToast.frame = CGRectMake(20, self.bottomPanel.top - 20 - 60, kScreenWidth - 40, 60);
}

/// 单人直播布局
- (void)layoutSingleLive {
   
    [self.backBtn removeFromSuperview];
    [self.switchCameraBtn removeFromSuperview];
    [self.settingPanel removeFromSuperview];
    [self.bottomPanel removeFromSuperview];
    [self.warnToast removeFromSuperview];

    [self.view addSubview:self.anchorInfo];
    [self.view addSubview:self.audienceInfo];
    [self.view addSubview:self.chatView];
    [self.view addSubview:self.livingInputTool];
    [self.view addSubview:self.toolBar];
    [self.view addSubview:self.pkBtn];
    
    self.anchorInfo.frame = CGRectMake(8, (kIsFullScreen ? 44 : 20) + 4, 124, 36);
    self.audienceInfo.frame = CGRectMake(kScreenWidth - 8 - 195, self.anchorInfo.top + (36 - 28) / 2.0, 195, 28);
    CGFloat chatViewHeight = [self chatViewHeight];
    self.chatView.frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - chatViewHeight, kScreenWidth - 16 - 60 - 20, chatViewHeight);
    self.livingInputTool.frame = CGRectMake(0, kScreenHeight - (kIsFullScreen ? 34 : 0) - 14 - 36, kScreenWidth, 36);
    
    self.localRender.frame = self.view.frame;
    self.remoteRender.hidden = true;
    [self.pkStatusBar removeFromSuperview];
    [self.pkSuccessIco removeFromSuperview];
    [self.pkFailedIco removeFromSuperview];
    [self.inviteeInfo removeFromSuperview];
    [self.muteAudioBtn removeFromSuperview];
    self.muteAudioBtn = nil;

    [self.pkBtn setImage:[UIImage imageNamed:@"pk_ico"] forState:UIControlStateNormal];
    
    [self.audienceInfo reloadWithDatas:[NELiveKit shared].members];
}

- (void)layoutPkLiveWithInviter:(NELivePKAnchor *)inviter invitee:(NELivePKAnchor *)invitee {
    [self.backBtn removeFromSuperview];
    [self.switchCameraBtn removeFromSuperview];
    [self.settingPanel removeFromSuperview];
    [self.bottomPanel removeFromSuperview];

    [self.view addSubview:self.anchorInfo];
    [self.view addSubview:self.audienceInfo];
    [self.view addSubview:self.chatView];
    [self.view addSubview:self.livingInputTool];
    [self.view addSubview:self.toolBar];
    
    //开始PK布局 SE以及部分机型采用重新设置view来处理 页面布局错误问题
    [self pkResetLiveMediaController];
    CGFloat anchorInfoBottom = (kIsFullScreen ? 44 : 20) + 36 + 4;
    self.localRender.frame = CGRectMake(0, anchorInfoBottom + 24, kScreenWidth / 2.0, kScreenWidth / 2.0 * 1280 / 720.0);
    self.remoteRender.frame = CGRectMake(self.localRender.right, self.localRender.top, self.localRender.width, self.localRender.height);
    self.remoteRender.hidden = false;
    
    self.anchorInfo.frame = CGRectMake(8, (kIsFullScreen ? 44 : 20) + 4, 124, 36);
    self.audienceInfo.frame = CGRectMake(kScreenWidth - 8 - 195, self.anchorInfo.top + (36 - 28) / 2.0, 195, 28);
    CGFloat chatViewHeight = [self chatViewHeight];
    self.chatView.frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - chatViewHeight, kScreenWidth - 16 - 60 - 20, chatViewHeight);
    self.livingInputTool.frame = CGRectMake(0, kScreenHeight - (kIsFullScreen ? 34 : 0) - 14 - 36, kScreenWidth, 36);
    [self.pkSuccessIco removeFromSuperview];
    [self.pkFailedIco removeFromSuperview];
    
    [self.view addSubview:self.pkStatusBar];
    [self.view addSubview:self.muteAudioBtn];
    [self.view addSubview:self.inviteeInfo];
    
    self.muteAudioBtn.frame = CGRectMake(kScreenWidth - 35, self.localRender.bottom -35, 25, 25);
    self.pkStatusBar.frame = CGRectMake(0, self.localRender.bottom, kScreenWidth, 58);
    self.inviteeInfo.frame = CGRectMake(self.remoteRender.right - 8 - 82, self.remoteRender.top + 8, 82, 24);
    //update 对面主播显示信息
    BOOL isInviter = [inviter.userUuid isEqualToString:[NELiveKit shared].userUuid];
    if (isInviter) {
        //当前主播为邀请者
        [self.inviteeInfo reloadAvatar:invitee.avatar nickname:invitee.userUuid];
    }else{
        [self.inviteeInfo reloadAvatar:inviter.avatar nickname:inviter.userUuid];
    }
    
    [self.pkStatusBar refreshWithLeftRewardCoins:0 leftRewardAvatars:@[] rightRewardCoins:0 rightRewardAvatars:@[]];
    
    [self.pkBtn setImage:[UIImage imageNamed:@"end_pk_ico"] forState:UIControlStateNormal];
    [self.audienceInfo reloadWithDatas:[NELiveKit shared].members];
}

- (void)pkResetLiveMediaController{
    [[NELiveKit shared].liveMediaController resetPreviewWithCanvas:self.localRender];
}
- (CGFloat)chatViewHeight {
    if (kScreenHeight <= 568) {
        return 100;
    } else if (kScreenHeight <= 736) {
        return 130;
    }
    return 204;
}

#pragma mark - NETSAnchorBottomPanelDelegate 底部操作面板代理
- (void)clickBeautyBtn {
    [NETSBeautySettingActionSheet showWithMask:NO];
}

- (void)clickFilterBtn {
    [NETSFilterSettingActionSheet showWithMask:NO];
}

- (void)clickStartLiveBtn {
    self.cameraEnabled = false;
    self.micEnabled = false;
    [self _authMic];
    [self _authCamera];
}

- (void)tryToStartLive {
    if (self.cameraEnabled && self.micEnabled) {
        [NETSToast showLoading];
        NSString *topic = [self.settingPanel getTopic];
        NSString *cover = [self.settingPanel getCover];
        [[NELiveKit shared] startLiveWithLiveTopic:topic liveType:NEliveRoomTypePkLive nickName:@"测试主播" cover:cover callback:^(NSInteger code, NSString * _Nullable msg, NELiveDetail * _Nullable detail) {
            dispatch_async(dispatch_get_main_queue(),^{
                [NETSToast hideLoading];
                if (code == 0) {
                    [NETSToast showToast:@"开播成功"];
                    [self layoutSingleLive];
                    [self.anchorInfo installWithAvatar:detail.anchor.avatar nickname:detail.anchor.userUuid wealth:0];
                    [self.audienceInfo reloadWithDatas:[NELiveKit shared].members];
                } else {
                    [NETSToast showToast:[NSString stringWithFormat:@"开播失败 %zd %@", code, msg]];
                }
            });
        }];
    }
}

- (void)startPkAction:(UIButton *)sender {
    NELivePKStatus pkState = NELiveKit.shared.pkStatus;
    if (pkState == NELivePKStatusIdle) {
        if ([self.pkInvitingBar superview]) {
            [NETSToast showToast:NSLocalizedString(@"您已经再邀请中,不可再邀请", nil)];
            return;
        }
        NSLog(@"打开pk列表面板,开始pk");
        [NETSChoosePKSheet showWithTarget:self];
    } else if (pkState == NELivePKStatusPking || pkState == NELivePKStatusPunishing) {
        NSLog(@"点击结束pk");
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"结束PK", nil) message:NSLocalizedString(@"PK尚未结束,强制结束会返回普通直播模式", nil) preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            NSLog(@"取消强制结束pk");
        }];
        UIAlertAction *confirm = [UIAlertAction actionWithTitle:NSLocalizedString(@"立即结束", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [[NELiveKit shared] stopPKWithCallback:^(NSInteger code, NSString * _Nullable msg) {
                if (code == 0) {
                    ntes_main_async_safe(^{
                        [self layoutSingleLive];
                    });
                }
            }];
        }];
        [alert addAction:cancel];
        [alert addAction:confirm];
        [self presenAlert:alert];
    } else if (pkState == NELiveStatusInviting) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if ([self.pkInvitingBar superview]) {
                [NETSToast showToast:NSLocalizedString(@"您已经再邀请中,不可再邀请", nil)];
            }else {
                [NETSChoosePKSheet showWithTarget:self];
            }
        });
    }
}

#pragma mark - NETSChoosePKSheetDelegate 选择主播PK代理
- (void)choosePkOnSheet:(NETSChoosePKSheet *)sheet withRoom:(NELiveDetail *)room {
    [sheet dismiss];
    self.inviteeAccountId = room.anchor.userUuid;
    NSString *msg = [NSString stringWithFormat:NSLocalizedString(@"确定邀请\"%@\"进行PK?", nil), room.anchor.userUuid];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"邀请PK", nil) message:msg preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
//        YXAlogInfo(@"邀请者取消pk邀请...");
    }];

    UIAlertAction *confirm = [UIAlertAction actionWithTitle:NSLocalizedString(@"确定", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [NETSToast showLoading];
        
        NEPKRule *rule = [[NEPKRule alloc] init];
        [[NELiveKit shared] invitePKWithTargetAccountId:room.anchor.userUuid rule:rule callback:^(NSInteger code, NSString * _Nullable msg) {
            ntes_main_async_safe(^{
                [NETSToast hideLoading];
                if (code != 0) {
                    [NETSToast showToast:[NSString stringWithFormat:@"邀请PK失败 code:%zd msg:%@", code, msg]];
                } else {
                    NSString *title = [NSString stringWithFormat:NSLocalizedString(@"邀请\"%@\"PK连线中...", nil), room.anchor.userUuid];
                    self.pkInvitingBar = [NETSInvitingBar showInvitingWithTarget:self title:title];
                }
            });
        }];
    }];
    [alert addAction:cancel];
    [alert addAction:confirm];
    [self presenAlert:alert];
}

- (void)presenAlert:(UIAlertController *)alert {
    // 消除顶层视图
    UIView *topmostView = [TopmostView viewForApplicationWindow];
    for (UIView *subview in topmostView.subviews) {
        [subview removeFromSuperview];
    }
    topmostView.userInteractionEnabled = NO;
    
    // 弹出alert
    if (self.pkAlert) {
        [self.pkAlert dismissViewControllerAnimated:NO completion:nil];
        self.pkAlert = nil;
    }
    [[NENavigator shared].navigationController presentViewController:alert animated:YES completion:nil];
    self.pkAlert = alert;
}

#pragma mark - NTESKeyboardToolbarDelegate 键盘顶部工具条代理

- (void)didToolBarSendText:(NSString *)text {
    if (isEmptyString(text)) {
        [NETSToast showToast:NSLocalizedString(@"所发消息为空", nil)];

        return;
    }
    [self.livingInputTool resignFirstResponder];
    [[NELiveKit shared] sendTextMessageWithMessage:text callback:^(NSInteger code, NSString * _Nullable msg) {
        if (code != 0) {
            [NETSToast showToast:[NSString stringWithFormat:@"发送消息失败 code:%zd msg:%@", code, msg]];
        } else {
            NETSMessageModel *model = [[NETSMessageModel alloc] init];
            model.type = NETSMessageNormal;
            model.sender = [NELiveKit shared].userUuid;
            model.text = text;
            model.isAnchor = true;
            [_chatView addMessages:@[model]];
        }
    }];
}

#pragma mark - NETSInputToolBarDelegate 底部工具条代理事件
- (void)clickInputToolBarAction:(NETSInputToolBarAction)action {
    switch (action) {
        case NETSInputToolBarInput: {
            [self.toolBar becomeFirstResponse];
        }
            break;
        case NETSInputToolBarBeauty: {
            [NETSBeautySettingActionSheet show];
        }
            break;
        case NETSInputToolBarConnectRequest: {//主播连麦管理
//            [self connectMicManagerClick];
//            NETSRequestManageMainController *statusVc = [[NETSRequestManageMainController alloc] initWithRoomId:self.createRoomModel.live.roomId];
//            NTESActionSheetNavigationController *nav = [[NTESActionSheetNavigationController alloc] initWithRootViewController:statusVc];
//            nav.dismissOnTouchOutside = YES;
//            [[NENavigator shared].navigationController presentViewController:nav animated:YES completion:nil];
        }
            break;
        case NETSInputToolBarMusic: {
//            [NETSAudioMixingActionSheet show];
        }
            break;
        case NETSInputToolBarMore: {
            [NETSMoreSettingActionSheet showWithTarget:self items:self.moreSettings];
          
        }
            break;
            
        default:
            break;
    }
}

/// 点击屏幕收起键盘
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    [self.toolBar resignFirstResponder];
    [self.settingPanel resignFirstResponder];
    [self.view endEditing:true];
//    [self.bottomBar resignFirstResponder];
}

#pragma mark - NETSMoreSettingActionSheetDelegate 点击更多设置代理
/// 开启/关闭 摄像头
- (void)didSelectCameraEnable:(BOOL)enable {
    if (!enable) {
        [[NELiveKit shared].liveMediaController muteLiveVideo];
        NSArray *subViews = [NSArray arrayWithArray:self.localRender.subviews];
        for (UIView *view in subViews) {
            if ([view isKindOfClass:NSClassFromString(@"NMCMTLVideoView")]) {
                [view removeFromSuperview];
            }
        }
    } else {
        [[NELiveKit shared].liveMediaController unmuteLiveVideo];
        [[NELiveKit shared].liveMediaController startPreviewWithCanvas:self.localRender];
        for (UIView *view in self.localRender.subviews) {
            if ([view isKindOfClass:NSClassFromString(@"NMCMTLVideoView")]) {
                view.frame = CGRectMake(0, 0, self.localRender.width, self.localRender.height);
            }
        }
    }
}

/// 关闭直播间
- (void)didSelectCloseLive {
//    [self closeLiveRoom];
    
    [[NELiveKit shared] stopLiveWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (code != 0) {
                [NETSToast showToast:[NSString stringWithFormat:@"关闭直播 code:%zd msg:%@", code, msg]];
            }
            if ([self.presentedViewController isKindOfClass:[UIAlertController class]]) {//防止在销毁房间的时候有其他主播邀请pk
                [self.navigationController dismissViewControllerAnimated:YES completion:^{
                    [self.navigationController popViewControllerAnimated:YES];
                }];
            } else {
                [self.navigationController popViewControllerAnimated:YES];
            }
        });
    }];
}

#pragma mark - Views
- (NETSWarnToast *)warnToast {
    if (!_warnToast) {
        _warnToast = [[NETSWarnToast alloc] init];
        _warnToast.toast = NSLocalizedString(@"本应用为测试产品、请勿商用。单次直播最长10分钟，每个频道最多10人", nil);
    }
    return _warnToast;
}

- (NETSAnchorBottomPanel *)bottomPanel {
    if (!_bottomPanel) {
        _bottomPanel = [[NETSAnchorBottomPanel alloc] init];
        _bottomPanel.delegate = self;
    }
    return _bottomPanel;
}

- (NETSAnchorCoverSetting *)settingPanel {
    if (!_settingPanel) {
        _settingPanel = [[NETSAnchorCoverSetting alloc] init];
    }
    return _settingPanel;
}

- (UIButton *)backBtn {
    if (!_backBtn) {
        _backBtn = [[UIButton alloc] init];
        [_backBtn setImage:[UIImage imageNamed:@"back_ico"] forState:UIControlStateNormal];
        [_backBtn addTarget:self action:@selector(clickAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _backBtn;
}

- (UIButton *)switchCameraBtn {
    if (!_switchCameraBtn) {
        _switchCameraBtn = [[UIButton alloc] init];
        UIImage *img = [[UIImage imageNamed:@"switch_camera_ico"] ne_imageWithTintColor:[UIColor whiteColor]];
        [_switchCameraBtn setImage:img forState:UIControlStateNormal];
        [_switchCameraBtn addTarget:self action:@selector(clickAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _switchCameraBtn;
}

- (void)clickAction:(UIButton *)sender {
    if (sender == self.backBtn) {
        [[NELiveKit shared].liveMediaController stopPreview];
        [self.navigationController popViewControllerAnimated:YES];
    } else if (sender == self.switchCameraBtn) {
        [[NELiveKit shared].liveMediaController switchCamera];
    }
}

- (UIButton *)pkBtn {
    if (!_pkBtn) {
        _pkBtn = [[UIButton alloc] initWithFrame:CGRectMake(kScreenWidth - 60 - 8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - 60, 60, 60)];
        [_pkBtn setImage:[UIImage imageNamed:@"pk_ico"] forState:UIControlStateNormal];
        [_pkBtn addTarget:self action:@selector(startPkAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _pkBtn;
}

- (NTESKeyboardToolbarView *)toolBar {
    if (!_toolBar) {
        _toolBar = [[NTESKeyboardToolbarView alloc] initWithFrame:CGRectMake(0, kScreenHeight, kScreenWidth, 50)];
        _toolBar.backgroundColor = UIColor.whiteColor;
        _toolBar.cusDelegate = self;
    }
    return _toolBar;
}

- (NETSLiveChatView *)chatView {
    if (!_chatView) {
        CGRect frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - 204, kScreenWidth - 16 - 60 - 20, 204);
        _chatView = [[NETSLiveChatView alloc] initWithFrame:frame];
    }
    return _chatView;
}

- (NETSInputToolBar *)livingInputTool {
    if (!_livingInputTool) {
        _livingInputTool = [[NETSInputToolBar alloc] init];
        _livingInputTool.delegate = self;
        _livingInputTool.textField.inputAccessoryView = [[UIView alloc] init];
    }
    return _livingInputTool;
}

- (NETSAnchorTopInfoView *)anchorInfo {
    if (!_anchorInfo) {
        _anchorInfo = [[NETSAnchorTopInfoView alloc] init];
    }
    return _anchorInfo;
}

- (NETSAudienceNum *)audienceInfo {
    if (!_audienceInfo) {
        _audienceInfo = [[NETSAudienceNum alloc] initWithFrame:CGRectZero];
    }
    return _audienceInfo;
}

- (NETSPkStatusBar *)pkStatusBar {
    if (!_pkStatusBar) {
        _pkStatusBar = [[NETSPkStatusBar alloc] init];
    }
    return _pkStatusBar;
}

- (UIImageView *)pkSuccessIco {
    if (!_pkSuccessIco) {
        _pkSuccessIco = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 100, 100)];
    }
    return _pkSuccessIco;
}

- (UIImageView *)pkFailedIco {
    if (!_pkFailedIco) {
        _pkFailedIco = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 100, 100)];
    }
    return _pkFailedIco;
}

- (NETSInviteeInfoView *)inviteeInfo {
    if (!_inviteeInfo) {
        _inviteeInfo = [[NETSInviteeInfoView alloc] init];
    }
    return _inviteeInfo;
}

-(UIButton *)muteAudioBtn {
    if (!_muteAudioBtn) {
        _muteAudioBtn = [[UIButton alloc]init];
        [_muteAudioBtn addTarget:self action:@selector(muteAudioBtnClick:) forControlEvents:UIControlEventTouchUpInside];
        [_muteAudioBtn setImage:[UIImage imageNamed:@"blockAnchorVoice_normal"] forState:UIControlStateNormal];
        [_muteAudioBtn setImage:[UIImage imageNamed:@"blockAnchorVoice_normal"] forState:UIControlStateHighlighted];
        [_muteAudioBtn setImage:[UIImage imageNamed:@"blockAnchorVoice_blocked"] forState:UIControlStateSelected];
    }
    return _muteAudioBtn;
}

- (void)muteAudioBtnClick:(UIButton *)sender {
    sender.selected = !sender.selected;
    if (sender.selected) {
        [[NELiveKit shared].liveMediaController mutePKAudioWithCallback:^(NSInteger code, NSString * _Nullable msg) {
            ntes_main_async_safe(^{
                if (code != 0) {
                    [NETSToast showToast:[NSString stringWithFormat:@"静音失败 code:%zd msg:%@", code, msg]];
                }
            });
        }];
    } else {
        [[NELiveKit shared].liveMediaController unmutePKAudioWithCallback:^(NSInteger code, NSString * _Nullable msg) {
            ntes_main_async_safe(^{
                if (code != 0) {
                    [NETSToast showToast:[NSString stringWithFormat:@"取消静音失败 code:%zd msg:%@", code, msg]];
                }
            });
        }];
    }
}

- (UIView *)localRender
{
    if (!_localRender) {
        _localRender = [[UIView alloc] init];
    }
    return _localRender;
}

- (UIView *)remoteRender
{
    if (!_remoteRender) {
        _remoteRender = [[UIView alloc] initWithFrame:self.view.frame];
        NERoomVideoCanvas *canvas = [[NERoomVideoCanvas alloc] init];
        canvas.container = _remoteRender;
        canvas.renderMode = NERoomVideoRenderScaleModeCropFill;
        [[NELiveKit shared].liveMediaController setupRemoteViewWithCanvas:canvas];
    }
    return _remoteRender;
}

- (NSArray<NETSMoreSettingModel *> *)moreSettings {
    if (!_moreSettings) {
        NETSMoreSettingStatusModel *camera = [[NETSMoreSettingStatusModel alloc] initWithDisplay:NSLocalizedString(@"摄像头", nil) icon:@"camera_ico" type:NETSMoreSettingCamera disableIcon:@"no_camera_ico" disable:NO];
        NETSMoreSettingStatusModel *micro = [[NETSMoreSettingStatusModel alloc] initWithDisplay:NSLocalizedString(@"麦克风", nil) icon:@"micro_ico" type:NETSMoreSettingMicro disableIcon:@"no_micro_ico" disable:NO];
        NETSMoreSettingStatusModel *earBack = [[NETSMoreSettingStatusModel alloc] initWithDisplay:NSLocalizedString(@"耳返", nil) icon:@"earback_ico" type:NETSMoreSettingEarback disableIcon:@"no_earback_ico" disable:YES];
        NETSMoreSettingModel *reverse = [[NETSMoreSettingModel alloc] initWithDisplay:NSLocalizedString(@"翻转", nil) icon:@"switch_camera_ico" type:NETSMoreSettingReverse];
        NETSMoreSettingModel *filter = [[NETSMoreSettingModel alloc] initWithDisplay:NSLocalizedString(@"滤镜", nil) icon:@"anchor_more_filter" type:NETSMoreSettingfilter];
        NETSMoreSettingModel *end = [[NETSMoreSettingModel alloc] initWithDisplay:NSLocalizedString(@"结束直播", nil) icon:@"close_ico" type:NETSMoreSettingEndLive];
//        _moreSettings = @[camera, micro, earBack, reverse,filter, end];
        // 先隐藏耳返
        _moreSettings = @[camera, micro, reverse,filter, end];
    }
    return _moreSettings;
}

#pragma mark - NELiveListener

- (void)onPKInvitedWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    NSString *msg = [NSString stringWithFormat:NSLocalizedString(@"\"%@\"邀请你进行PK,是否接受?", nil), actionAnchor.userName.length ? actionAnchor.userName : actionAnchor.userUuid];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"邀请PK", nil) message:msg preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"拒绝", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [[NELiveKit shared] rejectPKWithCallback:^(NSInteger code, NSString * _Nullable msg) {
            if (code != 0) {
                [NETSToast showToast:[NSString stringWithFormat:@"拒绝邀请失败 code:%zd msg:%@", code, msg]];
            }
        }];
    }];

    UIAlertAction *confirm = [UIAlertAction actionWithTitle:NSLocalizedString(@"接受", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [NETSToast showLoading];
        [[NELiveKit shared] acceptPKWithCallback:^(NSInteger code, NSString * _Nullable msg) {
            ntes_main_async_safe(^{
                [NETSToast hideLoading];
                if (code != 0) {
                    [NETSToast showToast:[NSString stringWithFormat:@"接受邀请失败 code:%zd msg:%@", code, msg]];
                }
            });
        }];
    }];

    [alert addAction:cancel];
    [alert addAction:confirm];
    [self presenAlert:alert];
}

- (void)onPKAcceptedWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast showToast:@"对方主播接受PK"];
}

- (void)onPKTimeoutWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast hideLoading];
    [NETSToast showToast:NSLocalizedString(@"PK连接超时，已自动取消", nil)];
    if (self.pkAlert) {
        [self.pkAlert dismissViewControllerAnimated:YES completion:nil];
        self.pkAlert = nil;
    }
    if ([self.pkInvitingBar superview]) {
        [self.pkInvitingBar dismiss];
    }
}

- (void)onPKCanceledWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    [NETSToast showToast:NSLocalizedString(@"对方已取消PK邀请", nil)];
    if (self.pkAlert) {
        [self.pkAlert dismissViewControllerAnimated:YES completion:nil];
        self.pkAlert = nil;
    }
}

- (void)onPKRejectedWithActionAnchor:(NELivePKAnchor *)actionAnchor {
    if ([self.pkInvitingBar superview]) {
        [self.pkInvitingBar dismiss];
    }
    [NETSToast showToast:NSLocalizedString(@"对方已拒绝你的PK邀请", nil)];
}

- (void)onPKStartWithPkStartTime:(NSInteger)pkStartTime pkCountDown:(NSInteger)pkCountDown inviter:(NELivePKAnchor *)inviter invitee:(NELivePKAnchor *)invitee {
    // pk布局
    ntes_main_async_safe(^{
        [self layoutPkLiveWithInviter:inviter invitee:invitee];
        [self.pkInvitingBar dismiss];
        [NETSToast hideLoading];
    });
    
    // 开始pk倒计时
    [self.pkStatusBar countdownWithSeconds:pkCountDown prefix:@"PK "];
    [self.pkStatusBar refreshWithLeftRewardCoins:0 leftRewardAvatars:@[] rightRewardCoins:0 rightRewardAvatars:@[]];
}

- (void)onPKPunishingStartWithPkPenaltyCountDown:(NSInteger)pkPenaltyCountDown inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards {
    NETSPkResult res = NETSPkUnknownResult;
    if (inviteeRewards == inviterRewards) {
        res = NETSPkTieResult;
    }
    else if ((inviteeRewards > inviterRewards && ![NELiveKit shared].isInviter) ||
             (inviteeRewards < inviterRewards && [NELiveKit shared].isInviter)) {
        res = NETSPkCurrentAnchorWin;
    }
    else {
        res = NETSPkOtherAnchorWin;
    }
    
    if (res == NETSPkTieResult) {
        [self.pkStatusBar stopCountdown];
    } else {
//        int32_t seconds = kPkLivePunishTotalTime - (int32_t)((data.currentTime - data.pkPulishmentTime) / 1000);
        [self.pkStatusBar countdownWithSeconds:pkPenaltyCountDown prefix:@"惩罚 "];
    }
    
    //刷新惩罚UI
    CGRect leftIcoFrame = CGRectMake((self.localRender.width - 100) * 0.5, self.localRender.bottom - 100, 100, 100);
    CGRect rightIcoFrame = CGRectMake(self.remoteRender.left + (self.remoteRender.width - 100) * 0.5, self.remoteRender.bottom - 100, 100, 100);
    
    self.pkSuccessIco.image = [UIImage imageNamed:@"pk_succeed_ico"];
    self.pkFailedIco.image = [UIImage imageNamed:@"pk_failed_ico"];
    
    switch (res) {
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
    
    [self.view addSubview:self.pkSuccessIco];
    [self.view addSubview:self.pkFailedIco];
    
}

- (void)onPKEndWithReason:(NSInteger)reason pkEndTime:(NSInteger)pkEndTime inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards countDownEnd:(BOOL)countDownEnd {
    if (self.pkAlert) {
        [self.pkAlert dismissViewControllerAnimated:YES completion:nil];
        self.pkAlert = nil;
    }
    if (countDownEnd) {//自然倒计时结束 不弹toast提示
        return;
    }
    [NETSToast showToast:[NSString stringWithFormat:@"PK结束 reason:%zd", reason]];
    [self layoutSingleLive];
}

- (void)onVideoFrameCallbackWithSampleBuffer:(CMSampleBufferRef)sampleBuffer {
    CVPixelBufferRef pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    [[NETSFUManger shared] renderItemsToPixelBuffer:pixelBuffer];
}

- (void)onMembersJoinWithMembers:(NSArray<NERoomMember *> *)members {
    [self.audienceInfo reloadWithDatas:[NELiveKit shared].members];
    NSMutableString *nick = [NSMutableString string];
    for (NERoomMember *member in members) {
        //        [nick appendFormat:@"%@,", member.name.length ? member.name : member.userUuid];
        // 先全部使用userUuid
        if (![member.userUuid isEqualToString:[NELiveKit shared].userUuid]) {
            [nick appendFormat:@"%@,", member.userUuid];
        }
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
        if (![member.userUuid isEqualToString:[NELiveKit shared].userUuid]) {
            [nick appendFormat:@"%@,", member.userUuid];
        }
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

- (void)onRewardReceivedWithRewarderUserUuid:(NSString *)rewarderUserUuid rewarderUserName:(NSString *)rewarderUserName giftId:(NSInteger)giftId anchorReward:(NELiveAnchorReward *)anchorReward otherAnchorReward:(NELiveAnchorReward *)otherAnchorReward {
    BOOL isInviter = [anchorReward.userUuid isEqualToString:[NELiveKit shared].userUuid];
    if ([NELiveKit shared].pkStatus == NELivePKStatusPking) {
        // pk状态,更新pk状态栏
        int64_t leftReward = isInviter ? anchorReward.pkRewardTotal : otherAnchorReward.pkRewardTotal;
        NSArray *leftAvatars = isInviter ? anchorReward.rewardAvatars : otherAnchorReward.rewardAvatars;
        int64_t rightReward = isInviter ? otherAnchorReward.pkRewardTotal : anchorReward.pkRewardTotal;
        NSArray *rightAvatars = isInviter ? otherAnchorReward.rewardAvatars : anchorReward.rewardAvatars;
        [self.pkStatusBar refreshWithLeftRewardCoins:leftReward leftRewardAvatars:leftAvatars rightRewardCoins:rightReward rightRewardAvatars:rightAvatars];
    }
    
    // 更新用户信息栏(云币值)
    int32_t coins = anchorReward.rewardTotal;
    if (!isInviter) {
        coins = otherAnchorReward.rewardTotal;
    }
    [self.anchorInfo updateCoins:coins];
    
    //如果打赏的是当前主播,向聊天室发送打赏消息
    if (isInviter) {
        NETSMessageModel *message = [[NETSMessageModel alloc] init];
        message.type = NETSMessageReward;
        message.giftId = giftId;
        message.giftFrom = rewarderUserName.length ? rewarderUserName : rewarderUserUuid;
        [self.chatView addMessages:@[message]];
    }
}

- (void)onMessagesReceivedWithMessages:(NSArray<NERoomMessage *> *)messages {
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:messages.count];
    for (NERoomMessage *message in messages) {
        NETSMessageModel *model = [[NETSMessageModel alloc] init];
        model.type = NETSMessageNormal;
        model.text = message.text;
        model.sender = message.from.userUuid;
        [array addObject:model];
    }
    [self.chatView addMessages:array];
}

- (void)onLiveEndWithReason:(NSInteger)reason {
    [NETSToast showToast:@"直播结束"];
    if ([self.presentedViewController isKindOfClass:[UIAlertController class]]) {//防止在销毁房间的时候有其他主播邀请pk
        [self.navigationController dismissViewControllerAnimated:YES completion:^{
            [self.navigationController popViewControllerAnimated:YES];
        }];
    } else {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

#pragma mark - 当键盘事件

- (void)keyboardWillShow:(NSNotification *)aNotification {
    NSDictionary *userInfo = [aNotification userInfo];
    CGRect rect = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    CGFloat keyboardHeight = rect.size.height;
    CGFloat chatViewHeight = [self chatViewHeight];
    [UIView animateWithDuration:[userInfo[UIKeyboardAnimationDurationUserInfoKey] doubleValue] animations:^{
        self.chatView.frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - chatViewHeight - keyboardHeight - 50, kScreenWidth - 16 - 60 - 20, chatViewHeight);
        self.toolBar.frame = CGRectMake(0, kScreenHeight - keyboardHeight - 50, kScreenWidth, 50);
    }];
    [self.view bringSubviewToFront:self.toolBar];

}

- (void)keyboardWillHide:(NSNotification *)aNotification {
    CGFloat chatViewHeight = [self chatViewHeight];
    [UIView animateWithDuration:0.1 animations:^{
        self.chatView.frame = CGRectMake(8, kScreenHeight - (kIsFullScreen ? 34 : 0) - 64 - chatViewHeight, kScreenWidth - 16 - 60 - 20, chatViewHeight);
        self.toolBar.frame = CGRectMake(0, kScreenHeight + 50, kScreenWidth, 50);
    }];
}
#pragma mark - NETSInvitingBarDelegate
- (void)clickCancelInviting:(NETSInviteBarType)barType {
    [[NELiveKit shared] cancelPKInviteWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        if (code != 0) {
            ntes_main_async_safe(^{
                [NETSToast showToast:[NSString stringWithFormat:@"取消邀请失败 %zd %@", code, msg]];
            });
        }
    }];
}

- (void)_authCamera {
    void(^quitBlock)(void) = ^(void) {
        [NETSToast showToast:NSLocalizedString(@"直播需要开启相机权限", nil)];
    };
    
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    switch (authStatus) {
        case AVAuthorizationStatusRestricted:
        case AVAuthorizationStatusDenied:
            quitBlock();
            break;
        case AVAuthorizationStatusNotDetermined:
        {
            [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
                if (!granted) {
                    quitBlock();
                } else {
                    dispatch_queue_t queue= dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0);
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1* NSEC_PER_SEC)), queue, ^{
                        ntes_main_async_safe(^{
                            self.cameraEnabled = true;
                            [self tryToStartLive];
                        });
                    });
                }
            }];
        }
            break;
        case AVAuthorizationStatusAuthorized:
        {
            ntes_main_async_safe(^{
                self.cameraEnabled = true;
                [self tryToStartLive];
            });
        }
            break;
        default:
            break;
    }
}

- (void)_authMic {
    void(^quitBlock)(void) = ^(void) {
        [NETSToast showToast:NSLocalizedString(@"直播需要开启麦克风权限", nil)];
    };
    
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeAudio];
    switch (authStatus) {
        case AVAuthorizationStatusRestricted:
        case AVAuthorizationStatusDenied:
            quitBlock();
            break;
        case AVAuthorizationStatusNotDetermined:
        {
            [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
                if (!granted) {
                    quitBlock();
                } else {
                    dispatch_queue_t queue= dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0);
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1* NSEC_PER_SEC)), queue, ^{
                        ntes_main_async_safe(^{
                            self.micEnabled = true;
                            [self tryToStartLive];
                        });
                    });
                }
            }];
        }
            break;
        case AVAuthorizationStatusAuthorized:
        {
            ntes_main_async_safe(^{
                self.micEnabled = true;
                [self tryToStartLive];
            });
        }
            break;
        default:
            break;
    }
}

@end
