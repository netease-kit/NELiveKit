//
//  NETSLiveListViewController.m
//  NEOnlinePKDemo
//
//  Created by Ginger on 2022/2/28.
//

#import "NETSLiveListViewController.h"
#import "NETSLiveListVM.h"
#import "NETSLiveListCell.h"
#import "NETSEmptyListView.h"
#import "NEPkLiveViewController.h"
#import <MJRefresh/MJRefresh.h>
#import <ReactiveObjC/ReactiveObjC.h>

@interface NETSLiveListViewController () <UICollectionViewDelegate, UICollectionViewDataSource>

@property (nonatomic, strong)   NETSLiveListVM       *viewModel;
@property (nonatomic, strong)   UICollectionView     *collectionView;
@property (nonatomic, strong)   UIButton            *startPkBtn;
@property (nonatomic, strong)   NETSEmptyListView            *emptyView;

@end

@implementation NETSLiveListViewController

- (instancetype)init {
    self = [super init];
    if (self) {
        self.viewModel = [[NETSLiveListVM alloc] init];
        self.hidesBottomBarWhenPushed = YES;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setupViews];
    [self bindAction];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    
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
        [self.viewModel load];
    }
}

- (void)setupViews {
    self.title = NSLocalizedString(@"PK直播", nil);
    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.view.backgroundColor = HEXCOLOR(0x000000);
    self.navigationController.navigationBar.topItem.title = @"";

    [self.view addSubview:self.collectionView];
    [self.view addSubview:self.startPkBtn];
    
    [self.collectionView addSubview:self.emptyView];
    self.collectionView.backgroundColor = [UIColor clearColor];
    self.emptyView.centerX = self.collectionView.centerX;
    self.emptyView.centerY = self.collectionView.centerY - (kIsFullScreen ? 88 : 64);
    [self setNavigationBackgroundColor:[UIColor blackColor]];
    }

- (void)setNavigationBackgroundColor:(UIColor *)color {

   if (@available(iOS 15.0, *)) {
      UINavigationBarAppearance *appearance = [[UINavigationBarAppearance alloc] init];
      appearance.backgroundEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleDark];
       [appearance setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor colorWithWhite:1 alpha:1]}];
       self.navigationController.navigationBar.scrollEdgeAppearance = appearance;
       self.navigationController.navigationBar.standardAppearance = appearance;
    }
}

- (void)bindAction {
    @weakify(self);
    MJRefreshGifHeader *mjHeader = [MJRefreshGifHeader headerWithRefreshingBlock:^{
        @strongify(self);
        [self.viewModel load];
    }];
    [mjHeader setTitle:NSLocalizedString(@"下拉更新", nil) forState:MJRefreshStateIdle];
    [mjHeader setTitle:NSLocalizedString(@"下拉更新", nil) forState:MJRefreshStatePulling];
    [mjHeader setTitle:NSLocalizedString(@"更新中...", nil) forState:MJRefreshStateRefreshing];
    mjHeader.lastUpdatedTimeLabel.hidden = YES;
    [mjHeader setTintColor:[UIColor whiteColor]];
    self.collectionView.mj_header = mjHeader;
    
    self.collectionView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingBlock:^{
        @strongify(self);
        if (self.viewModel.isEnd) {
            [NETSToast showToast:NSLocalizedString(@"无更多内容", nil)];
            [self.collectionView.mj_footer endRefreshing];
        } else {
            [self.viewModel loadMore];
        }
    }];
    
    [RACObserve(self.viewModel, datas) subscribeNext:^(NSArray *array) {
        @strongify(self);
        [self.collectionView reloadData];
        self.emptyView.hidden = [array count] > 0;
    }];
    [RACObserve(self.viewModel, isLoading) subscribeNext:^(id  _Nullable x) {
        @strongify(self);
        if (self.viewModel.isLoading == NO) {
            [self.collectionView.mj_header endRefreshing];
            [self.collectionView.mj_footer endRefreshing];
        }
    }];
    [RACObserve(self.viewModel, error) subscribeNext:^(NSError * _Nullable err) {
        if (!err) { return; }
        if (err.code == 1003) {
            [NETSToast showToast:NSLocalizedString(@"直播列表为空", nil)];
        } else {
            NSString *msg = err.userInfo[NSLocalizedDescriptionKey] ?: NSLocalizedString(@"请求直播列表错误", nil);
            [NETSToast showToast:msg];
        }
    }];
}

#pragma mark - UICollectionView delegate

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return [self.viewModel.datas count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    return [NETSLiveListCell cellWithCollectionView:collectionView
                                          indexPath:indexPath
                                              datas:self.viewModel.datas];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    if ([self.viewModel.datas count] > indexPath.row) {
//        NELiveDetail *model = self.viewModel.datas[indexPath.row];
        [[NENavigator shared] showLivingRoom:self.viewModel.datas withIndex:(int)indexPath.row];
    }
}

#pragma mark - lazy load

- (UICollectionView *)collectionView {
    if (!_collectionView) {
        UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
        layout.itemSize = [NETSLiveListCell size];
        layout.scrollDirection = UICollectionViewScrollDirectionVertical;
        layout.minimumInteritemSpacing = 8;
        layout.minimumLineSpacing = 8;
        layout.sectionInset = UIEdgeInsetsMake(8, 8, 8, 8);
        
        CGRect rect = CGRectMake(0, 0, self.view.width, kScreenHeight - (kIsFullScreen ? 34 : 0));
        _collectionView = [[UICollectionView alloc] initWithFrame:rect collectionViewLayout:layout];
        [_collectionView registerClass:[NETSLiveListCell class] forCellWithReuseIdentifier:[NETSLiveListCell description]];
        _collectionView.delegate = self;
        _collectionView.dataSource = self;
        _collectionView.showsVerticalScrollIndicator = NO;
    }
    return _collectionView;
}

- (UIButton *)startPkBtn {
    if (!_startPkBtn) {
        CGFloat topOffset = self.view.height - 100 - (kIsFullScreen ? 34 : 0);
        _startPkBtn = [[UIButton alloc] initWithFrame:CGRectMake(self.view.width - 100, topOffset, 100, 100)];
        UIImage *img = [UIImage imageNamed:@"start_pk_ico"];
        [_startPkBtn setImage:img forState:UIControlStateNormal];
        [_startPkBtn addTarget:self action:@selector(startLive) forControlEvents:UIControlEventTouchUpInside];
    }
    return _startPkBtn;
}

- (void)startLive {
    NEPkLiveViewController *view = [[NEPkLiveViewController alloc] init];
    [self.navigationController pushViewController:view animated:true];
}

- (NETSEmptyListView *)emptyView {
    if (!_emptyView) {
        _emptyView = [[NETSEmptyListView alloc] initWithFrame:CGRectZero];
    }
    return _emptyView;
}

- (NETSLiveListVM *)viewModel {
    if (!_viewModel) {
        _viewModel = [[NETSLiveListVM alloc]init];
    }
    return _viewModel;
}

- (void)dealloc {
//    [[NIMSDK sharedSDK].loginManager logout:^(NSError * _Nullable error) {
//        YXAlogInfo(@"pk直播主播端销毁,IM登出, error: %@...", error);
//    }];
}

@end
