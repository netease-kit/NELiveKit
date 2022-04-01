//
//  NETSAudienceMask.h
//  NLiteAVDemo
//
//  Created by Ease on 2020/11/25.
// Copyright (c) 2021 NetEase, Inc.  All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

#import <UIKit/UIKit.h>
//#import "NETSLiveModel.h"

NS_ASSUME_NONNULL_BEGIN


/// 观众端视频流状态
typedef NS_ENUM(NSInteger, NETSAudienceStreamStatus) {
    NETSAudienceStreamDefault   = 0,    // 默认单人视频流
    NETSAudienceIMPkStart       = 1,    // 接到pk开始信令
    NETSAudienceStreamMerge     = 2,    // 合流成功
    NETSAudienceIMPkEnd         = 3,     // 接到pk结束信令
    NETSAudienceConnectStart    = 4      //接到pk连麦信令
};



@class NELiveRoomListDetailModel, NECreateRoomResponseModel,NETSConnectMicAttachment;

///
/// 客户端蒙层
///

@protocol NETSAudienceMaskDelegate <NSObject>
/**
  播放器偏移
 @param status  - 直播间状态
 */
- (void)didChangeRoomStatus:(NETSAudienceStreamStatus)status;

/**
 直播间关闭
 */
- (void)didLiveRoomClosed;

////音频变化通知
//- (void)didAudioChanged:(NESeatInfo *)seatInfo;
//
//- (void)didVideoChanged:(NESeatInfo *)seatInfo;
///// 房间成员上下麦位
///// @param isOnWheat 是否上麦
///// @param seatInfo 麦位信息
//- (void)memberSeatStateChanged:(BOOL)isOnWheat seatInfo:(NESeatInfo *)seatInfo;
//
/// 观众加入rtc房间失败
/// @param errorCode 错误码
- (void)joinchannelFailed:(NSInteger)errorCode;

@end

@interface NETSAudienceMask : UIView

@property (nonatomic, weak) id<NETSAudienceMaskDelegate>    delegate;

/// 直播间是否可用(控制侧滑，YES可侧滑,NO不可侧滑)
@property(nonatomic, assign)    BOOL chatRoomAvailable;
/// 房间模型
@property (nonatomic, strong)   NELiveDetail       *room;
/// 房间详情
@property (nonatomic, strong)   NELiveDetail   *info;
/// 直播间状态
@property (nonatomic, assign)   int  roomStatus;

/// 关闭直播间，调用关闭蒙版
- (void)closeChatRoom;
//清空直播间数据
- (void)clearCurrentLiveRoomData;

@end

NS_ASSUME_NONNULL_END
