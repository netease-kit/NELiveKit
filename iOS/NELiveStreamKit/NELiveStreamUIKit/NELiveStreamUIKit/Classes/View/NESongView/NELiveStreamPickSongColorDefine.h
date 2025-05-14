// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#ifndef NELiveStreamPickSongColorDefine_h
#define NELiveStreamPickSongColorDefine_h

#define color_313D3C 0x313D3C
#define color_FFFFFF 0xFFFFFF
#define color_5F5F5F 0x5F5F5F
#define color_FE7081 0xFE7081
#define color_FF4FA6 0xFF4FA6
#define color_BFBFBF 0xBFBFBF
#define color_999999 0x999999

typedef enum : NSUInteger {
  // 歌曲已点
  SONG_ERROR_SONG_POINTED = 1011,
  // 个人点歌受限
  SONG_ERROR_SONG_POINTED_USER_LIMIT = 1009,
  // 房间点歌受限
  SONG_ERROR_SONG_POINTED_ROOM_LIMIT = 1010,

} NELiveStreamOrderSongErrorCode;

#define HEXCOLORA(rgbValue, alphaValue)                                \
  [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16)) / 255.0 \
                  green:((float)((rgbValue & 0x00FF00) >> 8)) / 255.0  \
                   blue:((float)(rgbValue & 0x0000FF)) / 255.0         \
                  alpha:alphaValue]

#define HEXCOLOR(rgbValue) HEXCOLORA(rgbValue, 1.0)

#define liveStreamUILog @"NELiveStreamUILog"

#endif /* NELiveStreamPickSongColorDefine_h */
