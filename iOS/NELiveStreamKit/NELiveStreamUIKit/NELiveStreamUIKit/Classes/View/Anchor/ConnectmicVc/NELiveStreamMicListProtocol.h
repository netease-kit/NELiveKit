//// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#ifndef NELiveStreamMicListProtocol_h
#define NELiveStreamMicListProtocol_h

#import <Foundation/Foundation.h>

@class NELiveStreamSeatItem;

@protocol NEliveStreamMicListDelegate <NSObject>

- (NSArray<NELiveStreamSeatItem *> *)didRequestSubmitMicData;
- (NSArray<NELiveStreamSeatItem *> *)didRequestMicManagerData;
- (NSArray<NELiveStreamSeatItem *> *)didRequestInviteMicData;

- (void)onAcceptWithSeatItem:(NELiveStreamSeatItem *)seatItem;
- (void)onRejectWithSeatItem:(NELiveStreamSeatItem *)seatItem;
- (void)onKickWithSeatItem:(NELiveStreamSeatItem *)seatItem;
- (void)onInviteWithSeatItem:(NELiveStreamSeatItem *)seatItem;

@end

#endif /* NELiveStreamMicListProtocol_h */
