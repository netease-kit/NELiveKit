//
//  NETabbarController.m
//  NEOnlinePK
//
//  Created by Ginger on 2022/2/28.
//

#import "NETabbarController.h"
#import "NEMenuViewController.h"
#import "NEPersonViewController.h"

@interface NETabbarController ()

@end

@implementation NETabbarController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initTabbarControllerStyle];
    [self addChildViewControllers];
}

- (void)initTabbarControllerStyle {
    self.tabBar.tintColor = [UIColor whiteColor];
    self.tabBar.barStyle = UIBarStyleBlack;
}

- (void)addChildViewControllers {
    NEMenuViewController *menuVC = [[NEMenuViewController alloc] init];
    UINavigationController *appNav = [[UINavigationController alloc] initWithRootViewController:menuVC];
    self.menuNavController = appNav;
    appNav.tabBarItem.title = NSLocalizedString(@"应用", nil);
    appNav.tabBarItem.image = [UIImage imageNamed:@"application"];
    appNav.tabBarItem.selectedImage = [UIImage imageNamed:@"application_select"];

    NEPersonViewController *personVC = [[NEPersonViewController alloc] init];
    UINavigationController *personNav = [[UINavigationController alloc] initWithRootViewController:personVC];
    personNav.tabBarItem.title = NSLocalizedString(@"个人中心", nil);
    personNav.tabBarItem.image = [UIImage imageNamed:@"mine"];
    personNav.tabBarItem.selectedImage = [UIImage imageNamed:@"mine_select"];
   
    self.viewControllers = @[appNav,personNav];
}

@end
