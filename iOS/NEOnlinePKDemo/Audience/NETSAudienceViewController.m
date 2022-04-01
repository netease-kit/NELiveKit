//
//  NETSAudienceViewController.m
//  NEOnlinePKDemo
//
//  Created by Ginger on 2022/3/20.
//

#import "NETSAudienceViewController.h"
#import "NETSAudienceBottomBar.h"
#import "NETSAudienceSendGiftSheet.h"
#import "UIViewController+Gesture.h"

@interface NETSAudienceViewController () <UICollectionViewDelegate, UICollectionViewDataSource>


//数据源
@property(nonatomic, strong) NSArray<NELiveDetail *> *liveData;

//选中的index
@property(nonatomic, assign) NSInteger                 selectRoomIndex;

@property(nonatomic, strong) UICollectionView          *collectionView;

@property(nonatomic, strong) NSIndexPath               *currentIndexPath;

@property (nonatomic, copy)  NSString                  *currentRoomUuid;

/// 即将移出屏幕的cell indePath
@property (nonatomic, strong) NSIndexPath               *playingCellIndexPath;
/// 即将移出屏幕的cell
@property (nonatomic, strong) NETSAudienceChatRoomCell  *playingCell;


@property (nonatomic, strong) UIView *videoView;

@end

@implementation NETSAudienceViewController

- (instancetype)initWithScrollData:(NSArray<NELiveDetail *> *)liveData currentRoom:(NSInteger)selectRoomIndex {
    if (self = [super init]) {
       _liveData = liveData;
       _selectRoomIndex = selectRoomIndex;
    }
    return self;
}

//- (instancetype)initWithDetail:(NELiveDetail *)detail {
//    if ([super init]) {
//        self.detail = detail;
//    }
//    return self;
//}

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
    }
//    [NELiveKit shared].listener = self;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [UIViewController popGestureClose:self];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [UIViewController popGestureOpen:self];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self nets_addSubViews];
}

-(void)nets_addSubViews {
    [self.view addSubview:self.collectionView];
    //滚动到选择位置
    [self.collectionView scrollToItemAtIndexPath:[NSIndexPath indexPathForRow:self.selectRoomIndex inSection:0] atScrollPosition:UICollectionViewScrollPositionNone animated:true];
    self.currentIndexPath = [NSIndexPath indexPathForRow:self.selectRoomIndex inSection:0];
    
    NELiveDetail *detailModel = self.liveData[self.selectRoomIndex];
    self.currentRoomUuid    = detailModel.live.roomUuid ;
    
    self.playingCellIndexPath = [NSIndexPath indexPathForRow:self.selectRoomIndex inSection:0];
}



- (void)dealloc {
    NSLog(@"aaaaaaaaaaaaaaaaaaaaaaaaaa");
}

#pragma mark <UICollectionViewDataSource>
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.liveData.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    NELiveDetail *roomModel = self.liveData[indexPath.row];
    NETSAudienceChatRoomCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[NETSAudienceChatRoomCell description]
        
                                                                               forIndexPath:indexPath];
    if (self.currentIndexPath.row == indexPath.row) {
        cell.roomModel = roomModel;
    }
    if (_playingCell == nil && indexPath.row == self.playingCellIndexPath.row) {
        _playingCell = cell;
    }
    return cell;
}

#pragma mark <UICollectionViewDelegate>
- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    if (self.playingCellIndexPath != nil ) {
        CGRect cellRect = [self.collectionView convertRect:self.playingCell.frame toView:self.collectionView];
        CGRect rectInSuperview = [self.collectionView convertRect:cellRect toView:self.view];
        if (rectInSuperview.origin.y >= kScreenHeight || rectInSuperview.origin.y + rectInSuperview.size.height <= 0) {
            [_playingCell shutdownPlayer];
            [_playingCell closeConnectMicRoomAction];
        }
    }
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    CGPoint pointInView = [self.view convertPoint:self.collectionView.center toView:self.collectionView];
    NSIndexPath *indexPathNow = [self.collectionView indexPathForItemAtPoint:pointInView];
    
    // 滚动到相同的直播间 不做处理
    if ([self.currentRoomUuid isEqualToString:[self.liveData[indexPathNow.row] live].roomUuid]) {
        return;
    }
    
    // 退出之前的聊天室
//    NELiveDetail *roomModel = self.liveData[self.selectRoomIndex];
    [[NELiveKit shared] leaveLiveWithCallback:^(NSInteger code, NSString * _Nullable msg) {
        ntes_main_async_safe(^{
            if (code == 0) {
                NSLog(@"退出上一个直播间成功");
            } else {
                [NETSToast showToast:[NSString stringWithFormat:@"离开直播失败 code:%zd msg:%@", code, msg]];
            }
        });
    }];
    

    // 赋值给记录当前坐标的变量
    self.currentIndexPath = indexPathNow;
    self.currentRoomUuid = [self.liveData[indexPathNow.row] live].roomUuid;
    
    self.selectRoomIndex = indexPathNow.row;
    
    NSLog(@"观众端 滚动结束后的section %ld，row: %ld",(long)indexPathNow.section,(long)indexPathNow.row);
    NETSAudienceChatRoomCell *currentCell = (NETSAudienceChatRoomCell*)[self.collectionView cellForItemAtIndexPath:indexPathNow];
    [currentCell resetPageUserinterface];
    currentCell.roomModel = self.liveData[indexPathNow.row];
    
    // 记录正在播放cell赋值
    self.playingCellIndexPath = self.currentIndexPath;
    self.playingCell = currentCell;
}
#pragma mark - lazyMethod

- (UICollectionView *)collectionView {
    if (!_collectionView) {
        UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc]init];
        flowLayout.minimumLineSpacing = 0;
        flowLayout.minimumInteritemSpacing = 0;
        flowLayout.itemSize = CGSizeMake(kScreenWidth, kScreenHeight);
        flowLayout.scrollDirection = UICollectionViewScrollDirectionVertical;
        _collectionView = [[UICollectionView alloc]initWithFrame:CGRectMake(0, 0, kScreenWidth, kScreenHeight) collectionViewLayout:flowLayout];
        _collectionView.backgroundColor = HEXCOLOR(0x262623);
        _collectionView.delegate = self;
        _collectionView.dataSource = self;
        _collectionView.showsVerticalScrollIndicator = NO;
        _collectionView.keyboardDismissMode = UIScrollViewKeyboardDismissModeOnDrag;
        _collectionView.alwaysBounceVertical = YES; //垂直方向遇到边框是否总是反弹
        _collectionView.pagingEnabled = YES;
        [_collectionView registerClass:[NETSAudienceChatRoomCell class] forCellWithReuseIdentifier:[NETSAudienceChatRoomCell description]];
        if (@available(iOS 11.0, *)) {
            _collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
        }
    }
    return _collectionView;
}


#pragma mark - NELiveListener
//- (void)onMembersJoinWithMembers:(NSArray<NERoomMember *> *)members {
//    NETSAudienceChatRoomCell *cell = [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//    [cell updateRoomMessageByMembersJoinWithMembers:members];
//}
//
//- (void)onMembersLeaveWithMembers:(NSArray<NERoomMember *> *)members {
//    NETSAudienceChatRoomCell *cell = [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//    [cell updateRoomMessageByMembersLeaveWithMembers:members];
//}
//
//- (void)onRewardReceivedWithRewarderUserUuid:(NSString *)rewarderUserUuid rewarderUserName:(NSString *)rewarderUserName giftId:(NSInteger)giftId anchorReward:(NELiveAnchorReward *)anchorReward otherAnchorReward:(NELiveAnchorReward *)otherAnchorReward {
//    NETSAudienceChatRoomCell *cell = [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//    [cell updateRoomMessageOnRewardReceivedWithRewarderUserUuid:rewarderUserUuid rewarderUserName:rewarderUserName giftId:giftId anchorReward:anchorReward otherAnchorReward:otherAnchorReward];
//}
//
//- (void)onMessagesReceivedWithMessages:(NSArray<NERoomMessage *> *)messages {
//    [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//    NETSAudienceChatRoomCell *cell = [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//    [cell updateOnMessagesReceivedWithMessages:messages];
//}
//
//- (void)onPKStartWithPkStartTime:(NSInteger)pkStartTime pkCountDown:(NSInteger)pkCountDown inviter:(NELivePKAnchor *)inviter invitee:(NELivePKAnchor *)invitee {
//    NETSAudienceChatRoomCell *cell = [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//    [cell updateOnPKStartWithPkStartTime:pkStartTime pkCountDown:pkCountDown inviter:inviter invitee:invitee];
//}
//
//- (void)onPKPunishingStartWithPkPenaltyCountDown:(NSInteger)pkPenaltyCountDown inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards {
//    NETSAudienceChatRoomCell *cell = [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//
//    [cell updateOnPKPunishingStartWithPkPenaltyCountDown:pkPenaltyCountDown inviterRewards:inviterRewards inviteeRewards:inviteeRewards];
//}
//
//- (void)onPKEndWithReason:(NSInteger)reason pkEndTime:(NSInteger)pkEndTime inviterRewards:(NSInteger)inviterRewards inviteeRewards:(NSInteger)inviteeRewards countDownEnd:(BOOL)countDownEnd {
//    NETSAudienceChatRoomCell *cell = [self.collectionView cellForItemAtIndexPath:self.currentIndexPath];
//    [cell updateOnPKEndWithReason:reason pkEndTime:(NSInteger)pkEndTime inviterRewards:inviterRewards inviteeRewards:inviteeRewards countDownEnd:countDownEnd];
//}
//
//- (void)onLiveEnd {
//    [self exitLiveWithMessage:nil];
//}
//- (void)exitLiveWithMessage:(NSString *)showMessage {
//    if (!showMessage || showMessage.length <= 0) {
//        showMessage = @"直播结束";
//    }
//    ///切换为主线程，避免其他地方子线程调用
//    dispatch_async(dispatch_get_main_queue() , ^{
//        [NETSToast showToast:showMessage];
//        [[NENavigator shared].navigationController popViewControllerAnimated:YES];
//    });
//}
//
//- (void)didChangeRoomStatus:(BOOL)isPKing {
//    // 视频上边缘距离设备顶部
//    CGFloat top = 64 + (kIsFullScreen ? 44 : 20);
//    if (isPKing) {
//        CGFloat y = top - (kScreenHeight - (640 / 720.0 * kScreenWidth)) / 2.0;
//        self.videoView.frame = CGRectMake(0, y, kScreenWidth, kScreenHeight);
//    } else {
//        self.videoView.frame = self.view.frame;
//    }
//}
@end
