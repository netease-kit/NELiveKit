//
//  NEMenuViewController.m
//  NEOnlinePK
//
//  Created by Ginger on 2022/2/28.
//

#import "NEMenuViewController.h"
#import "NENavCustomView.h"
#import "NEMenuCell.h"
#import "NETSLiveListViewController.h"

static NSString *cellID = @"menuCellID";

@interface NEMenuViewController () <UITableViewDelegate,UITableViewDataSource>

@property(strong,nonatomic)UITableView *tableView;
@property(strong,nonatomic)UIImageView *bgImageView;

@property (nonatomic, strong)   NSArray *datas;

@end

@implementation NEMenuViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setupDatas];
    [self setupUI];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];
}

- (void)setupDatas {
    NEMenuCellModel *live = [[NEMenuCellModel alloc]initWithTitle:NSLocalizedString(@"PK直播", nil) subtitle:NSLocalizedString(@"从单人直播到主播间PK，观众连麦多种玩法", nil) icon:@"home_pkLive_icon"];
    NEMenuCellModel *connectMic = [[NEMenuCellModel alloc]initWithTitle:NSLocalizedString(@"多人连麦直播", nil)  subtitle:NSLocalizedString(@"支持1V4主播和观众的视频互动", nil) icon:@"home_connectMic_icon"];
//    NSArray *sectionTwo = @[live, connectMic];
    NSArray *sectionTwo = @[live];
    _datas = @[sectionTwo];
}

- (void)setupUI {
    [self.view addSubview:self.bgImageView];
    [self.bgImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(UIEdgeInsetsZero);
    }];
    
    NENavCustomView *customView = [[NENavCustomView alloc] init];
    [self.view addSubview:customView];
    CGFloat statusHeight = [[UIApplication sharedApplication] statusBarFrame].size.height;
    [customView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.top.right.mas_equalTo(0);
        make.height.mas_equalTo(statusHeight + 80);
    }];
    
    [self.view addSubview:self.tableView];
    [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(customView.mas_bottom);
        make.right.mas_equalTo(-20);
        make.left.mas_equalTo(20);
        make.bottom.mas_equalTo(0);
    }];
}

#pragma mark - UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return [_datas count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if ([_datas count] > section) {
        NSArray *arr = _datas[section];
        return [arr count];
    }
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return [NEMenuCell height];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([_datas count] > indexPath.section) {
        NSArray *array = _datas[indexPath.section];
        if ([array count] > indexPath.row) {
            NEMenuCellModel *data = array[indexPath.row];
            return [NEMenuCell cellWithTableView:tableView indexPath:indexPath data:data];
        }
    }
    return [NEMenuCell new];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
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
        return;
    }
//
//    if ([_datas count] > indexPath.section) {
//        NSArray *array = _datas[indexPath.section];
//        if ([array count] > indexPath.row) {
//            NEMenuCellModel *data = array[indexPath.row];
//            if (!data.block) { return; }
//                [NETSToast showLoading];
//                [self setupIMWithLoginCompletion:^(NSError * _Nullable error) {
//                    [NETSToast hideLoading];
//                    if (error) {
//                        [NETSToast showToast:NSLocalizedString(@"IM登录失败", nil)];
//                        YXAlogInfo(@"IM登录失败, error: %@", error);
//                    } else {
//                        data.block();
//                    }
//                }];
//                return;
//        }
//    }
    
    switch (indexPath.section) {
        case 0: {
            NETSLiveListViewController *view = [[NETSLiveListViewController alloc] init];
            [self.navigationController pushViewController:view animated:true];
            break;
        }
        case 1:
            break;
        default:
            break;
    }
}

#pragma mark - property

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [UIView new];
        _tableView.backgroundColor = [UIColor clearColor];
        _tableView.rowHeight = 104;
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        [_tableView registerClass:[NEMenuCell class] forCellReuseIdentifier:cellID];
    }
    return _tableView;
}

- (UIImageView *)bgImageView {
    if (!_bgImageView) {
        _bgImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"menu_bg"]];
    }
    return _bgImageView;
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

@end
