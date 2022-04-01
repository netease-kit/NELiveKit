//
//  AppDelegate.m
//  NEOnlinePKDemo
//
//  Created by Ginger on 2022/2/28.
//

#import "AppDelegate.h"
#import "NETabbarController.h"
#import "TestViewController.h"

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.

    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.window.backgroundColor = UIColor.whiteColor;
    NETabbarController *tabbarCtrl = [[NETabbarController alloc]init];
//    TestViewController *tabbarCtrl = [[TestViewController alloc] init];
    self.window.rootViewController = tabbarCtrl;
    [NENavigator shared].navigationController = tabbarCtrl.menuNavController;
    [self.window makeKeyAndVisible];
    
    NELiveKitOptions *o = [[NELiveKitOptions alloc] init];
    o.appKey = kAppKey;
    [[NELiveKit shared] initializeWithOptions:o callback:^(NSInteger code, NSString * _Nullable msg) {
        if (code == 0) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self setupLoginSDK];
//                [[NELiveKit shared] loginWithAccount:@"jinjie" token:@"jinjie" callback:^(NSInteger code, NSString * _Nullable msg) {
//                    dispatch_async(dispatch_get_main_queue(),^{
//                        if (code == 0) {
//                            [NETSToast showToast:@"登录成功"];
//                            [[NELiveKit shared] stopLiveWithLiveRecordId:19];
//                        } else {
//                            [NETSToast showToast:[NSString stringWithFormat:@"登录失败 %zd %@", code, msg]];
//                        }
//                    });
//                }];
            });
        }
    }];
    
    return YES;
}

- (UIInterfaceOrientationMask)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window {
    return UIInterfaceOrientationMaskPortrait;
}

- (void)setupLoginSDK {
    YXConfig *config = [[YXConfig alloc] init];
    config.appKey = kAppKey;
    config.parentScope = [NSNumber numberWithInt:5];
    config.scope = [NSNumber numberWithInt:3];
    config.supportInternationalize = NO;
    config.isOnline = YES;
    config.type = YXLoginPhone;
    AuthorManager *LoginManager = [AuthorManager shareInstance];
    [LoginManager initAuthorWithConfig:config];
    //    __weak typeof(self) weakSelf = self;
    if ([LoginManager canAutologin] == YES) {
        [LoginManager autoLoginWithCompletion:^(YXUserInfo * _Nullable userinfo, NSError * _Nullable error) {
            
            if (error == nil) {
                NSLog(@"统一登录sdk登录成功");
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
            } else {
                [NETSToast showToast:error.localizedDescription];
            }
        }];
    }else {
        NSLog(@"LoginManager startEntrance");
        
        [LoginManager startEntranceWithCompletion:^(YXUserInfo * _Nullable userinfo, NSError * _Nullable error) {
            if (error == nil) {
                NSLog(@"统一登录sdk登录成功");
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
            }else {
                [NETSToast showToast:error.localizedDescription];
            }
        }];
    }
}

@end
