//
//  NSMacro.h
//  NEOnlinePK
//
//  Created by Ginger on 2022/2/28.
//

#import <Foundation/Foundation.h>

// UIColor宏定义

#define HEXCOLORA(rgbValue, alphaValue) [UIColor \
colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
green:((float)((rgbValue & 0x00FF00) >> 8))/255.0 \
blue:((float)(rgbValue & 0x0000FF))/255.0 \
alpha:alphaValue]

#define HEXCOLOR(rgbValue) HEXCOLORA(rgbValue, 1.0)


/// 设备尺寸
#define kScreenWidth        [UIScreen mainScreen].bounds.size.width
#define kScreenHeight       [UIScreen mainScreen].bounds.size.height
#define kScreenMinLen       MIN([UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height)
#define kScreenMaxLen       MAX([UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height)
#define KStatusHeight       [[UIApplication sharedApplication] statusBarFrame].size.height
#define KNavBottom          KStatusHeight + 44
#define KIsSmallSize        [UIScreen mainScreen].bounds.size.width <= 568 ? YES : NO
#define IPHONE_X \
({BOOL isPhoneX = NO;\
if (@available(iOS 11.0, *)) {\
isPhoneX = [[UIApplication sharedApplication] delegate].window.safeAreaInsets.bottom > 0.0;\
}\
(isPhoneX);})


/// 是否全面屏
#define kIsFullScreen        (@available(iOS 11.0, *) && UIApplication.sharedApplication.keyWindow.safeAreaInsets.bottom > 0.0)


NSString * kFormatNum(int32_t num);

bool isEmptyString(NSString *string);


// 线程
void ntes_main_sync_safe(dispatch_block_t block);
void ntes_main_async_safe(dispatch_block_t block);

NSString *getAccessToken();
void setAccessToken(NSString * token);


/// pk结果枚举
typedef NS_ENUM(NSUInteger, NETSPkResult) {
    NETSPkUnknownResult     = -1,   // 未知获胜状态
    NETSPkCurrentAnchorWin  = 0,    // 当前主播获胜
    NETSPkOtherAnchorWin    = 1,    // 另一个主播获胜
    NETSPkTieResult         = 2      // 平局
};

static const NSString * kAppKey = @"";
static const NSString * kHost = @"";
