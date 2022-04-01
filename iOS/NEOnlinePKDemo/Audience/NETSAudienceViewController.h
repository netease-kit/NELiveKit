//
//  NETSAudienceViewController.h
//  NEOnlinePKDemo
//
//  Created by Ginger on 2022/3/20.
//

#import <UIKit/UIKit.h>
#import "NETSAudienceChatRoomCell.h"

NS_ASSUME_NONNULL_BEGIN

@interface NETSAudienceViewController : UIViewController

/// 构造函数
/// @param liveData 直播数据源
/// @param selectRoomIndex 选中的房间
- (instancetype)initWithScrollData:(NSArray<NELiveDetail *> *)liveData currentRoom:(NSInteger)selectRoomIndex;

@end

NS_ASSUME_NONNULL_END
